package com.example.dao;

import com.example.model.Appointment;
import com.example.utils.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for appointments.
 */
public class AppointmentDAO {

    /**
     * Creates a new appointment record.
     * @param appt the Appointment to insert (id is ignored)
     * @return the generated appointment ID
     * @throws SQLException on DB error
     */
    public static int create(Appointment appt) throws SQLException {
        String sql = "INSERT INTO appointments (creator_id, title, description, start_time, end_time) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appt.getCreatorId());
            ps.setString(2, appt.getTitle());
            ps.setString(3, appt.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(appt.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(appt.getEndTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Creating appointment failed, no ID obtained.");
    }

    /**
     * Retrieves a single appointment by its ID.
     */
    public static Appointment getById(int id) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment a = new Appointment();
                    a.setId(id);
                    a.setCreatorId(rs.getInt("creator_id"));
                    a.setTitle(rs.getString("title"));
                    a.setDescription(rs.getString("description"));
                    a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    return a;
                }
            }
        }
        return null;
    }

    /**
     * Returns all appointments created by a given user.
     */
    public static List<Appointment> getByCreator(int creatorId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE creator_id = ? ORDER BY start_time";
        List<Appointment> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment a = new Appointment();
                    a.setId(rs.getInt("id"));
                    a.setCreatorId(creatorId);
                    a.setTitle(rs.getString("title"));
                    a.setDescription(rs.getString("description"));
                    a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                    a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                    list.add(a);
                }
            }
        }
        return list;
    }

    /**
     * Updates an existing appointment.
     */
    public static void update(Appointment appt) throws SQLException {
        String sql = "UPDATE appointments " +
                     "SET title = ?, description = ?, start_time = ?, end_time = ? " +
                     "WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, appt.getTitle());
            ps.setString(2, appt.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(appt.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(appt.getEndTime()));
            ps.setInt(5, appt.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an appointment (participants will cascade if FK is set).
     */
    public static void delete(int id) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Convenience: get the creator’s user ID for an appointment.
     */
    public static int getCreatorId(int appointmentId) throws SQLException {
        String sql = "SELECT creator_id FROM appointments WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("creator_id");
                }
            }
        }
        throw new SQLException("Appointment not found: " + appointmentId);
    }
}
