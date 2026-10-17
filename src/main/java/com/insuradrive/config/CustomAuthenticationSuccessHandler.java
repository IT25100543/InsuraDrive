package com.insuradrive.config;

import com.insuradrive.service.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        Object principal = authentication.getPrincipal();

        String targetUrl = "/dashboard";
        if (principal instanceof CustomUserDetails userDetails) {
            String roleName = userDetails.getRoleName();
            session.setAttribute("activeRole", roleName);
            session.setAttribute("currentUser", userDetails.getUsername());
            session.setAttribute("displayName", userDetails.getDisplayName());
            if ("CUSTOMER".equalsIgnoreCase(roleName)) {
                targetUrl = "/customer/dashboard";
            }
        } else if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String authority = authentication.getAuthorities().iterator().next().getAuthority();
            if (authority.startsWith("ROLE_")) {
                String role = authority.substring(5);
                session.setAttribute("activeRole", role);
                if ("CUSTOMER".equalsIgnoreCase(role)) {
                    targetUrl = "/customer/dashboard";
                }
            }
            session.setAttribute("currentUser", authentication.getName());
        }

        response.sendRedirect(request.getContextPath() + targetUrl);
    }
}
