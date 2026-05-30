package com.example.registertest.global.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider; // 토큰 검증 및 파싱을 담당하는 컴포넌트 (직접 구현 필요)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 "Bearer " 토큰 추출
        String token = resolveToken(request);

        try {
            // 2. 토큰이 존재하고 유효한지 검증
            if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

                // 3. 토큰에서 ID와 권한(Role) 추출
                String userId = jwtProvider.getUserId(token);
                String role = jwtProvider.getUserRole(token); // 예: "ROLE_USER" 또는 "ROLE_ADMIN"

                // 4. Spring Security가 인식할 권한 객체 리스트 생성
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                // 5. UsernamePasswordAuthenticationToken 생성
                // Principal 자리에 유저 ID를 넣고, Credentials는 보안상 null, 마지막에 권한 등록
                Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                // 6. SecurityContextHolder에 인증 객체 저장 (이제 인증된 사용자로 인식됨)
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security Context에 '{}' 인증 정보를 저장했습니다.", userId);
            }

            // 7. 다음 필터로 요청 넘기기
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰 검증 중 에러 발생 시 (만료, 위변조 등) 바로 클라이언트에게 에러 응답 전송
            log.error("JWT 검증 실패: {}", e.getMessage());
            handleJwtException(response, e.getMessage());
        }
    }

    /**
     * HTTP Request Header에서 Bearer 토큰을 추출하는 메서드
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 뒷부분의 순수 토큰만 반환
        }
        return null;
    }

    /**
     * Filter 단에서 발생한 예외를 직접 JSON 응답으로 구워주는 메서드
     */
    private void handleJwtException(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized

        // 실무에서는 커스텀 ErrorResponse 객체를 만들어 ObjectMapper로 변환하는 것이 좋습니다.
        String jsonResponse = String.format("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}", message);

        response.getWriter().write(jsonResponse);
    }
}