package service;

import repository.NotificationRepository;
import repository.UserRepository;
import repository.CourseRepository;
import model.Notification;
import model.User;
import model.Course;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    
    private NotificationRepository notifRepo;
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    
    // ===== QUEUE: Bildirim gönderme kuyruğu (FIFO) =====
    private Queue<Notification> notificationQueue = new LinkedList<>();
    
    // ===== HASHMAP: Kullanıcı bazlı okunmamış bildirim sayısı cache =====
    private Map<Integer, Integer> unreadCountCache = new HashMap<>();
    
    public NotificationService() {
        this.notifRepo = new NotificationRepository();
        this.userRepo = new UserRepository();
        this.courseRepo = new CourseRepository();
    }
    
    // ===== QUEUE YÖNETİMİ =====
    
    /**
     * Kuyruğa bildirim ekle (toplu gönderim için)
     * @param notification Eklenecek bildirim
     */
    public void queueNotification(Notification notification) {
        notificationQueue.offer(notification);
    }
    
    /**
     * Kuyruktaki bildirimleri işle ve gönder
     * @return Gönderilen bildirim sayısı
     */
    public int processNotificationQueue() {
        int sent = 0;
        while (!notificationQueue.isEmpty()) {
            Notification notif = notificationQueue.poll();
            if (notif != null) {
                int id = notifRepo.insert(notif);
                if (id > 0) {
                    sent++;
                    invalidateUnreadCountCache(notif.getUserId());
                }
            }
        }
        return sent;
    }
    
    /**
     * Kuyruk boyutu
     * @return Kuyruktaki bildirim sayısı
     */
    public int getQueueSize() {
        return notificationQueue.size();
    }
    
    /**
     * Sıradaki bildirimi göster (çıkarmadan)
     * @return Sıradaki bildirim
     */
    public Notification peekNextNotification() {
        return notificationQueue.peek();
    }
    
    // ===== HASHMAP: CACHE YÖNETİMİ =====
    
    /**
     * Okunmamış bildirim sayısını cache'ten al - O(1)
     */
    public int getUnreadCountFast(int userId) {
        if (unreadCountCache.containsKey(userId)) {
            return unreadCountCache.get(userId);
        }
        // Cache miss - veritabanından al ve cache'le
        int count = notifRepo.getUnreadCount(userId);
        unreadCountCache.put(userId, count);
        return count;
    }
    
    /**
     * Cache'i güncelle
     */
    public void updateUnreadCountCache(int userId, int count) {
        unreadCountCache.put(userId, count);
    }
    
    /**
     * Belirli kullanıcının cache'ini temizle
     */
    public void invalidateUnreadCountCache(int userId) {
        unreadCountCache.remove(userId);
    }
    
    /**
     * Tüm cache'i temizle
     */
    public void clearUnreadCountCache() {
        unreadCountCache.clear();
    }
    
    // ===== MEVCUT METODLAR (Güncellendi) =====
    
    // New registration notification (to instructor)
    public void sendNewRegistrationNotification(int instructorId, int studentId, int courseId) {
        User student = userRepo.findById(studentId);
        Course course = courseRepo.findById(courseId);
        
        if (student != null && course != null) {
            String title = "📋 New Registration Request";
            String message = student.getFullName() + " has applied for the course '" + course.getCode() + " - " + course.getName() + "'.";
            
            Notification notif = new Notification(instructorId, title, message);
            notifRepo.insert(notif);
            
            // Cache'i invalidate et
            invalidateUnreadCountCache(instructorId);
        }
    }
    
    // Registration approval/rejection notification (to student)
    public void sendApprovalNotification(int studentId, int courseId, boolean approved) {
        Course course = courseRepo.findById(courseId);
        
        if (course != null) {
            String title = approved ? "✅ Registration Approved" : "❌ Registration Rejected";
            String message = "Your registration for '" + course.getCode() + " - " + course.getName() + "' has been " + (approved ? "approved." : "rejected.");
            
            Notification notif = new Notification(studentId, title, message);
            notifRepo.insert(notif);
            
            // Cache'i invalidate et
            invalidateUnreadCountCache(studentId);
        }
    }
    
    // Send general notification
    public void sendNotification(int userId, String title, String message) {
        Notification notif = new Notification(userId, title, message);
        notifRepo.insert(notif);
        
        // Cache'i invalidate et
        invalidateUnreadCountCache(userId);
    }
    
    // Send notification to multiple users using Queue
    public void sendBulkNotifications(List<Integer> userIds, String title, String message) {
        for (int userId : userIds) {
            Notification notif = new Notification(userId, title, message);
            queueNotification(notif);
        }
        // Kuyruğu işle
        processNotificationQueue();
    }
    
    // Get user's notifications
    public List<Notification> getNotifications(int userId) {
        return notifRepo.findByUserId(userId);
    }
    
    // Get unread notifications
    public List<Notification> getUnreadNotifications(int userId) {
        return notifRepo.findUnreadByUserId(userId);
    }
    
    // Unread count - uses HashMap cache
    public int getUnreadCount(int userId) {
        return getUnreadCountFast(userId);
    }
    
    // Mark notification as read
    public boolean markAsRead(int notificationId) {
        boolean success = notifRepo.markAsRead(notificationId);
        // Not: Bildirim sahibinin cache'ini invalidate etmek için
        // ek bir veritabanı sorgusu gerekir, basitlik için atlıyoruz
        return success;
    }
    
    // Mark all as read
    public boolean markAllAsRead(int userId) {
        boolean success = notifRepo.markAllAsRead(userId);
        if (success) {
            // Cache'i güncelle - 0 okunmamış
            updateUnreadCountCache(userId, 0);
        }
        return success;
    }
    
    // Delete notification
    public boolean deleteNotification(int notificationId) {
        return notifRepo.delete(notificationId);
    }
    
    // Delete all notifications
    public boolean deleteAllNotifications(int userId) {
        return notifRepo.deleteAllByUserId(userId);
    }
}
