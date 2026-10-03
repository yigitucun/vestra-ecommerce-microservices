package com.vestra.auth.service.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final RSAKey rsaKey;

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String,Object>> jwks(){
        return ResponseEntity.ok()
                .body(new JWKSet(rsaKey.toPublicJWK()).toJSONObject());
    }
}
