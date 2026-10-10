package com.vestra.auth.service.config;

import com.vestra.auth.service.utils.PemUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
@Configuration
public class RsaKeyConfig {

    @Value("${rsa.private-key:}")
    private String privateKeyContent;

    @Value("${rsa.public-key:}")
    private String publicKeyContent;

    @Value("${rsa.private-key-location:}")
    private String privateKeyLocation;

    @Value("${rsa.public-key-location:}")
    private String publicKeyLocation;

    private final ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public RsaKeyConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        resolveKeys();
    }

    private void resolveKeys() {
        // 1. Private key çözümleme (Çevre değişkeni -> Dosya konumu)
        if (StringUtils.hasText(privateKeyContent)) {
            log.info("RSA Private Key konfigürasyondan/çevre değişkeninden başarıyla yüklendi.");
            this.privateKey = PemUtils.parsePrivateKey(privateKeyContent);
        } else if (StringUtils.hasText(privateKeyLocation)) {
            Resource resource = resourceLoader.getResource(privateKeyLocation);
            if (resource.exists()) {
                log.info("RSA Private Key dosya kaynağından yüklendi: {}", privateKeyLocation);
                this.privateKey = PemUtils.readPrivateKey(resource);
            }
        }

        // 2. Eğer Private key bulunduysa, Public key'i çözümle veya türet
        if (this.privateKey != null) {
            if (StringUtils.hasText(publicKeyContent)) {
                log.info("RSA Public Key konfigürasyondan/çevre değişkeninden başarıyla yüklendi.");
                this.publicKey = PemUtils.parsePublicKey(publicKeyContent);
            } else if (StringUtils.hasText(publicKeyLocation)) {
                Resource resource = resourceLoader.getResource(publicKeyLocation);
                if (resource.exists()) {
                    log.info("RSA Public Key dosya kaynağından yüklendi: {}", publicKeyLocation);
                    this.publicKey = PemUtils.readPublicKey(resource);
                }
            }

            // Public key verilmemişse private key'den otomatik türet
            if (this.publicKey == null) {
                log.info("RSA Public Key ayrıca belirtilmedi; Private Key üzerinden otomatik türetildi.");
                this.publicKey = PemUtils.derivePublicKey(this.privateKey);
            }
            return;
        }

        // 3. Fallback: Anahtar tanımlanmamışsa yerel dev/test için bellekte (in-memory) üret
        log.warn("=========================================================================================");
        log.warn("[GÜVENLİK UYARISI] RSA_PRIVATE_KEY veya geçerli bir anahtar dosyası konfigüre edilmemiş!");
        log.warn("Geliştirme ve test ortamı için bellekte geçici (ephemeral) 2048-bit RSA anahtar çifti oluşturuldu.");
        log.warn("UYARI: Servis her yeniden başladığında anahtar değişecek ve eski token'lar geçersiz olacaktır.");
        log.warn("Production ortamında RSA_PRIVATE_KEY ve RSA_PUBLIC_KEY çevre değişkenlerini MUTLAKA tanımlayın!");
        log.warn("=========================================================================================");

        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        return this.privateKey;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        return this.publicKey;
    }
}
