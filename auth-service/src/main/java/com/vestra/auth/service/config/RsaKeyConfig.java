package com.vestra.auth.service.config;

import com.vestra.auth.service.utils.PemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class RsaKeyConfig {

    @Value("${rsa.private-key-location}")
    private Resource privateKeyResource;

    @Value("${rsa.public-key-location}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        return PemUtils.readPrivateKey(privateKeyResource);
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        return PemUtils.readPublicKey(publicKeyResource);
    }

}
