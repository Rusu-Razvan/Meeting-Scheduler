package com.example.controller;

import com.example.utils.DBUtil;
import com.example.dao.AppointmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.dao.ParticipantDAO;
import com.example.model.Appointment;
import com.example.model.Participant;
import com.example.dao.UserDAO;
import com.example.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.dao.NotificationDAO;
import com.example.dao.UserDAO;



@WebServlet(urlPatterns = {"/meetings", "/meetings/add"})
public class MeetingServlet extends HttpServlet {

    /* ----------  GET  /meetings?date=YYYY-MM-DD  ---------------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	String action = req.getParameter("action");
    	if ("add".equals(action)) {
    	    try {
    	        // 1) Load all users for the multi‐select
    	        List<User> users = UserDAO.getAll();
    	        req.setAttribute("userList", users);
    	        // 2) Preserve the date query param if you like
    	        req.setAttribute("date", req.getParameter("date"));
    	        // 3) Forward to JSP
    	        req.getRequestDispatcher("/addMeeting.jsp")
    	           .forward(req, resp);
    	    } catch (SQLException e) {
    	        throw new ServletException("Unable to load user list", e);
    	    }
    	    return;
    	}

        // 1) If there's an "id" parameter, show the details page
        String idParam = req.getParameter("id");
        if (idParam != null) {
            int apptId = Integer.parseInt(idParam);
            Appointment appt;
            List<Participant> participants;
            try {
                appt = AppointmentDAO.getById(apptId);
                participants = ParticipantDAO.getByAppointment(apptId);
            } catch (SQLException e) {
                throw new ServletException(e);
            }

            req.setAttribute("appointment", appt);
            req.setAttribute("participants", participants);
            // forward to your details JSP
            req.getRequestDispatcher("/meetingDetails.jsp")
               .forward(req, resp);
            return;
        }

        // 2) Existing calendar JSON logic by date
        String dateStr = req.getParameter("date");
        if (dateStr == null) {
            resp.sendError(400, "date missing");
            return;
        }

        final int uid = (int) req.getSession().getAttribute("userId");
        List<Meeting> list = new ArrayList<>();
        String sql =
            "SELECT a.id, a.title, DATE_FORMAT(a.start_time,'%H:%i') AS time, " +
            "       TIMESTAMPDIFF(MINUTE,a.start_time,a.end_time) AS duration " +
            "FROM   appointments a " +
            "JOIN   appointment_participants ap ON ap.appointment_id = a.id " +
            "WHERE  ap.user_id = ? AND DATE(a.start_time) = ? " +
            "ORDER  BY a.start_time";

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, uid);
            ps.setDate(2, Date.valueOf(LocalDate.parse(dateStr)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Meeting(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("time"),
                        rs.getInt("duration")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            out.print("[");
            for (int i = 0; i < list.size(); i++) {
                Meeting m = list.get(i);
                out.printf(
                    "{\"id\":%d,\"title\":\"%s\",\"time\":\"%s\",\"duration\":%d}%s",
                    m.id, escape(m.title), m.time, m.duration,
                    (i < list.size() - 1 ? "," : "")
                );
            }
            out.print("]");
        }
    }


    /* ----------  POST  /meetings/add  --------------------------- */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String ctx    = req.getContextPath();

        // Add a new meeting
        if ("add".equals(action)) {
            // 1) Check login
            Integer creatorId = (Integer) req.getSession().getAttribute("userId");
            if (creatorId == null) {
                resp.sendRedirect(ctx + "/login.jsp");
                return;
            }

            // 2) Parse form fields
            String dateStr     = req.getParameter("date");      // yyyy-MM-dd
            String timeStr     = req.getParameter("time");      // HH:mm
            int    duration    = Integer.parseInt(req.getParameter("duration"));
            String title       = req.getParameter("title");
            String description = req.getParameter("description");
            String[] parts     = req.getParameterValues("participants");

            // 3) Build start/end
            LocalDateTime start = LocalDateTime.parse(dateStr + "T" + timeStr);
            LocalDateTime end   = start.plusMinutes(duration);

            // 4) Create appointment
            Appointment appt = new Appointment();
            appt.setCreatorId(creatorId);
            appt.setTitle(title);
            appt.setDescription(description);
            appt.setStartTime(start);
            appt.setEndTime(end);

            int appointmentId;
            try {
                appointmentId = AppointmentDAO.create(appt);
            } catch (SQLException e) {
                throw new ServletException("Error creating appointment", e);
            }

            // 5) Add participants & send invites
            try {
                List<Integer> pids = new ArrayList<>();
                pids.add(creatorId);  // creator is always included

                // add checked participants
                if (parts != null) {
                    for (String pidStr : parts) {
                        int pid = Integer.parseInt(pidStr);
                        if (pid != creatorId) pids.add(pid);
                    }
                }
                // batch insert
                ParticipantDAO.addParticipants(appointmentId, pids);

                // notify each invitee
                String creatorName = UserDAO.getById(creatorId).getUsername();
                String when        = start.toLocalDate().toString();
                for (int pid : pids) {
                    if (pid == creatorId) continue;
                    String msg = creatorName
                               + " invited you to meeting \""
                               + title
                               + "\" on " + when;
                    NotificationDAO.create(pid, msg);
                }
                // mark creator accepted
                ParticipantDAO.update(appointmentId, creatorId, "ACCEPTED", null);

            } catch (SQLException e) {
                throw new ServletException("Error saving participants", e);
            }

            // 6) Redirect to dashboard
            resp.sendRedirect(ctx + "/dashboard.jsp");
            return;
        }

        // Delete an existing meeting
        else if ("delete".equals(action)) {
            // 1) Check login
            Integer userId = (Integer) req.getSession().getAttribute("userId");
            if (userId == null) {
                resp.sendRedirect(ctx + "/login.jsp");
                return;
            }

            // 2) Parse appointmentId
            int apptId = Integer.parseInt(req.getParameter("appointmentId"));

            // 3) Verify user is creator
            int creatorId;
            try {
                creatorId = AppointmentDAO.getCreatorId(apptId);
            } catch (SQLException e) {
                throw new ServletException("Error verifying appointment owner", e);
            }
            if (creatorId != userId) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 4) Perform delete (cascade handles participants)
            try {
                AppointmentDAO.delete(apptId);
            } catch (SQLException e) {
                throw new ServletException("Error deleting appointment", e);
            }

            // 5) Redirect to dashboard
            resp.sendRedirect(ctx + "/dashboard.jsp");
            return;
        }

        // Unknown action
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }



    /* helpers */
    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace("\"", "&quot;");
    }

    private record Meeting(int id, String title, String time, int duration) { }
}
