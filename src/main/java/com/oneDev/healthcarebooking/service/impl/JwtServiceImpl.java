package com.oneDev.healthcarebooking.service.impl;

import com.oneDev.healthcarebooking.configuration.JwtSecretConfig;
import com.oneDev.healthcarebooking.enumaration.ExceptionType;
import com.oneDev.healthcarebooking.exception.ApplicationException;
import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtSecretConfig jwtSecretConfig;
    private final SecretKey signKey;

    @Override
    public String generateToken(UserInfo userInfo) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtSecretConfig.getJwtExpirationMs());
        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(signKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(signKey)
                    .build();
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (SignatureException | IllegalArgumentException | UnsupportedJwtException | MalformedJwtException |
                 ExpiredJwtException ex) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Token tidak valid atau kadaluwarsa");
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build();
        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getSubject();

    }
}
