package com.infotact.fleet.security;

import com.infotact.fleet.domain.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer:infotact-fleet}")
    private String issuer;

    @Value("${security.jwt.expires-minutes:480}")
    private long expiresMinutes;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiresMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(user.getUsername())
                .claim("roles", List.of(user.getRole().name()))
                .claim("name", user.getName())
                .claim("id", user.getId())
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        return this.jwtEncoder.encode(parameters).getTokenValue();
    }
}
