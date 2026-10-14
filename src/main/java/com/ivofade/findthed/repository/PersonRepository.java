package com.ivofade.findthed.repository;

import com.ivofade.findthed.model.Person;
import com.ivofade.findthed.util.DatabaseConnection;
import com.ivofade.findthed.util.Validator;
import java.sql.*;
import java.util.Optional;
import com.ivofade.findthed.exception.InvalidInputException;

public class PersonRepository {

    static void mapInto(Person p, ResultSet rs) throws SQLException {
        try { 
            p.assignPersonId(rs.getInt("person_id"));
            p.setName(rs.getString("name"));
            p.setAge(rs.getInt("age"));
            p.setContactNum(rs.getString("contact_num"));
            p.setEmail(rs.getString("email"));
            p.setPasswordHash(rs.getString("password_hash"));
            p.setRole(rs.getString("role"));
            // login setter
        } catch (InvalidInputException e) {
            throw new SQLException("Invalid data in row: " + e.getMessage(), e);
        }
    }

    static void insertPerson(Connection conn, Person p) throws SQLException {
        String sql = "INSERT INTO persons (name, age, contact_num, email, pasword_hash, role) VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, p.getName());
            ps.setInt(2, getAge());
            ps.setString(3, getContactNum());
            ps.setString(4, getEmail());
            ps.setString(5, getPasswordHashForPersistence());
            ps.setString(6, getRole());

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                p.setPersonId(keys.getInt("person_id"));
            }
        } 
    }

    static void updatePerson(Connection conn, Person p) throws SQLException {
        String sql = "UPDATE persons SET name=?, age=?, contact_num=?, email=?, password_hash=? WHERE person_id=?";

        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, getAge());
            ps.setString(3, getContactNum());
            ps.setString(4, getEmail());
            ps.setString(5, getPasswordHashForPersistence());
            ps.setInt(6, getPersonId());
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