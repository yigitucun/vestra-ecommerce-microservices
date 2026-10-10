package com.vestra.payment.service.repository;

import com.vestra.payment.service.entity.Payment;
import com.vestra.payment.service.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderId(UUID orderId);
    List<Payment> findByUserId(UUID userId);
    Optional<Payment> findByOrderIdAndStatus(UUID orderId, PaymentStatus status);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
