package com.example.dao;

import com.example.model.Participant;
import com.example.utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantDAO {

    /**
     * Add a set of participants (all initially INVITED) for an appointment.
     */
    public static void addParticipants(int appointmentId, List<Integer> userIds) throws SQLException {
        String sql = "INSERT INTO appointment_participants (appointment_id, user_id) VALUES (?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int uid : userIds) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, uid);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Fetch all Participant records for a given appointment.
     */
    public static List<Participant> getByAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM appointment_participants WHERE appointment_id = ?";
        List<Participant> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participant p = new Participant();
                p.setAppointmentId(rs.getInt("appointment_id"));
                p.setUserId(rs.getInt("user_id"));
                p.setStatus(rs.getString("status"));
                p.setComment(rs.getString("comment"));
                list.add(p);
            }
        }
        return list;
    }

    /**
     * Update the RSVP status and comment for a single participant.
     */
    public static void update(int appointmentId, int userId, String status, String comment)
            throws SQLException {
        String sql = "UPDATE appointment_participants SET status = ?, comment = ? WHERE appointment_id = ? AND user_id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, comment);
            ps.setInt(3, appointmentId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Convenience method to get just the list of user IDs for a given appointment.
     */
    public static List<Integer> getUserIds(int appointmentId) throws SQLException {
        String sql = "SELECT user_id FROM appointment_participants WHERE appointment_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("user_id"));
            }
        }
        return ids;
    }
}
