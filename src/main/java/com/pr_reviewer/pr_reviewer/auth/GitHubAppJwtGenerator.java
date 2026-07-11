package com.pr_reviewer.pr_reviewer.auth;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class GitHubAppJwtGenerator {
    private final String appId;
    private final RSAPrivateKey privateKey;

    public GitHubAppJwtGenerator(
            @Value("${github.app.id}") String appId,
            @Value("${github.app.private-key-path}") String privateKeyPath) throws Exception {
        this.appId = appId;
        this.privateKey = loadPrivateKey(privateKeyPath);
    }

    public String generateJwt() {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuedAt(Date.from(now.minusSeconds(60)))
                .expiration(Date.from(now.plusSeconds(600))) // 10 mins max
                .issuer(appId)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private RSAPrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }
}
