package com.vestra.auth.service.utils;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {

    private PemUtils() {
    }

    public static RSAPrivateKey parsePrivateKey(String keyContent) {
        if (keyContent == null || keyContent.isBlank()) {
            throw new IllegalArgumentException("Private key içeriği boş olamaz.");
        }
        if (keyContent.contains("BEGIN RSA PRIVATE KEY")) {
            throw new IllegalArgumentException(
                    "Sağlanan anahtar PKCS#1 formatında (BEGIN RSA PRIVATE KEY). " +
                    "Java PKCS#8 formatı (BEGIN PRIVATE KEY) gerektirir. " +
                    "Dönüştürmek için: openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.pem -out private_pkcs8.pem"
            );
        }
        try {
            String clean = cleanPem(keyContent);
            byte[] decoded = Base64.getDecoder().decode(clean);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            throw new IllegalStateException("RSA Private key ayrıştırılamadı. Formatın geçerli bir PKCS#8 PEM olduğunu kontrol edin.", e);
        }
    }

    public static RSAPublicKey parsePublicKey(String keyContent) {
        if (keyContent == null || keyContent.isBlank()) {
            throw new IllegalArgumentException("Public key içeriği boş olamaz.");
        }
        try {
            String clean = cleanPem(keyContent);
            byte[] decoded = Base64.getDecoder().decode(clean);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            throw new IllegalStateException("RSA Public key ayrıştırılamadı. Formatın geçerli bir X.509 PEM olduğunu kontrol edin.", e);
        }
    }

    public static RSAPrivateKey readPrivateKey(Resource resource) {
        if (resource == null || !resource.exists()) {
            throw new IllegalArgumentException("Private key kaynağı bulunamadı veya mevcut değil: " + resource);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parsePrivateKey(content);
        } catch (IOException e) {
            throw new IllegalStateException("Private key kaynağından okunamadı: " + resource, e);
        }
    }

    public static RSAPublicKey readPublicKey(Resource resource) {
        if (resource == null || !resource.exists()) {
            throw new IllegalArgumentException("Public key kaynağı bulunamadı veya mevcut değil: " + resource);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parsePublicKey(content);
        } catch (IOException e) {
            throw new IllegalStateException("Public key kaynağından okunamadı: " + resource, e);
        }
    }

    public static RSAPublicKey derivePublicKey(RSAPrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateCrtKey crtKey) {
            try {
                RSAPublicKeySpec spec = new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent());
                KeyFactory factory = KeyFactory.getInstance("RSA");
                return (RSAPublicKey) factory.generatePublic(spec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new IllegalStateException("Private key üzerinden Public key türetilemedi.", e);
            }
        }
        throw new IllegalArgumentException("Private key bir RSAPrivateCrtKey değil, public key otomatik türetilemez.");
    }

    public static KeyPair generateRsaKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA anahtar üretimi desteklenmiyor.", e);
        }
    }

    public static String toPem(PrivateKey privateKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    public static String toPem(PublicKey publicKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n";
    }

    private static String cleanPem(String content) {
        String trimmed = content.trim();
        trimmed = trimmed.replace("\\n", "\n").replace("\\r", "");

        if (!trimmed.contains("-----BEGIN") && !trimmed.contains("-----END")) {
            try {
                byte[] decoded = Base64.getDecoder().decode(trimmed);
                String maybePem = new String(decoded, StandardCharsets.UTF_8);
                if (maybePem.contains("-----BEGIN")) {
                    trimmed = maybePem.replace("\\n", "\n").replace("\\r", "");
                } else {
                    return trimmed.replaceAll("\\s", "");
                }
            } catch (IllegalArgumentException ignored) {
                // Not a base64 encoded PEM, use trimmed string as-is
            }
        }

        return trimmed
                .replaceAll("-----BEGIN [A-Z0-9 ]+-----", "")
                .replaceAll("-----END [A-Z0-9 ]+-----", "")
                .replaceAll("\\s", "");
    }
}
