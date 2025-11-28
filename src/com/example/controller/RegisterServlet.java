package com.example.controller;

import com.example.utils.DBUtil;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                req.setAttribute("error", "User already exists!");
                req.getRequestDispatcher("register.jsp").forward(req, resp);
                return;
            }

            String hashed = hash(password);
            ps = conn.prepareStatement("INSERT INTO users(username,email,password_hash,created_at) VALUES(?,?,?,NOW())"); 
            		//Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashed);
            ps.executeUpdate();
            
           /* try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);                   // id-ul nou creat
                    req.getSession(true).setAttribute("userId", id);
                    req.getSession().setAttribute("username", username); // dacă îți trebuie
                }
            }*/

            resp.sendRedirect("login.jsp"); //tried changing to dashboard.jsp but the if clause in the file stopped me
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
