package com.ivofade.findthed.repository;

import com.ivofade.findthed.model.Person;
import com.ivofade.findthed.util.DatabaseConnection;
import java.sql.*;
import java.util.Optional;

public class PersonRepository {

    static void insertPerson(Connection conn, Person p) throws SQLException {
        String sql = "INSERT INTO persons (name, age, contact_num, email, pasword_hash, role) VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = conn.prepareStatement(sql, new String[] { "person_id" })){
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getContactNum());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getPasswordHashForPersistence());
            ps.setString(6, p.getRole().name());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if(keys.next()){
                    p.assignPersonId(keys.getInt(1));
                } else {
                    throw new SQLException("Insert succeeded but no ID was returned.");
                }
            }
        } 
    }

    static void updatePerson(Connection conn, Person p) throws SQLException {
        String sql = "UPDATE persons SET name=?, age=?, contact_num=?, email=?, password_hash=? WHERE person_id=?";

        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getContactNum());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getPasswordHashForPersistence());
            ps.setInt(6, p.getPersonId());
            ps.executeUpdate();

        }
    }

    public Optional<String> findRoleByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT role FROM persons WHERE email = ?")) {

                ps.setString(1, email.toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getString("role")) : Optional.empty();
                }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        return findRoleByEmail(email).isPresent();
    }

    public void setLoggedIn(int personId, boolean loggedIn) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("UPDATE persons SET is_logged_in=? WHERE persons_id=?")) {
                ps.setBoolean(1, loggedIn);
                ps.setInt(2, personId);
                ps.executeUpdate();
        }
    }
}