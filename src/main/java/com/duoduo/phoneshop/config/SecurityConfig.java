package com.duoduo.phoneshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security配置类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF保护(仅为演示，生产环境应开启)
                .csrf().disable()

                // 配置访问权限
                .authorizeRequests()
                // 允许所有用户访问的路径
                .antMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/user/login", "/user/register", "/user/sendCode").permitAll()
                .antMatchers("/category/**", "/search", "/product/**").permitAll()

                // 需要USER角色的路径
                .antMatchers("/user/**", "/cart/**", "/order/**", "/address/**", "/favorite/**").hasAnyRole("USER", "ADMIN")

                // 需要ADMIN角色的路径
                .antMatchers("/admin/**").hasRole("ADMIN")

                // 其他请求需要认证
                .anyRequest().authenticated()

                // 配置登录
                .and()
                .formLogin()
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .defaultSuccessUrl("/")
                .permitAll()

                // 配置登出
                .and()
                .logout()
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/")
                .permitAll()

                // 配置session管理
                .and()
                .sessionManagement()
                .invalidSessionUrl("/user/login")
                .maximumSessions(1)
                .expiredUrl("/user/login");
    }
}