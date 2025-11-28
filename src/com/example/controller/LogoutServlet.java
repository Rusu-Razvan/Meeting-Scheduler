package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // invalidate the session
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // Redirect to login page
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    // also handle GET if someone navigates there directly
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
}
