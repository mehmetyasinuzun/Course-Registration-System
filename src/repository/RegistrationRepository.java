package repository;

import database.DatabaseConnection;
import model.Registration;
import model.TranscriptEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRepository {
    
    private Connection conn;
    
    public RegistrationRepository() {
        this.conn = DatabaseConnection.getConnection();
    }
    
    // ============ CREATE ============
    
    public int insert(Registration reg) {
        String sql = "INSERT INTO registrations (student_id, course_id, status, request_date) VALUES (?, ?, ?, datetime('now'))";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, reg.getStudentId());
            pstmt.setInt(2, reg.getCourseId());
            pstmt.setString(3, reg.getStatus());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Kayıt ekleme hatası: " + e.getMessage());
        }
        return -1;
    }
    
    // Tamamlanmış ders ekle
    public boolean addCompletedCourse(int studentId, int courseId) {
        String sql = "INSERT INTO completed_courses (student_id, course_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Tamamlanmış ders ekleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Tamamlanmış ders ekle (catalog course code ile)
    public boolean addCompletedCourseByCode(int studentId, String courseCode) {
        // Önce course catalog'dan course kodunu bul, sonra course_catalog id'sini al
        String sql = """
            INSERT INTO completed_courses (student_id, course_id)
            SELECT ?, id FROM course_catalog WHERE code = ?
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, courseCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Duplicate entry hatası görmezden gel (aynı ders zaten eklenmiş olabilir)
            if (!e.getMessage().contains("UNIQUE")) {
                System.out.println("Tamamlanmış ders ekleme hatası: " + e.getMessage());
            }
        }
        return false;
    }
    
    // ============ READ ============
    
    public Registration findById(int id) {
        String sql = "SELECT * FROM registrations WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToRegistration(rs);
            }
        } catch (SQLException e) {
            System.out.println("Kayıt arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Öğrenci + Ders için kayıt var mı?
    public Registration findByStudentAndCourse(int studentId, int courseId) {
        String sql = "SELECT * FROM registrations WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToRegistration(rs);
            }
        } catch (SQLException e) {
            System.out.println("Kayıt arama hatası: " + e.getMessage());
        }
        return null;
    }
    
    // Tüm bekleyen kayıtlar
    public List<Registration> findPending() {
        List<Registration> regs = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE status = 'PENDING' ORDER BY request_date";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                regs.add(mapToRegistration(rs));
            }
        } catch (SQLException e) {
            System.out.println("Bekleyen kayıt listeleme hatası: " + e.getMessage());
        }
        return regs;
    }
    
    // Hocanın derslerine ait bekleyen kayıtlar
    public List<Registration> findPendingByInstructorId(int instructorId) {
        List<Registration> regs = new ArrayList<>();
        String sql = """
            SELECT r.* FROM registrations r 
            INNER JOIN courses c ON r.course_id = c.id 
            WHERE r.status = 'PENDING' AND c.instructor_id = ?
            ORDER BY r.request_date
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                regs.add(mapToRegistration(rs));
            }
        } catch (SQLException e) {
            System.out.println("Bekleyen kayıt listeleme hatası: " + e.getMessage());
        }
        return regs;
    }
    
    // Öğrencinin kayıtları
    public List<Registration> findByStudentId(int studentId) {
        List<Registration> regs = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE student_id = ? ORDER BY request_date DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                regs.add(mapToRegistration(rs));
            }
        } catch (SQLException e) {
            System.out.println("Öğrenci kayıtları listeleme hatası: " + e.getMessage());
        }
        return regs;
    }
    
    // Öğrencinin onaylı kayıtları
    public List<Registration> findApprovedByStudentId(int studentId) {
        List<Registration> regs = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE student_id = ? AND status = 'APPROVED'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                regs.add(mapToRegistration(rs));
            }
        } catch (SQLException e) {
            System.out.println("Onaylı kayıt listeleme hatası: " + e.getMessage());
        }
        return regs;
    }
    
    // Dersin kayıtlı öğrencileri
    public List<Registration> findByCourseId(int courseId) {
        List<Registration> regs = new ArrayList<>();
        String sql = "SELECT * FROM registrations WHERE course_id = ? AND status = 'APPROVED'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                regs.add(mapToRegistration(rs));
            }
        } catch (SQLException e) {
            System.out.println("Ders kayıtları listeleme hatası: " + e.getMessage());
        }
        return regs;
    }
    
    // Öğrenci bu dersi tamamlamış mı?
    public boolean hasCompletedCourse(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM completed_courses WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Tamamlanmış ders kontrol hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Öğrencinin transkriptini getir (tamamlanan dersler)
    public List<TranscriptEntry> getTranscript(int studentId) {
        List<TranscriptEntry> transcript = new ArrayList<>();
        String sql = """
            SELECT cc.student_id, cat.code, cat.name, cat.credits, cat.semester, 
                   COALESCE(cc.grade, 'CC') as grade,
                   COALESCE(cc.entry_type, 'REGULAR') as entry_type
            FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ?
            ORDER BY cat.semester, cat.code
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TranscriptEntry entry = new TranscriptEntry(
                    rs.getInt("student_id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("semester"),
                    rs.getString("grade"),
                    rs.getString("entry_type")
                );
                transcript.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("Transkript getirme hatası: " + e.getMessage());
        }
        return transcript;
    }
    
    // Öğrencinin tamamlanan ders sayısı
    public int getCompletedCourseCount(int studentId) {
        String sql = "SELECT COUNT(*) FROM completed_courses WHERE student_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Tamamlanan ders sayısı alma hatası: " + e.getMessage());
        }
        return 0;
    }
    
    // Öğrencinin tamamlanan toplam kredi
    public int getCompletedCredits(int studentId) {
        String sql = """
            SELECT COALESCE(SUM(cat.credits), 0) as total_credits
            FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ?
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_credits");
            }
        } catch (SQLException e) {
            System.out.println("Tamamlanan kredi hesaplama hatası: " + e.getMessage());
        }
        return 0;
    }
    
    // Öğrencinin GPA'sını hesapla (CGPA - Cumulative GPA)
    public double calculateGPA(int studentId) {
        List<TranscriptEntry> transcript = getTranscript(studentId);
        if (transcript.isEmpty()) return 0.0;
        
        double totalPoints = 0.0;
        int totalCredits = 0;
        
        for (TranscriptEntry entry : transcript) {
            // GPA hesaplamasında TÜM dersler dahil edilir (başarısız dersler dahil)
            totalPoints += entry.getGradePoint() * entry.getCredits();
            totalCredits += entry.getCredits();
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    
    // Tamamlanmış dersi güncelle (not ve entry type)
    public boolean updateCompletedCourse(int studentId, int courseId, String grade, String entryType) {
        String sql = "UPDATE completed_courses SET grade = ?, entry_type = ? WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, grade);
            pstmt.setString(2, entryType);
            pstmt.setInt(3, studentId);
            pstmt.setInt(4, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Tamamlanmış ders güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Tamamlanmış dersi sil
    public boolean deleteCompletedCourse(int studentId, int courseId) {
        String sql = "DELETE FROM completed_courses WHERE student_id = ? AND course_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Tamamlanmış ders silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Catalog course code ile tamamlanmış ders ekle (entry type ile)
    public boolean addCompletedCourseWithGrade(int studentId, String courseCode, String grade, String entryType) {
        String sql = """
            INSERT INTO completed_courses (student_id, course_id, grade, entry_type)
            SELECT ?, id, ?, ? FROM course_catalog WHERE code = ?
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, grade);
            pstmt.setString(3, entryType);
            pstmt.setString(4, courseCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (!e.getMessage().contains("UNIQUE")) {
                System.out.println("Tamamlanmış ders ekleme hatası: " + e.getMessage());
            }
        }
        return false;
    }
    
    // ============ UPDATE ============
    
    // Kayıt onayla
    public boolean approve(int id) {
        String sql = "UPDATE registrations SET status = 'APPROVED', response_date = datetime('now') WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kayıt onaylama hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Kayıt reddet
    public boolean reject(int id) {
        String sql = "UPDATE registrations SET status = 'REJECTED', response_date = datetime('now') WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kayıt reddetme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ DELETE ============
    
    public boolean delete(int id) {
        String sql = "DELETE FROM registrations WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Kayıt silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ FAILED COURSES (ALTTAN DERSLER) ============
    
    // Öğrencinin başarısız olduğu dersleri getir (FF, FD, DD, DC notları)
    public List<TranscriptEntry> getFailedCourses(int studentId) {
        List<TranscriptEntry> failedCourses = new ArrayList<>();
        String sql = """
            SELECT cc.student_id, cat.code, cat.name, cat.credits, cat.semester, 
                   cc.grade, COALESCE(cc.entry_type, 'REGULAR') as entry_type
            FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ? AND cc.grade IN ('FF', 'FD', 'DD', 'DC')
            ORDER BY cat.semester, cat.code
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TranscriptEntry entry = new TranscriptEntry(
                    rs.getInt("student_id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("semester"),
                    rs.getString("grade"),
                    rs.getString("entry_type")
                );
                failedCourses.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("Başarısız ders listeleme hatası: " + e.getMessage());
        }
        return failedCourses;
    }
    
    // Öğrencinin belirli bir dersten başarısız olup olmadığını kontrol et (ders kodu ile)
    public boolean hasFailedCourse(int studentId, String courseCode) {
        String sql = """
            SELECT COUNT(*) FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ? AND cat.code = ? AND cc.grade IN ('FF', 'FD', 'DD', 'DC')
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, courseCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Başarısız ders kontrol hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Öğrencinin başarısız olduğu ve şu an açık olan dersleri getir
    public List<String> getFailedCourseCodesWithOpenCourses(int studentId) {
        List<String> failedCourseCodes = new ArrayList<>();
        String sql = """
            SELECT DISTINCT cat.code 
            FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            INNER JOIN courses c ON c.code = cat.code
            WHERE cc.student_id = ? AND cc.grade IN ('FF', 'FD', 'DD', 'DC')
            AND NOT EXISTS (
                SELECT 1 FROM registrations r 
                WHERE r.student_id = cc.student_id AND r.course_id = c.id 
                AND r.status IN ('PENDING', 'APPROVED')
            )
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                failedCourseCodes.add(rs.getString("code"));
            }
        } catch (SQLException e) {
            System.out.println("Açık başarısız ders listeleme hatası: " + e.getMessage());
        }
        return failedCourseCodes;
    }
    
    // Öğrencinin kaydolmadığı başarısız ders sayısı (açık olan)
    public int getUnregisteredFailedCourseCount(int studentId) {
        return getFailedCourseCodesWithOpenCourses(studentId).size();
    }
    
    // Öğrencinin TÜM başarısız ders sayısı (açık olsun olmasın)
    public int getTotalFailedCourseCount(int studentId) {
        String sql = """
            SELECT COUNT(*) FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ? AND cc.grade IN ('FF', 'FD', 'DD', 'DC')
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Başarısız ders sayısı hatası: " + e.getMessage());
        }
        return 0;
    }
    
    // Öğrencinin TÜM başarısız ders kodlarını getir (transcript'teki)
    public List<String> getAllFailedCourseCodes(int studentId) {
        List<String> failedCodes = new ArrayList<>();
        String sql = """
            SELECT cat.code FROM completed_courses cc
            INNER JOIN course_catalog cat ON cc.course_id = cat.id
            WHERE cc.student_id = ? AND cc.grade IN ('FF', 'FD', 'DD', 'DC')
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                failedCodes.add(rs.getString("code"));
            }
        } catch (SQLException e) {
            System.out.println("Başarısız ders kodları hatası: " + e.getMessage());
        }
        return failedCodes;
    }
    
    // ============ HELPER ============
    
    private Registration mapToRegistration(ResultSet rs) throws SQLException {
        Registration reg = new Registration();
        reg.setId(rs.getInt("id"));
        reg.setStudentId(rs.getInt("student_id"));
        reg.setCourseId(rs.getInt("course_id"));
        reg.setStatus(rs.getString("status"));
        reg.setRequestDate(rs.getString("request_date"));
        reg.setResponseDate(rs.getString("response_date"));
        return reg;
    }
}
