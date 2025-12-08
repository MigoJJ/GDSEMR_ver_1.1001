package com.emr.gds.main.medication.db;

import com.emr.gds.main.medication.model.MedicationGroup;
import com.emr.gds.main.medication.model.MedicationItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DEFAULT_DB_FILENAME = "med_data.db";
    private final String dbFileName;
    
    private boolean pendingChanges = false;
    private Map<String, List<MedicationGroup>> cachedData = null;
    private List<String> cachedCategories = null;

    public DatabaseManager() {
        this(DEFAULT_DB_FILENAME);
    }

    public DatabaseManager(String dbFileName) {
        this.dbFileName = dbFileName;
        initializeDatabase();
    }

    private Path getDbPath() {
        // Logic to find the app/db directory relative to project root
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew"))) {
            p = p.getParent();
        }
        
        if (p != null) {
            Path appDb = p.resolve("app/db").resolve(dbFileName);
            if (Files.exists(appDb)) return appDb;
            
            Path localDb = p.resolve("db").resolve(dbFileName);
            if (Files.exists(localDb)) return localDb;

            return appDb; // Default to app/db
        }
        return Paths.get("app/db").resolve(dbFileName);
    }
    
    private String getConnectionString() {
        return "jdbc:sqlite:" + getDbPath().toAbsolutePath().toString();
    }

    private void initializeDatabase() {
        String url = getConnectionString();
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    // Create tables
                    stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                 "name TEXT UNIQUE, " +
                                 "display_order INTEGER)");

                    stmt.execute("CREATE TABLE IF NOT EXISTS medication_groups (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                 "category_id INTEGER, " +
                                 "title TEXT, " +
                                 "display_order INTEGER, " +
                                 "FOREIGN KEY(category_id) REFERENCES categories(id))");

                    stmt.execute("CREATE TABLE IF NOT EXISTS medication_items (" +
                                 "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                 "group_id INTEGER, " +
                                 "text TEXT, " +
                                 "display_order INTEGER, " +
                                 "FOREIGN KEY(group_id) REFERENCES medication_groups(id))");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to initialize database at " + url + ": " + e.getMessage());
        }
    }

    public void createTables() {
        initializeDatabase();
    }

    public void ensureSeedData() {
        // No longer seeding from XML. Data is expected to be in DB.
        if (cachedData == null) {
            loadData();
        }
    }

    public List<String> getOrderedCategories() {
        if (cachedCategories == null) {
            loadData();
        }
        return cachedCategories;
    }

    public Map<String, List<MedicationGroup>> getMedicationData() {
        if (cachedData == null) {
            loadData();
        }
        return cachedData;
    }

    private void loadData() {
        cachedData = new LinkedHashMap<>();
        cachedCategories = new ArrayList<>();
        String url = getConnectionString();

        try (Connection conn = DriverManager.getConnection(url)) {
            // Load Categories
            Map<Integer, String> catIdMap = new LinkedHashMap<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM categories ORDER BY display_order")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    cachedCategories.add(name);
                    catIdMap.put(id, name);
                }
            }

            // Load Groups and Items
            for (Map.Entry<Integer, String> catEntry : catIdMap.entrySet()) {
                int catId = catEntry.getKey();
                String catName = catEntry.getValue();
                List<MedicationGroup> groups = new ArrayList<>();

                String groupSql = "SELECT id, title FROM medication_groups WHERE category_id = ? ORDER BY display_order";
                try (PreparedStatement pstmt = conn.prepareStatement(groupSql)) {
                    pstmt.setInt(1, catId);
                    try (ResultSet rsGroup = pstmt.executeQuery()) {
                        while (rsGroup.next()) {
                            int groupId = rsGroup.getInt("id");
                            String groupTitle = rsGroup.getString("title");
                            List<MedicationItem> items = new ArrayList<>();

                            String itemSql = "SELECT text FROM medication_items WHERE group_id = ? ORDER BY display_order";
                            try (PreparedStatement pstmtItem = conn.prepareStatement(itemSql)) {
                                pstmtItem.setInt(1, groupId);
                                try (ResultSet rsItem = pstmtItem.executeQuery()) {
                                    while (rsItem.next()) {
                                        items.add(new MedicationItem(rsItem.getString("text")));
                                    }
                                }
                            }
                            groups.add(new MedicationGroup(groupTitle, items));
                        }
                    }
                }
                cachedData.put(catName, groups);
            }

        } catch (SQLException e) {
            LOGGER.severe("Failed to load from DB (" + url + "): " + e.getMessage());
        }
    }

    public boolean hasPendingChanges() {
        return pendingChanges;
    }

    public void markDirty() {
        this.pendingChanges = true;
    }

    public void commitPending() {
        if (cachedCategories == null || cachedData == null) return;
        saveToDb();
        pendingChanges = false;
        LOGGER.info("Changes saved to database.");
    }

    private void saveToDb() {
        String url = getConnectionString();
        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // Clear existing data (Full rewrite strategy)
                stmt.executeUpdate("DELETE FROM medication_items");
                stmt.executeUpdate("DELETE FROM medication_groups");
                stmt.executeUpdate("DELETE FROM categories");
                
                String insertCat = "INSERT INTO categories (name, display_order) VALUES (?, ?)";
                String insertGroup = "INSERT INTO medication_groups (category_id, title, display_order) VALUES (?, ?, ?)";
                String insertItem = "INSERT INTO medication_items (group_id, text, display_order) VALUES (?, ?, ?)";

                try (PreparedStatement pstmtCat = conn.prepareStatement(insertCat, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement pstmtGroup = conn.prepareStatement(insertGroup, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement pstmtItem = conn.prepareStatement(insertItem)) {
                    
                    int catOrder = 0;
                    for (String catName : cachedCategories) {
                        pstmtCat.setString(1, catName);
                        pstmtCat.setInt(2, catOrder++);
                        pstmtCat.executeUpdate();
                        
                        try (ResultSet rsCat = pstmtCat.getGeneratedKeys()) {
                            if (rsCat.next()) {
                                int catId = rsCat.getInt(1);
                                List<MedicationGroup> groups = cachedData.get(catName);
                                if (groups != null) {
                                    int groupOrder = 0;
                                    for (MedicationGroup group : groups) {
                                        pstmtGroup.setInt(1, catId);
                                        pstmtGroup.setString(2, group.title());
                                        pstmtGroup.setInt(3, groupOrder++);
                                        pstmtGroup.executeUpdate();
                                        
                                        try (ResultSet rsGroup = pstmtGroup.getGeneratedKeys()) {
                                            if (rsGroup.next()) {
                                                int groupId = rsGroup.getInt(1);
                                                int itemOrder = 0;
                                                for (MedicationItem item : group.medications()) {
                                                    pstmtItem.setInt(1, groupId);
                                                    pstmtItem.setString(2, item.getText());
                                                    pstmtItem.setInt(3, itemOrder++);
                                                    pstmtItem.addBatch();
                                                }
                                                pstmtItem.executeBatch();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to save to DB: " + e.getMessage());
        }
    }

    public void removeItem(MedicationItem item) {
        if (cachedData == null) return;
        for (List<MedicationGroup> groups : cachedData.values()) {
            for (MedicationGroup group : groups) {
                if (group.medications().remove(item)) {
                    markDirty();
                    return;
                }
            }
        }
    }

    public void addItem(String category, String groupName, MedicationItem item) {
        if (cachedData == null) return;
        List<MedicationGroup> groups = cachedData.get(category);
        if (groups != null) {
            for (MedicationGroup group : groups) {
                if (group.title().equals(groupName)) {
                    group.medications().add(item);
                    markDirty();
                    return;
                }
            }
        }
    }

    public void addCategory(String categoryName) {
        if (cachedCategories == null) cachedCategories = new ArrayList<>();
        if (!cachedCategories.contains(categoryName)) {
            cachedCategories.add(categoryName);
            if (cachedData == null) cachedData = new LinkedHashMap<>();
            cachedData.put(categoryName, new ArrayList<>());
            markDirty();
        }
    }

    public void addGroup(String categoryName, String groupName) {
        if (cachedData == null) return;
        List<MedicationGroup> groups = cachedData.get(categoryName);
        if (groups != null) {
            // Check if exists
            for (MedicationGroup g : groups) {
                if (g.title().equals(groupName)) return;
            }
            groups.add(new MedicationGroup(groupName, new ArrayList<>()));
            markDirty();
        }
    }
}
