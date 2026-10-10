package com.vestra.auth.service.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PemUtilsTest {

    @Test
    @DisplayName("Should generate valid 2048-bit RSA key pair and serialize to PEM")
    void shouldGenerateAndSerializeKeypair() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getPrivate()).isInstanceOf(RSAPrivateKey.class);
        assertThat(keyPair.getPublic()).isInstanceOf(RSAPublicKey.class);

        String privatePem = PemUtils.toPem(keyPair.getPrivate());
        String publicPem = PemUtils.toPem(keyPair.getPublic());

        assertThat(privatePem).startsWith("-----BEGIN PRIVATE KEY-----");
        assertThat(privatePem).contains("-----END PRIVATE KEY-----");
        assertThat(publicPem).startsWith("-----BEGIN PUBLIC KEY-----");
        assertThat(publicPem).contains("-----END PUBLIC KEY-----");
    }

    @Test
    @DisplayName("Should parse private and public keys from PEM strings")
    void shouldParseKeysFromPem() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        String privatePem = PemUtils.toPem(keyPair.getPrivate());
        String publicPem = PemUtils.toPem(keyPair.getPublic());

        RSAPrivateKey parsedPrivate = PemUtils.parsePrivateKey(privatePem);
        RSAPublicKey parsedPublic = PemUtils.parsePublicKey(publicPem);

        assertThat(parsedPrivate).isNotNull();
        assertThat(parsedPublic).isNotNull();
        assertThat(parsedPrivate.getModulus()).isEqualTo(((RSAPrivateKey) keyPair.getPrivate()).getModulus());
        assertThat(parsedPublic.getModulus()).isEqualTo(((RSAPublicKey) keyPair.getPublic()).getModulus());
    }

    @Test
    @DisplayName("Should parse PEM strings with escaped newlines (e.g. from .env variables)")
    void shouldParsePemWithEscapedNewlines() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        String privatePem = PemUtils.toPem(keyPair.getPrivate());
        String escapedPrivatePem = privatePem.replace("\n", "\\n");

        RSAPrivateKey parsedPrivate = PemUtils.parsePrivateKey(escapedPrivatePem);
        assertThat(parsedPrivate).isNotNull();
        assertThat(parsedPrivate.getModulus()).isEqualTo(((RSAPrivateKey) keyPair.getPrivate()).getModulus());
    }

    @Test
    @DisplayName("Should parse base64-encoded PEM string")
    void shouldParseBase64EncodedPem() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        String publicPem = PemUtils.toPem(keyPair.getPublic());
        String base64EncodedPem = Base64.getEncoder().encodeToString(publicPem.getBytes());

        RSAPublicKey parsedPublic = PemUtils.parsePublicKey(base64EncodedPem);
        assertThat(parsedPublic).isNotNull();
        assertThat(parsedPublic.getModulus()).isEqualTo(((RSAPublicKey) keyPair.getPublic()).getModulus());
    }

    @Test
    @DisplayName("Should mathematically derive public key from private key when public key is omitted")
    void shouldDerivePublicKeyFromPrivateKey() {
        KeyPair keyPair = PemUtils.generateRsaKeyPair(2048);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey originalPublic = (RSAPublicKey) keyPair.getPublic();

        RSAPublicKey derivedPublic = PemUtils.derivePublicKey(privateKey);

        assertThat(derivedPublic).isNotNull();
        assertThat(derivedPublic.getModulus()).isEqualTo(originalPublic.getModulus());
        assertThat(derivedPublic.getPublicExponent()).isEqualTo(originalPublic.getPublicExponent());
    }

    @Test
    @DisplayName("Should throw clear exception when given empty or PKCS#1 key")
    void shouldThrowOnInvalidInput() {
        assertThatThrownBy(() -> PemUtils.parsePrivateKey(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> PemUtils.parsePrivateKey("-----BEGIN RSA PRIVATE KEY-----\nabc\n-----END RSA PRIVATE KEY-----"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PKCS#1");
    }
}
