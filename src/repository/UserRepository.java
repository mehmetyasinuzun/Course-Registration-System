package repository;

import database.DatabaseConnection;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    
    private Connection conn;
    
    public UserRepository() {
        this.conn = DatabaseConnection.getConnection();
    }
    
    // ============ CREATE ============
    
    // Yeni kullanıcı ekle
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, role, student_number, year, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getRole());
            
            // Student ise student_number, year, semester'ı da kaydet
            if (user instanceof Student student) {
                pstmt.setString(6, student.getStudentNumber());
                pstmt.setInt(7, student.getYear());
                pstmt.setInt(8, student.getSemester());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setInt(7, 1);
                pstmt.setInt(8, 1);
            }
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Kullanıcı ekleme hatası: " + e.getMessage());
        }
        return -1;
    }
    
    // ============ READ ============
    
    // ID ile kullanıcı bul
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToUser(rs);
            }
        } catch (SQLException e) {
            System.out.println("Kullanıcı arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Username ile kullanıcı bul
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToUser(rs);
            }
        } catch (SQLException e) {
            System.out.println("Kullanıcı arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Tüm kullanıcıları getir
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapToUser(rs));
            }
        } catch (SQLException e) {
            System.out.println("Kullanıcı listeleme hatası: " + e.getMessage());
        }
        return users;
    }
    
    // Role göre kullanıcıları getir
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(mapToUser(rs));
            }
        } catch (SQLException e) {
            System.out.println("Kullanıcı listeleme hatası: " + e.getMessage());
        }
        return users;
    }
    
    // ============ UPDATE ============
    
    // Kullanıcı güncelle
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, email = ?, student_number = ?, year = ?, semester = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            
            if (user instanceof Student student) {
                pstmt.setString(5, student.getStudentNumber());
                pstmt.setInt(6, student.getYear());
                pstmt.setInt(7, student.getSemester());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setInt(6, 1);
                pstmt.setInt(7, 1);
            }
            
            pstmt.setInt(8, user.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kullanıcı güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ DELETE ============
    
    // Kullanıcı sil
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kullanıcı silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ HELPER ============
    
    // ResultSet'i User nesnesine dönüştür
    private User mapToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;
        
        switch (role) {
            case "STUDENT":
                Student student = new Student();
                student.setStudentNumber(rs.getString("student_number"));
                student.setYear(rs.getInt("year"));
                student.setSemester(rs.getInt("semester"));
                user = student;
                break;
            case "INSTRUCTOR":
                user = new Instructor();
                break;
            case "ADMIN":
                user = new Admin();
                break;
            default:
                user = new User();
        }
        
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);
        
        return user;
    }
}
