package com.example.controller;

import com.example.dao.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/notifications/delete")
public class NotificationDeleteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        int nid = Integer.parseInt(idParam);
        try {
            NotificationDAO.deleteNotification(nid);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
