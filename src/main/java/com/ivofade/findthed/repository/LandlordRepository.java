package com.ivofade.findthed.repository;

import com.ivofade.findthed.model.Landlord;
import com.ivofade.findthed.util.DatabaseConnection;
import com.ivofade.findthed.exception.InvalidInputException;
import java.sql.*;
import java.util.*;

public class LandlordRepository implements RepositoryInterface<Landlord, Integer> {

    private static final String SELECT = "SELECT from p.*, l.other_ontact FROM persons p JOIN landlords l ON l.person_id = p.person_id";

    private Landlord map(Result rs) throws SQLException {
        try {
            // construct the data from tenant that super/calls constructor on person
            return new Landlord(
                rs.getInt("person_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("contact_num"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("other_contact")
            );
        } catch (InvalidInputException e){
            throw new SQLException("Invalid data in a row: " + e.getMessage(), e);
        }
    }

    @Override 
    public void save(Landlord l) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PersonRepository.insertPerson(conn, l);

                try (PreparedStatement ps = conn.preparedStatement(" INSERT INTO landlords (person_id, current_dorm_id) VALUES (?, ?)")) {
                    ps.setInt(1, l.getPersonId());
                    ps.setString(2, l.getOtherContact());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } 
    }

    @Override
    public Optional<Landlord> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            Preparedstatement ps = conn.prepareStatment(SELECT + " WHERE p.person_id = ?")) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
        }
    }

    // mainly for logging in
    public Optional<Landlord> findByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.preparedStatement(SELECT + " WHERE p.email = ?")){

                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
        }
    }

    @Override 
    public List<Landlord> findAll() throws SQLException {
        
        List<Landlord> list = new ArrayList<>();
        try(Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(SELECT)) {
                while (rs.next()) list.add(map(rs));
            }

        return list;
    }

    @Override 
    public void update(Landlord l) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PersonRepository.updatePerson(conn, l);
                try (PreparedStatement ps = conn.preparedStatement(" UPDATE landlords SET other_contact = ? WHERE person_id = ?")){
                    ps.setString(1, l.getOtherContact());
                    ps.setInt(2, l.getPersonId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } 
    }

    @Override 
    public void delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.preparedStatement(" DELETE FROM persons WHERE person_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
    }

}