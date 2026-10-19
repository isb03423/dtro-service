package com.project.dtro.common.config;

import com.project.dtro.security.MemberSessionRefreshFilter;
import com.project.dtro.user.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableMethodSecurity
public class SpringConfig {

    // 비밀번호 암호화
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, MemberRepository members) throws Exception {
        http
                .addFilterBefore(new MemberSessionRefreshFilter(members), AuthorizationFilter.class)
                // CORS 설정
                .cors(Customizer.withDefaults())
                // Vue SPA용 CSRF 보호
                .csrf(csrf -> csrf.spa())
                // 세션 기반 인증
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                // API 접근 권한
                .authorizeHttpRequests(auth -> auth
                                // 전체 접근 허용
                                .requestMatchers(
                                        "/",
                                        "/api/auth/login",
                                        "/api/auth/csrf",
                                        "/api/members",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/error",
                                        "/favicon.ico"
                                ).permitAll()
                                .requestMatchers("/api/auth/me").authenticated()
                                .requestMatchers("/api/v1/customer-service-overview").hasRole("ADMIN")
                                .requestMatchers("/api/trains", "/api/trains/**", "/api/train-overview")
                                .hasAnyRole("TRAIN_MANAGER", "ADMIN")
                                // 최고관리자
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                // 로그인 필요
                                .requestMatchers("/api/members/me", "/api/boards/**").authenticated()
                                // 나머지 요청
                                .anyRequest().permitAll()
                )
                // 로그인 처리
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("userid")
                        .passwordParameter("password")
                        // 로그인 성공
                        .successHandler((req, res, auth) -> {
                            res.setStatus(200);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":true}");
                        })
                        // 로그인 실패
                        .failureHandler((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":false,\"message\":\"아이디 또는 비밀번호가 올바르지 않습니다.\"}");
                        })
                        .permitAll()
                )
                // 로그아웃 처리
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        // 로그아웃 성공
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(200);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":true}");
                        })
                )
                // 인증 및 인가 예외 처리
                .exceptionHandling(ex -> ex
                        // 401: 로그인하지 않은 사용자
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
                        })
                        // 403: 접근 권한 없음
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}");
                        })
                );

        return http.build();
    }
}
