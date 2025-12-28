package repository;

import database.DatabaseConnection;
import model.CourseRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseRequestRepository {

    private Connection conn;

    public CourseRequestRepository() {
        this.conn = DatabaseConnection.getConnection();
    }

    // Talep oluştur (SEMESTER DAHİL)
    public int insert(CourseRequest req) {
        String sql = """
            INSERT INTO course_requests
            (code, name, credits, capacity, instructor_id, day_of_week, start_time, end_time, semester, status, request_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', datetime('now'))
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, req.getCode());
            pstmt.setString(2, req.getName());
            pstmt.setInt(3, req.getCredits());
            pstmt.setInt(4, req.getCapacity());
            pstmt.setInt(5, req.getInstructorId());
            pstmt.setString(6, req.getDayOfWeek());
            pstmt.setString(7, req.getStartTime());
            pstmt.setString(8, req.getEndTime());
            pstmt.setInt(9, req.getSemester()); // ✅ semester artık kaydediliyor

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Ders talebi ekleme hatası: " + e.getMessage());
        }
        return -1;
    }

    // ID ile bul
    public CourseRequest findById(int id) {
        String sql = "SELECT * FROM course_requests WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToRequest(rs);
            }
        } catch (SQLException e) {
            System.out.println("Ders talebi arama hatası: " + e.getMessage());
        }
        return null;
    }

    // Tüm bekleyen talepler
    public List<CourseRequest> findPending() {
        List<CourseRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM course_requests WHERE status = 'PENDING' ORDER BY request_date";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapToRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println("Bekleyen talep listeleme hatası: " + e.getMessage());
        }
        return requests;
    }

    // Hocanın talepleri
    public List<CourseRequest> findByInstructorId(int instructorId) {
        List<CourseRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM course_requests WHERE instructor_id = ? ORDER BY request_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(mapToRequest(rs));
            }
        } catch (SQLException e) {
            System.out.println("Hoca talepleri listeleme hatası: " + e.getMessage());
        }
        return requests;
    }

    // Ders kodu var mı kontrolü (hem course hem request tablosunda)
    public boolean isCodeExists(String code) {
        String sql = """
            SELECT COUNT(*) FROM (
                SELECT code FROM courses WHERE code = ?
                UNION
                SELECT code FROM course_requests WHERE code = ? AND status = 'PENDING'
            )
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Kod kontrolü hatası: " + e.getMessage());
        }
        return false;
    }

    // Hocanın pending taleplerinde schedule çakışması kontrolü
    public CourseRequest checkInstructorPendingTimeConflict(int instructorId, String dayOfWeek, String startTime, String endTime) {
        String sql = """
            SELECT * FROM course_requests
            WHERE instructor_id = ?
            AND day_of_week = ?
            AND status = 'PENDING'
            AND (
                (start_time <= ? AND end_time > ?) OR
                (start_time < ? AND end_time >= ?) OR
                (start_time >= ? AND end_time <= ?)
            )
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setString(2, dayOfWeek);

            pstmt.setString(3, startTime);
            pstmt.setString(4, startTime);

            pstmt.setString(5, endTime);
            pstmt.setString(6, endTime);

            pstmt.setString(7, startTime);
            pstmt.setString(8, endTime);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToRequest(rs);
            }
        } catch (SQLException e) {
            System.out.println("Pending çakışma kontrolü hatası: " + e.getMessage());
        }
        return null;
    }

    // Onayla
    public boolean approve(int id) {
        String sql = "UPDATE course_requests SET status = 'APPROVED', response_date = datetime('now') WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Talep onaylama hatası: " + e.getMessage());
        }
        return false;
    }

    // Reddet
    public boolean reject(int id) {
        String sql = "UPDATE course_requests SET status = 'REJECTED', response_date = datetime('now') WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Talep reddetme hatası: " + e.getMessage());
        }
        return false;
    }

    // Sil
    public boolean delete(int id) {
        String sql = "DELETE FROM course_requests WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Talep silme hatası: " + e.getMessage());
        }
        return false;
    }

    private CourseRequest mapToRequest(ResultSet rs) throws SQLException {
        CourseRequest req = new CourseRequest();
        req.setId(rs.getInt("id"));
        req.setCode(rs.getString("code"));
        req.setName(rs.getString("name"));
        req.setCredits(rs.getInt("credits"));
        req.setCapacity(rs.getInt("capacity"));
        req.setInstructorId(rs.getInt("instructor_id"));
        req.setDayOfWeek(rs.getString("day_of_week"));
        req.setStartTime(rs.getString("start_time"));
        req.setEndTime(rs.getString("end_time"));
        req.setSemester(rs.getInt("semester")); // ✅ semester artık okunuyor
        req.setStatus(rs.getString("status"));
        req.setRequestDate(rs.getString("request_date"));
        req.setResponseDate(rs.getString("response_date"));
        return req;
    }
}
