package com.ivofade.findthed.repository;

import com.ivofade.findthed.model.Tenant;
import com.ivofade.findthed.util.DatabaseConnection;
import com.ivofade.findthed.exception.InvalidInputException;
import java.sql.*;
import java.util.*;

public class TenantRepository implements RepositoryInterface<Tenant, Integer> {
    
    private static final String SELECT = "SELECT from p.*, t.current_dorm_id FROM persons p JOIN tenants t ON t.person_id = p.person_id";

    //returns tenant data from db for login mainly
    private Tenant map(ResultSet rs) throws SQLException {
        try {
            int dorm = rs.getInt("current_dorm_id");
            Integer currentDorm = rs.wasNull() ? null : dorm;

            // construct the data from tenant that super/calls constructor on person
            return new Tenant(
                rs.getInt("person_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("contact_num"),
                rs.getString("email"),
                rs.getString("password_hash"),
                currentDorm
            );
        } catch (InvalidInputException e){
            throw new SQLException("Invalid data in a row: " + e.getMessage(), e);
        }
    }

    @Override 
    public void save(Tenant t) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PersonRepository.insertPerson(conn, t);

                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tenants (person_id, current_dorm_id) VALUES (?, ?)")) {
                    ps.setInt(1, t.getPersonId());
                    setNullableInt(ps, 2, t.getCurrentDormId());
                    ps.executeUpdate();
                }
                conn.commit();
            }catch (SQLException e) {
                conn.rollback();
                throw e;
            } 
        } 
    }

    @Override
    public Optional<Tenant> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            Preparedstatement ps = conn.prepareStatment(SELECT + "WHERE p.person_id = ?")) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
        }
    }

    // mainly for logging in
    public Optional<Tenant> findByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE p.email = ?")){

                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
        }
    }

    @Override 
    public List<Tenant> findAll() throws SQLException {
        
        List<Tenant> list = new ArrayList<>();
        try(Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(SELECT)) {
                while (rs.next()) list.add(map(rs));
            }

        return list;
    }

    public List<Tenant> findByCurrentDorm(int dormId) throws SQLException {

        List<Tenant> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE t.current_dorm_id = ?")) {
                ps.setInt(1, dormId);

                try(ResultSet rs = ps.executeQuery()){
                    while(rs.next()) list.add(map(rs));
                }
            }

        return list;
    }
    
    @Override 
    public void update(Tenant t) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PersonRepository.updatePerson(conn, t);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE tenants SET current_dorm_id = ? WHERE person_id = ?")){
                    setNullableInt(ps, 1, t.getCurrentDormId());
                    ps.setInt(2, t.getPersonId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } 
    }

    // pass null to clear curr dorm id
    public void setCurrentDormId(int tenantId, Integer dormId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(SELECT + "UPDATE tenants SET current_dorm_id = ? WHERE t.tenant_id = ?")) {
                setNullableInt(ps, 1, dormId);
                ps.setInt(2, tenantId);
                ps.executeUpdate();
            }
    }

    @Override 
    public void delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM persons WHERE person_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
    }



    private static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, v);
    }
}
