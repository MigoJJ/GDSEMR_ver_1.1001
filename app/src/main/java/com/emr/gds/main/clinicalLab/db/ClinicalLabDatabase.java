package com.emr.gds.main.clinicalLab.db;

import com.emr.gds.main.clinicalLab.model.ClinicalLabItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class ClinicalLabDatabase {
    
    private String getDbUrl() {
        String[] possiblePaths = {
            "app/db/ClinicalLabItemsSqlite3.db",
            "db/ClinicalLabItemsSqlite3.db",
            "../app/db/ClinicalLabItemsSqlite3.db"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                // System.out.println("ClinicalLabDatabase: DB found at " + file.getAbsolutePath());
                return "jdbc:sqlite:" + file.getAbsolutePath();
            }
        }
        System.err.println("ClinicalLabDatabase: DB file not found! Defaulting to app/db/...");
        return "jdbc:sqlite:app/db/ClinicalLabItemsSqlite3.db";
    }

    public List<ClinicalLabItem> getAllItems() {
        List<ClinicalLabItem> items = new ArrayList<>();
        String sql = "SELECT * FROM clinical_lab_items";

        try (Connection conn = DriverManager.getConnection(getDbUrl());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching lab items: " + e.getMessage());
        }
        return items;
    }

    public List<ClinicalLabItem> searchItems(String query) {
        List<ClinicalLabItem> items = new ArrayList<>();
        String sql = """
            SELECT *
            FROM clinical_lab_items
            WHERE COALESCE(LOWER(test_name), '') LIKE ?
               OR COALESCE(LOWER(category), '') LIKE ?
               OR COALESCE(LOWER(unit), '') LIKE ?
               OR COALESCE(LOWER(male_reference_range), '') LIKE ?
               OR COALESCE(LOWER(female_reference_range), '') LIKE ?
               OR COALESCE(LOWER(codes), '') LIKE ?
               OR COALESCE(LOWER(comments), '') LIKE ?
               OR COALESCE(CAST(male_range_low AS TEXT), '') LIKE ?
               OR COALESCE(CAST(male_range_high AS TEXT), '') LIKE ?
               OR COALESCE(CAST(female_range_low AS TEXT), '') LIKE ?
               OR COALESCE(CAST(female_range_high AS TEXT), '') LIKE ?
            """;

        try (Connection conn = DriverManager.getConnection(getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String param = "%" + query.toLowerCase() + "%";
            for (int i = 1; i <= 11; i++) {
                pstmt.setString(i, param);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching lab items: " + e.getMessage());
        }
        return items;
    }

    public void updateItem(ClinicalLabItem item) {
        String sql = "UPDATE clinical_lab_items SET category = ?, test_name = ?, unit = ?, male_range_low = ?, male_range_high = ?, female_range_low = ?, female_range_high = ?, male_reference_range = ?, female_reference_range = ?, codes = ?, comments = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getCategory());
            pstmt.setString(2, item.getTestName());
            pstmt.setString(3, item.getUnit());
            pstmt.setObject(4, item.getMaleRangeLow());
            pstmt.setObject(5, item.getMaleRangeHigh());
            pstmt.setObject(6, item.getFemaleRangeLow());
            pstmt.setObject(7, item.getFemaleRangeHigh());
            pstmt.setString(8, item.getMaleReferenceRange());
            pstmt.setString(9, item.getFemaleReferenceRange());
            pstmt.setString(10, item.getCodes());
            pstmt.setString(11, item.getComments());
            pstmt.setInt(12, item.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating lab item: " + e.getMessage());
        }
    }

    public void insertItem(ClinicalLabItem item) {
        String sql = "INSERT INTO clinical_lab_items (category, test_name, unit, male_range_low, male_range_high, female_range_low, female_range_high, male_reference_range, female_reference_range, codes, comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, item.getCategory());
            pstmt.setString(2, item.getTestName());
            pstmt.setString(3, item.getUnit());
            pstmt.setObject(4, item.getMaleRangeLow());
            pstmt.setObject(5, item.getMaleRangeHigh());
            pstmt.setObject(6, item.getFemaleRangeLow());
            pstmt.setObject(7, item.getFemaleRangeHigh());
            pstmt.setString(8, item.getMaleReferenceRange());
            pstmt.setString(9, item.getFemaleReferenceRange());
            pstmt.setString(10, item.getCodes());
            pstmt.setString(11, item.getComments());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1)); // Set the generated ID back to the item
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting lab item: " + e.getMessage());
        }
    }

    public void deleteItem(int id) {
        String sql = "DELETE FROM clinical_lab_items WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(getDbUrl());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting lab item: " + e.getMessage());
        }
    }

    private ClinicalLabItem mapResultSetToItem(ResultSet rs) throws SQLException {
        return new ClinicalLabItem(
            rs.getInt("id"),
            rs.getString("category"),
            rs.getString("test_name"),
            rs.getString("unit"),
            getObjectOrNull(rs, "male_range_low"),
            getObjectOrNull(rs, "male_range_high"),
            getObjectOrNull(rs, "female_range_low"),
            getObjectOrNull(rs, "female_range_high"),
            rs.getString("male_reference_range"),
            rs.getString("female_reference_range"),
            rs.getString("codes"),
            rs.getString("comments")
        );
    }

    private Double getObjectOrNull(ResultSet rs, String column) throws SQLException {
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : val;
    }
}
