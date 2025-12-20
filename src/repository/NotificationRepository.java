package repository;

import database.DatabaseConnection;
import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    
    private Connection conn;
    
    public NotificationRepository() {
        this.conn = DatabaseConnection.getConnection();
    }
    
    // ============ CREATE ============
    
    public int insert(Notification notif) {
        String sql = "INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, 0, datetime('now'))";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, notif.getUserId());
            pstmt.setString(2, notif.getTitle());
            pstmt.setString(3, notif.getMessage());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Bildirim ekleme hatası: " + e.getMessage());
        }
        return -1;
    }
    
    // ============ READ ============
    
    // Kullanıcının tüm bildirimleri
    public List<Notification> findByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapToNotification(rs));
            }
        } catch (SQLException e) {
            System.out.println("Bildirim listeleme hatası: " + e.getMessage());
        }
        return notifications;
    }
    
    // Kullanıcının okunmamış bildirimleri
    public List<Notification> findUnreadByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notifications.add(mapToNotification(rs));
            }
        } catch (SQLException e) {
            System.out.println("Okunmamış bildirim listeleme hatası: " + e.getMessage());
        }
        return notifications;
    }
    
    // Okunmamış bildirim sayısı
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Bildirim sayısı hatası: " + e.getMessage());
        }
        return 0;
    }
    
    // ============ UPDATE ============
    
    // Bildirimi okundu yap
    public boolean markAsRead(int id) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Bildirim güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Tüm bildirimleri okundu yap
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Toplu bildirim güncelleme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ DELETE ============
    
    public boolean delete(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Bildirim silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // Kullanıcının tüm bildirimlerini sil
    public boolean deleteAllByUserId(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Toplu bildirim silme hatası: " + e.getMessage());
        }
        return false;
    }
    
    // ============ HELPER ============
    
    private Notification mapToNotification(ResultSet rs) throws SQLException {
        Notification notif = new Notification();
        notif.setId(rs.getInt("id"));
        notif.setUserId(rs.getInt("user_id"));
        notif.setTitle(rs.getString("title"));
        notif.setMessage(rs.getString("message"));
        notif.setRead(rs.getInt("is_read") == 1);
        notif.setCreatedAt(rs.getString("created_at"));
        return notif;
    }
}
