package com.duoduo.phoneshop.filter;

import com.duoduo.phoneshop.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
 * Session到SecurityContext同步过滤器
 * 将session中的用户信息同步到Spring Security上下文中
 */
@Component
public class SessionToSecurityContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 如果SecurityContext中已经有认证信息，则跳过
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从session中获取用户信息
        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                // 创建权限列表
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                // 如果是管理员，添加管理员权限
                if (currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                // 创建认证令牌
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(currentUser, null, authorities);

                // 设置到SecurityContext中
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}