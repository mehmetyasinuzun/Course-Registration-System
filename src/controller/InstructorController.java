package controller;

import service.CourseService;
import service.RegistrationService;
import service.NotificationService;
import service.UserService;
import service.CourseRequestService;
import model.*;
import java.util.List;

public class InstructorController {
    
    private CourseService courseService;
    private RegistrationService regService;
    private NotificationService notifService;
    private UserService userService;
    private CourseRequestService requestService;
    
    public InstructorController() {
        this.courseService = new CourseService();
        this.regService = new RegistrationService();
        this.notifService = new NotificationService();
        this.userService = new UserService();
        this.requestService = new CourseRequestService();
    }
    
    // ========== DERS İŞLEMLERİ ==========
    
    // Hocanın derslerini getir
    public List<Course> getMyCourses(int instructorId) {
        return courseService.getCoursesByInstructor(instructorId);
    }
    
    // Ders açma talebi oluştur (Admin onayı gerekir)
    public String createCourseRequest(String code, String name, int credits, int capacity, 
                                       int instructorId, String dayOfWeek, String startTime, String endTime) {
        // Hoca çakışma kontrolü
        Course conflicting = courseService.checkInstructorTimeConflict(instructorId, dayOfWeek, startTime, endTime);
        if (conflicting != null) {
            return "ERROR: You already have a course at this time:\n\n" +
                   "Course: " + conflicting.getCode() + " - " + conflicting.getName() + "\n" +
                   "Schedule: " + conflicting.getScheduleDisplay();
        }
        
        CourseRequest request = new CourseRequest(code, name, credits, capacity, instructorId, dayOfWeek, startTime, endTime);
        return requestService.createRequest(request);
    }
    
    // Hocanın ders taleplerini getir
    public List<CourseRequest> getMyCourseRequests(int instructorId) {
        return requestService.getRequestsByInstructor(instructorId);
    }
    
    // Ders güncelle
    public boolean updateCourse(Course course) {
        return courseService.updateCourse(course);
    }
    
    // Ders sil
    public boolean deleteCourse(int courseId) {
        return courseService.deleteCourse(courseId);
    }
    
    // Ön koşul ekle
    public boolean addPrerequisite(int courseId, int prerequisiteId) {
        return courseService.addPrerequisite(courseId, prerequisiteId);
    }
    
    // Ön koşul sil
    public boolean removePrerequisite(int courseId, int prerequisiteId) {
        return courseService.removePrerequisite(courseId, prerequisiteId);
    }
    
    // Ön koşulları getir
    public List<Course> getPrerequisites(int courseId) {
        return courseService.getPrerequisites(courseId);
    }
    
    // Derse kayıtlı öğrenci sayısı
    public int getEnrollmentCount(int courseId) {
        return courseService.getEnrollmentCount(courseId);
    }
    
    // Tüm dersler (dropdown için)
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }
    
    // ID ile ders getir
    public Course getCourseById(int courseId) {
        return courseService.getCourseById(courseId);
    }
    
    // ========== KAYIT İŞLEMLERİ ==========
    
    // Bekleyen kayıtlar (sadece hocanın dersleri)
    public List<Registration> getPendingRegistrations(int instructorId) {
        return regService.getPendingByInstructor(instructorId);
    }
    
    // Dersin kayıtlı öğrencileri
    public List<Registration> getCourseRegistrations(int courseId) {
        return regService.getCourseRegistrations(courseId);
    }
    
    // Kayıt onayla
    public boolean approveRegistration(int registrationId) {
        Registration reg = regService.getRegistrationById(registrationId);
        if (reg != null) {
            return regService.approveRegistration(registrationId, reg.getStudentId(), reg.getCourseId());
        }
        return false;
    }
    
    // Kayıt reddet
    public boolean rejectRegistration(int registrationId) {
        Registration reg = regService.getRegistrationById(registrationId);
        if (reg != null) {
            return regService.rejectRegistration(registrationId, reg.getStudentId(), reg.getCourseId());
        }
        return false;
    }
    
    // ========== BİLDİRİMLER ==========
    
    public List<Notification> getNotifications(int userId) {
        return notifService.getNotifications(userId);
    }
    
    public int getUnreadCount(int userId) {
        return notifService.getUnreadCount(userId);
    }
    
    public boolean markNotificationAsRead(int notificationId) {
        return notifService.markAsRead(notificationId);
    }
    
    public boolean markAllNotificationsAsRead(int userId) {
        return notifService.markAllAsRead(userId);
    }
    
    // ========== YARDIMCI ==========
    
    // Öğrenci bilgisi getir (kayıt listesinde göstermek için)
    public User getStudentById(int studentId) {
        return userService.getUserById(studentId);
    }
    
    // ========== DERS KATALOĞU ==========
    
    private repository.CourseCatalogRepository catalogRepo = new repository.CourseCatalogRepository();
    
    // Tüm katalog derslerini getir
    public java.util.List<model.CourseCatalog> getAllCatalogCourses() {
        return catalogRepo.findAll();
    }
    
    // Katalogdan ders talebi oluştur
    public String createCourseRequestFromCatalog(int catalogId, int capacity, int instructorId, 
                                                   String dayOfWeek, String startTime, String endTime) {
        model.CourseCatalog catalog = catalogRepo.findById(catalogId);
        if (catalog == null) return "ERROR: Course not found!";
        
        // Hoca çakışma kontrolü
        Course conflicting = courseService.checkInstructorTimeConflict(instructorId, dayOfWeek, startTime, endTime);
        if (conflicting != null) {
            return "ERROR: You already have a course at this time:\n\n" +
                   "Course: " + conflicting.getCode() + " - " + conflicting.getName() + "\n" +
                   "Schedule: " + conflicting.getScheduleDisplay();
        }
        
        CourseRequest request = new CourseRequest(
            catalog.getCode(), catalog.getName(), catalog.getCredits(),
            capacity, instructorId, dayOfWeek, startTime, endTime
        );
        request.setSemester(catalog.getSemester());
        return requestService.createRequest(request);
    }
}
