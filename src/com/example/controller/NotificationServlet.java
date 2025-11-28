package com.example.controller;

import com.example.dao.NotificationDAO;
import com.example.model.Notification;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer uid = (Integer) req.getSession().getAttribute("userId");
        if (uid == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            List<Notification> notes = NotificationDAO.getNotifications(uid);
            resp.setContentType("application/json");
            new Gson().toJson(notes, resp.getWriter());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
