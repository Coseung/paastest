package com.example.registertest.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider  {

    // application.yml 에 설정한 비밀키와 만료시간을 가져옵니다.
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long tokenValidityInMilliseconds; // 예: 1800000 (30분)

    private Key key;

    /**
     * Bean 주입이 완료된 후, secretKey 문자열을 암호화 키 객체(Key)로 변환합니다.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 1. 토큰 생성 (ID와 권한을 담아서 발급)
     */
    public String createToken(String userId, String role) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role); // Custom Claim으로 권한 추가

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256) // HS256 알고리즘 사용
                .compact();
    }

    /**
     * 2. 토큰 검증 (Validation)
     * 예외가 발생하면 구조에 맞게 에러 메시지를 실어 예외를 던집니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("잘못된 JWT 서명입니다.");
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            log.error("유효하지 않은 구성의 JWT 토큰입니다.");
            throw new JwtException("유효하지 않은 구성의 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 형式の JWT 토큰입니다.");
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰의 Claims 문자열이 비어있습니다.");
            throw new JwtException("JWT Claims가 비어있습니다.");
        }
    }

    /**
     * 3. 토큰에서 유저 ID(Subject) 추출
     */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 4. 토큰에서 유저 권한(Role) 추출
     */
    public String getUserRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 토큰 복호화 및 Claims 추출 공통 메서드
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
