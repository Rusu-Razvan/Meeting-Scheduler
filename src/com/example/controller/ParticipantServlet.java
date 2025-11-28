package com.example.controller;

import com.example.dao.AppointmentDAO;
import com.example.dao.NotificationDAO;
import com.example.dao.ParticipantDAO;
import com.example.dao.UserDAO;
import com.example.model.Appointment;
import com.example.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/participant")
public class ParticipantServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1) Who is responding?
        int userId = (Integer) req.getSession().getAttribute("userId");
        // 2) Which meeting?
        int apptId = Integer.parseInt(req.getParameter("appointmentId"));
        // 3) What did they choose?
        String status  = req.getParameter("status");    // "ACCEPTED" or "DECLINED"
        String comment = req.getParameter("comment");   // may be empty

        try {
            // 4) Update their RSVP/comment
            ParticipantDAO.update(apptId, userId, status, comment);

            // 5) Notify the creator
            Appointment appt = AppointmentDAO.getById(apptId);
            int creatorId = appt.getCreatorId();
            User me = UserDAO.getById(userId);

         // build a simple accept/decline notification — no comment appended
            String msg = me.getUsername() + " "
                       + status.toLowerCase()
                       + " your meeting \"" + appt.getTitle() + "\" on "
                       + appt.getStartTime().toLocalDate();
            NotificationDAO.create(creatorId, msg);

        } catch (Exception e) {
            throw new ServletException(e);
        }

        // 6) Back to the details page
        resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
    }
}
