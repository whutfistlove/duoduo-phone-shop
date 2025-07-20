package com.duoduo.phoneshop.filter;

import com.duoduo.phoneshop.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Session到SecurityContext的过滤器
 * 用于将session中的用户信息同步到Spring Security的上下文中
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Component
public class SessionToSecurityContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");

            if (currentUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 创建权限列表
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + currentUser.getRole()));

                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(currentUser, null, authorities);

                // 设置到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("从Session恢复用户认证信息: {}", currentUser.getUsern());
            }
        }

        filterChain.doFilter(request, response);
    }
}