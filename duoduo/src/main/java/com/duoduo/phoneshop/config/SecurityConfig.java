package com.duoduo.phoneshop.config;

import com.duoduo.phoneshop.filter.SessionToSecurityContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SessionToSecurityContextFilter sessionToSecurityContextFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF 保护 (仅为演示，生产环境应开启)
                .csrf(csrf -> csrf.disable())

                // 配置访问权限
                .authorizeRequests(authorize -> authorize
                        // 静态资源和公共页面
                        .antMatchers(
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/sockjs-node/**",
                                "/__webpack_dev_server__/**",
                                "/favicon.ico",
                                "/error"  // 重要：允许访问错误页面
                        ).permitAll()

                        // 用户认证相关页面
                        .antMatchers(
                                "/user/login",
                                "/user/register",
                                "/user/sendCode"
                        ).permitAll()

                        // 商品浏览相关页面
                        .antMatchers(
                                "/category/**",
                                "/search",
                                "/product/**"
                        ).permitAll()

                        // 需要用户权限的页面
                        .antMatchers(
                                "/user/**",
                                "/cart/**",
                                "/order/**",
                                "/address/**",
                                "/favorite/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // 管理员页面
                        .antMatchers("/admin/**").hasRole("ADMIN")

                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 禁用Spring Security的默认表单登录
                .formLogin(form -> form.disable())

                // 登出配置
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Session 管理
                .sessionManagement(session -> session
                        .invalidSessionUrl("/user/login")
                        .maximumSessions(1)
                        .expiredUrl("/user/login?expired")
                )

                // 处理无权限访问的情况
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/user/login")    // 无权限时跳转到登录页
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/user/login");
                        })
                )

                // 添加自定义过滤器
                .addFilterBefore(sessionToSecurityContextFilter, UsernamePasswordAuthenticationFilter.class);
    }
}