package service;

import repository.NotificationRepository;
import repository.UserRepository;
import repository.CourseRepository;
import model.Notification;
import model.User;
import model.Course;
import java.util.List;

public class NotificationService {
    
    private NotificationRepository notifRepo;
    private UserRepository userRepo;
    private CourseRepository courseRepo;
    
    public NotificationService() {
        this.notifRepo = new NotificationRepository();
        this.userRepo = new UserRepository();
        this.courseRepo = new CourseRepository();
    }
    
    // New registration notification (to instructor)
    public void sendNewRegistrationNotification(int instructorId, int studentId, int courseId) {
        User student = userRepo.findById(studentId);
        Course course = courseRepo.findById(courseId);
        
        if (student != null && course != null) {
            String title = "📋 New Registration Request";
            String message = student.getFullName() + " has applied for the course '" + course.getCode() + " - " + course.getName() + "'.";
            
            Notification notif = new Notification(instructorId, title, message);
            notifRepo.insert(notif);
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
        }
    }
    
    // Send general notification
    public void sendNotification(int userId, String title, String message) {
        Notification notif = new Notification(userId, title, message);
        notifRepo.insert(notif);
    }
    
    // Get user's notifications
    public List<Notification> getNotifications(int userId) {
        return notifRepo.findByUserId(userId);
    }
    
    // Get unread notifications
    public List<Notification> getUnreadNotifications(int userId) {
        return notifRepo.findUnreadByUserId(userId);
    }
    
    // Unread count
    public int getUnreadCount(int userId) {
        return notifRepo.getUnreadCount(userId);
    }
    
    // Mark notification as read
    public boolean markAsRead(int notificationId) {
        return notifRepo.markAsRead(notificationId);
    }
    
    // Mark all as read
    public boolean markAllAsRead(int userId) {
        return notifRepo.markAllAsRead(userId);
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
