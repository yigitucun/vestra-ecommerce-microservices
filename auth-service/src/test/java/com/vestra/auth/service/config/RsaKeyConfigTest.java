package com.vestra.auth.service.config;

import com.vestra.auth.service.utils.PemUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyConfigTest {

    @Test
    @DisplayName("Should fall back to generating ephemeral RSA keypair when no keys are configured")
    void shouldFallbackToEphemeralKeys() {
        RsaKeyConfig config = new RsaKeyConfig(new DefaultResourceLoader());
        config.init();

        RSAPrivateKey privateKey = config.rsaPrivateKey();
        RSAPublicKey publicKey = config.rsaPublicKey();

        assertThat(privateKey).isNotNull();
        assertThat(publicKey).isNotNull();
        assertThat(privateKey.getModulus()).isEqualTo(publicKey.getModulus());
    }

    @Test
    @DisplayName("Should configure keys from string properties and auto-derive public key if omitted")
    void shouldConfigureKeysFromString() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        String privatePem = PemUtils.toPem(keyPair.getPrivate());

        RsaKeyConfig config = new RsaKeyConfig(new DefaultResourceLoader());
        ReflectionTestUtils.setField(config, "privateKeyContent", privatePem);
        config.init();

        RSAPrivateKey privateKey = config.rsaPrivateKey();
        RSAPublicKey publicKey = config.rsaPublicKey();

        assertThat(privateKey).isNotNull();
        assertThat(publicKey).isNotNull();
        assertThat(privateKey.getModulus()).isEqualTo(((RSAPrivateKey) keyPair.getPrivate()).getModulus());
        assertThat(publicKey.getModulus()).isEqualTo(((RSAPublicKey) keyPair.getPublic()).getModulus());
    }
}
