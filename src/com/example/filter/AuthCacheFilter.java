package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/*")
public class AuthCacheFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 1) Disable caching for all dynamic pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 2) Allow static resources, login, register, and logout through
        String uri = request.getRequestURI();
        if (uri.endsWith("/login.jsp")
         || uri.endsWith("/login")
         || uri.endsWith("/register.jsp")
         || uri.endsWith("/register")
         || uri.endsWith("/logout")
         || uri.contains("/css/")   // your static dirs
         || uri.contains("/js/")
         || uri.contains("/images/")) {
            chain.doFilter(req, res);
            return;
        }

        // 3) For everything else, block if no userId in session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 4) OK to proceed
        chain.doFilter(req, res);
    }
}
