package com.ivofade.findthed.repository;

import com.ivofade.findthed.model.Tenant;
import com.ivofade.findthed.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class TenantRepository implements RepositoryInterface<Tenant, Integer> {
    
    private static final String SELECT = "SELECT from p.*, t.current_dorm_id FROM persons p JOIN tenants t ON t.current_dorm_id = p.person_id";

    
}
