package repository;

import database.DatabaseConnection;
import model.CourseCatalog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseCatalogRepository {
    
    private Connection conn;
    
    public CourseCatalogRepository() {
        this.conn = DatabaseConnection.getConnection();
    }
    
    // Yeni ders kataloğu ekle
    public int insert(CourseCatalog catalog) {
        String sql = "INSERT INTO course_catalog (code, name, credits, semester) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, catalog.getCode());
            pstmt.setString(2, catalog.getName());
            pstmt.setInt(3, catalog.getCredits());
            pstmt.setInt(4, catalog.getSemester());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Ders kataloğu ekleme hatası: " + e.getMessage());
        }
        return -1;
    }
    
    // ID ile bul
    public CourseCatalog findById(int id) {
        String sql = "SELECT * FROM course_catalog WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToCatalog(rs);
            }
        } catch (SQLException e) {
            System.out.println("Ders kataloğu arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Kod ile bul
    public CourseCatalog findByCode(String code) {
        String sql = "SELECT * FROM course_catalog WHERE code = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToCatalog(rs);
            }
        } catch (SQLException e) {
            System.out.println("Ders kataloğu arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Tümünü getir (FIFO - ID sırasına göre, yani ekleme sırasına göre)
    public List<CourseCatalog> findAll() {
        List<CourseCatalog> catalogs = new ArrayList<>();
        String sql = "SELECT * FROM course_catalog ORDER BY id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                catalogs.add(mapToCatalog(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ders kataloğu listeleme hatası: " + e.getMessage());
        }
        return catalogs;
    }
    
    // Güncelle
    public boolean update(CourseCatalog catalog) {
        String sql = "UPDATE course_catalog SET code = ?, name = ?, credits = ?, semester = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, catalog.getCode());
            pstmt.setString(2, catalog.getName());
            pstmt.setInt(3, catalog.getCredits());
            pstmt.setInt(4, catalog.getSemester());
            pstmt.setInt(5, catalog.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ders kataloğu güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Sil
    public boolean delete(int id) {
        String sql = "DELETE FROM course_catalog WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ders kataloğu silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ResultSet -> CourseCatalog
    private CourseCatalog mapToCatalog(ResultSet rs) throws SQLException {
        CourseCatalog catalog = new CourseCatalog();
        catalog.setId(rs.getInt("id"));
        catalog.setCode(rs.getString("code"));
        catalog.setName(rs.getString("name"));
        catalog.setCredits(rs.getInt("credits"));
        catalog.setSemester(rs.getInt("semester"));
        return catalog;
    }
}
