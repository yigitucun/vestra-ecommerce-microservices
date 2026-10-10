package com.vestra.payment.service.service;

import com.vestra.common.dto.PaginatedResponse;
import com.vestra.common.web.exception.ApiException;
import com.vestra.payment.service.dto.PaymentResponse;
import com.vestra.payment.service.dto.ProcessPaymentRequest;
import com.vestra.payment.service.dto.RefundPaymentRequest;
import com.vestra.payment.service.entity.Payment;
import com.vestra.payment.service.entity.PaymentStatus;
import com.vestra.payment.service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.vestra.payment.service.client.OrderServiceClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventService outboxEventService;
    private final OrderServiceClient orderServiceClient;

    @Transactional
    public PaymentResponse processPayment(UUID userId, ProcessPaymentRequest request) {
        log.info("Ödeme işlemi başlatılıyor: orderId={}, orderNumber={}, amount={}, userId={}",
                request.orderId(), request.orderNumber(), request.amount(), userId);

        // 1. Siparişi order-service üzerinden doğrula
        OrderServiceClient.OrderDetails order = orderServiceClient.getOrder(request.orderId());
        if (order == null) {
            throw ApiException.notFound("Sipariş Bulunamadı", "Sipariş bilgileri doğrulanamadı: " + request.orderId());
        }

        // 2. IDOR Koruması: Sipariş gerçekten bu kullanıcıya mı ait?
        if (!order.userId().equals(userId)) {
            log.warn("[GÜVENLİK İHLALİ] IDOR tespiti! Kullanıcı={}, başkasına ait siparişi ödemeye çalıştı: orderId={}, gerçekSahip={}",
                    userId, request.orderId(), order.userId());
            throw ApiException.unAuthorized("Yetkisiz Erişim", "Bu sipariş için ödeme yapma yetkiniz bulunmamaktadır.");
        }

        // 3. Sipariş Durumu Kontrolü: Sipariş beklemede (PENDING) mi?
        if (!"PENDING".equalsIgnoreCase(order.status())) {
            log.warn("[GÜVENLİK İHLALİ] Geçersiz durumdaki siparişe ödeme denendi: orderId={}, durum={}",
                    request.orderId(), order.status());
            throw ApiException.badRequest("Geçersiz Sipariş Durumu", "Sadece beklemede (PENDING) olan siparişler için ödeme yapılabilir. Mevcut durum: " + order.status());
        }

        // 4. Amount Tampering Koruması: Ödenmek istenen tutar siparişin gerçek tutarıyla birebir eşleşiyor mu?
        if (request.amount().compareTo(order.totalAmount()) != 0) {
            log.warn("[GÜVENLİK İHLALİ] Ödeme tutarı manipülasyonu engellendi! orderId={}, gönderilenTutar={}, gerçekTutar={}",
                    request.orderId(), request.amount(), order.totalAmount());
            throw ApiException.badRequest("Tutar Uyuşmazlığı", "Ödeme tutarı sipariş tutarı ile uyuşmuyor. Beklenen tutar: " + order.totalAmount());
        }

        // 5. Sipariş için zaten başarılı bir ödeme var mı kontrolü
        paymentRepository.findByOrderIdAndStatus(request.orderId(), PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw ApiException.badRequest("Mükerrer Ödeme", "Bu sipariş için zaten başarılı bir ödeme kaydı bulunmaktadır");
                });

        String lastFour;
        boolean isDeclined;

        if (request.paymentToken() != null && !request.paymentToken().isBlank()) {
            // PSP Tokenization Akışı (Stripe / Iyzico / PayTR Hosted Form)
            String token = request.paymentToken().trim();
            lastFour = (request.cardLastFour() != null && !request.cardLastFour().isBlank())
                    ? request.cardLastFour()
                    : (token.length() >= 4 ? token.substring(token.length() - 4) : "4242");
            isDeclined = token.toLowerCase().contains("declined") || token.endsWith("0000");
        } else {
            // Doğrudan Kart Bilgileri Akışı (Sandbox Simülasyonu)
            String cleanCardNumber = request.cardNumber() != null ? request.cardNumber().replaceAll("\\s+", "") : "";
            lastFour = cleanCardNumber.length() >= 4
                    ? cleanCardNumber.substring(cleanCardNumber.length() - 4)
                    : cleanCardNumber;
            isDeclined = cleanCardNumber.endsWith("0000") || cleanCardNumber.equals("4000000000000000");
        }

        if (isDeclined) {
            log.warn("Ödeme simülasyonu: Banka veya PSP işlemi reddetti. orderId={}", request.orderId());

            Payment failedPayment = Payment.builder()
                    .orderId(request.orderId())
                    .orderNumber(request.orderNumber())
                    .userId(userId)
                    .amount(request.amount())
                    .status(PaymentStatus.FAILED)
                    .paymentMethod(request.paymentMethod())
                    .cardLastFour(lastFour)
                    .failureReason("Kart limiti yetersiz veya banka tarafından reddedildi (Simülasyon)")
                    .build();

            Payment savedFailed = paymentRepository.save(failedPayment);
            outboxEventService.publishPaymentFailedEvent(savedFailed);

            throw ApiException.badRequest("Ödeme Reddedildi", "Ödeme reddedildi: Kart limiti yetersiz veya banka işlemi onaylamadı");
        }

        // Başarılı ödeme simülasyonu
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .orderNumber(request.orderNumber())
                .userId(userId)
                .amount(request.amount())
                .status(PaymentStatus.SUCCESS)
                .paymentMethod(request.paymentMethod())
                .transactionId(transactionId)
                .cardLastFour(lastFour)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Ödeme başarıyla tamamlandı: paymentId={}, txnId={}", savedPayment.getId(), transactionId);

        outboxEventService.publishPaymentCompletedEvent(savedPayment);

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, RefundPaymentRequest request) {
        log.info("Ödeme iade işlemi başlatılıyor: paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Ödeme Bulunamadı", "Ödeme kaydı bulunamadı"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw ApiException.badRequest("Geçersiz Durum", "Yalnızca başarılı ödemeler iade edilebilir. Mevcut durum: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setFailureReason("İade edildi: " + request.reason());
        Payment updated = paymentRepository.save(payment);

        log.info("Ödeme iade edildi: paymentId={}, orderNumber={}", payment.getId(), payment.getOrderNumber());
        outboxEventService.publishPaymentRefundedEvent(updated, updated.getAmount(), request.reason());

        return PaymentResponse.fromEntity(updated);
    }

    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId, UUID userId, boolean isAdmin) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (!payments.isEmpty() && !isAdmin) {
            Payment first = payments.getFirst();
            if (!first.getUserId().equals(userId)) {
                throw ApiException.unAuthorized("Yetkisiz Erişim", "Bu siparişe ait ödeme bilgilerini görüntüleme yetkiniz yok");
            }
        }
        return payments.stream().map(PaymentResponse::fromEntity).toList();
    }

    public PaymentResponse getPaymentById(UUID paymentId, UUID userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Ödeme Bulunamadı", "Ödeme kaydı bulunamadı"));

        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw ApiException.unAuthorized("Yetkisiz Erişim", "Bu ödeme kaydını görüntüleme yetkiniz yok");
        }

        return PaymentResponse.fromEntity(payment);
    }

    public PaginatedResponse<PaymentResponse> getAllPayments(Pageable pageable, PaymentStatus status) {
        Page<Payment> page = status != null
                ? paymentRepository.findByStatus(status, pageable)
                : paymentRepository.findAll(pageable);

        List<PaymentResponse> responses = page.getContent().stream()
                .map(PaymentResponse::fromEntity)
                .toList();

        return new PaginatedResponse<>(
                responses,
                new PaginatedResponse.PageInfo(
                        page.getSize(),
                        page.getNumber(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
