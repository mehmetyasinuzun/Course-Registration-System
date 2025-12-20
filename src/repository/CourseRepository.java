package repository;

import database.DatabaseConnection;
import model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {
    
    private Connection conn;
    
    public CourseRepository() {
        this.conn = DatabaseConnection.getConnection();
    }
    
    // ============ CREATE ============
    
    public int insert(Course course) {
        String sql = "INSERT INTO courses (code, name, credits, capacity, instructor_id, day_of_week, start_time, end_time, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getName());
            pstmt.setInt(3, course.getCredits());
            pstmt.setInt(4, course.getCapacity());
            pstmt.setInt(5, course.getInstructorId());
            pstmt.setString(6, course.getDayOfWeek());
            pstmt.setString(7, course.getStartTime());
            pstmt.setString(8, course.getEndTime());
            pstmt.setInt(9, course.getSemester());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Ders ekleme hatası: " + e.getMessage());
        }
        return -1;
    }
    
    // Ön koşul ekle
    public boolean addPrerequisite(int courseId, int prerequisiteId) {
        String sql = "INSERT INTO prerequisites (course_id, prerequisite_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, prerequisiteId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ön koşul ekleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ READ ============
    
    public Course findById(int id) {
        String sql = "SELECT * FROM courses WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToCourse(rs);
            }
        } catch (SQLException e) {
            System.out.println("Ders arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    public Course findByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToCourse(rs);
            }
        } catch (SQLException e) {
            System.out.println("Ders arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapToCourse(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ders listeleme hatası: " + e.getMessage());
        }
        return courses;
    }
    
    // Hocanın derslerini getir
    public List<Course> findByInstructorId(int instructorId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE instructor_id = ? ORDER BY code";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(mapToCourse(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ders listeleme hatası: " + e.getMessage());
        }
        return courses;
    }
    
    // Ön koşul derslerini getir
    public List<Course> getPrerequisites(int courseId) {
        List<Course> prerequisites = new ArrayList<>();
        String sql = "SELECT c.* FROM courses c INNER JOIN prerequisites p ON c.id = p.prerequisite_id WHERE p.course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                prerequisites.add(mapToCourse(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ön koşul listeleme hatası: " + e.getMessage());
        }
        return prerequisites;
    }
    
    // Derse kayıtlı öğrenci sayısı
    public int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE course_id = ? AND status = 'APPROVED'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Kayıt sayısı hatası: " + e.getMessage());
        }
        return 0;
    }
    
    // ============ UPDATE ============
    
    public boolean update(Course course) {
        String sql = "UPDATE courses SET code = ?, name = ?, credits = ?, capacity = ?, instructor_id = ?, day_of_week = ?, start_time = ?, end_time = ?, semester = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getName());
            pstmt.setInt(3, course.getCredits());
            pstmt.setInt(4, course.getCapacity());
            pstmt.setInt(5, course.getInstructorId());
            pstmt.setString(6, course.getDayOfWeek());
            pstmt.setString(7, course.getStartTime());
            pstmt.setString(8, course.getEndTime());
            pstmt.setInt(9, course.getSemester());
            pstmt.setInt(10, course.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ders güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ DELETE ============
    
    public boolean delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ders silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Ön koşul sil
    public boolean removePrerequisite(int courseId, int prerequisiteId) {
        String sql = "DELETE FROM prerequisites WHERE course_id = ? AND prerequisite_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, prerequisiteId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Ön koşul silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ HELPER ============
    
    private Course mapToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setCode(rs.getString("code"));
        course.setName(rs.getString("name"));
        course.setCredits(rs.getInt("credits"));
        course.setCapacity(rs.getInt("capacity"));
        course.setInstructorId(rs.getInt("instructor_id"));
        course.setDayOfWeek(rs.getString("day_of_week"));
        course.setStartTime(rs.getString("start_time"));
        course.setEndTime(rs.getString("end_time"));
        course.setSemester(rs.getInt("semester"));
        return course;
    }
    
    // Dönem bazlı ders listele
    public List<Course> findBySemester(int semester) {
        List<Course> courses = new ArrayList<>();
        // semester=0 ise tüm dersleri getir, değilse o döneme veya genel derslere (semester=0) bak
        String sql = semester == 0 
            ? "SELECT * FROM courses ORDER BY code"
            : "SELECT * FROM courses WHERE semester = ? OR semester = 0 ORDER BY code";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (semester != 0) {
                pstmt.setInt(1, semester);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courses.add(mapToCourse(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ders listeleme hatası: " + e.getMessage());
        }
        return courses;
    }
}
