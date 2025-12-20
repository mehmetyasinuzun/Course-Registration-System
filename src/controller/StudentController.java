package controller;

import service.CourseService;
import service.RegistrationService;
import service.NotificationService;
import service.UserService;
import model.*;
import java.util.List;

public class StudentController {

    private CourseService courseService;
    private RegistrationService regService;
    private NotificationService notifService;
    private UserService userService;

    public StudentController() {
        this.courseService = new CourseService();
        this.regService = new RegistrationService();
        this.notifService = new NotificationService();
        this.userService = new UserService();
    }

    // ========== DERS İŞLEMLERİ ==========

    // Tüm dersleri getir
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    // Ders detayı
    public Course getCourseById(int courseId) {
        return courseService.getCourseById(courseId);
    }

    // Derse kayıtlı öğrenci sayısı
    public int getEnrollmentCount(int courseId) {
        return courseService.getEnrollmentCount(courseId);
    }

    // Derste yer var mı?
    public boolean hasAvailableCapacity(int courseId) {
        return courseService.hasAvailableCapacity(courseId);
    }

    // Ön koşulları getir
    public List<Course> getPrerequisites(int courseId) {
        return courseService.getPrerequisites(courseId);
    }

    // Kayıtlı derslerim
    public List<Course> getMyCourses(int studentId) {
        return courseService.getStudentCourses(studentId);
    }

    // Hoca bilgisi getir
    public User getInstructorById(int instructorId) {
        return userService.getUserById(instructorId);
    }

    // ========== KAYIT İŞLEMLERİ ==========

    // Derse kayıt ol (dönem kontrolü ile)
    public String registerForCourse(int studentId, int courseId, int studentTotalSemester) {
        return regService.registerForCourse(studentId, courseId, studentTotalSemester);
    }

    // Kayıtlarımı getir
    public List<Registration> getMyRegistrations(int studentId) {
        return regService.getStudentRegistrations(studentId);
    }

    // Kayıt iptal (dersten çekilme)
    public boolean cancelRegistration(int registrationId) {
        return regService.cancelRegistration(registrationId);
    }

    // Bu dersi tamamlamış mıyım?
    public boolean hasCompletedCourse(int studentId, int courseId) {
        return regService.hasCompletedCourse(studentId, courseId);
    }

    // Transkriptimi getir
    public List<TranscriptEntry> getMyTranscript(int studentId) {
        return regService.getStudentTranscript(studentId);
    }

    // Tamamlanan ders sayım
    public int getCompletedCourseCount(int studentId) {
        return regService.getCompletedCourseCount(studentId);
    }

    // Tamamlanan toplam kredim
    public int getCompletedCredits(int studentId) {
        return regService.getCompletedCredits(studentId);
    }

    // GPA'mı hesapla
    public double calculateMyGPA(int studentId) {
        return regService.calculateGPA(studentId);
    }

    // ========== ALTTAN DERS (FAILED COURSE) İŞLEMLERİ ==========

    // Başarısız derslerimi getir
    public List<TranscriptEntry> getFailedCourses(int studentId) {
        return regService.getFailedCourses(studentId);
    }

    // Açık olan başarısız ders kodlarımı getir (kayıt olmadıklarım)
    public List<String> getFailedCourseCodesWithOpenCourses(int studentId) {
        return regService.getFailedCourseCodesWithOpenCourses(studentId);
    }

    // Kayıt olmadığım başarısız ders sayım
    public int getUnregisteredFailedCourseCount(int studentId) {
        return regService.getUnregisteredFailedCourseCount(studentId);
    }

    // Bu dersten daha önce başarısız mı oldum?
    public boolean hasFailedCourse(int studentId, String courseCode) {
        return regService.hasFailedCourse(studentId, courseCode);
    }

    // TOPLAM başarısız ders sayım (açık olsun olmasın transcript'teki)
    public int getTotalFailedCourseCount(int studentId) {
        return regService.getTotalFailedCourseCount(studentId);
    }

    // TOPLAM başarısız ders kodları (transcript'teki tüm başarısız dersler)
    public List<String> getAllFailedCourseCodes(int studentId) {
        return regService.getAllFailedCourseCodes(studentId);
    }


    // ========== BİLDİRİMLER ==========

    public List<Notification> getNotifications(int userId) {
        return notifService.getNotifications(userId);
    }

    public List<Notification> getUnreadNotifications(int userId) {
        return notifService.getUnreadNotifications(userId);
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

    // ========== KREDİ İŞLEMLERİ ==========

    // Mevcut kredi toplamı
    public int getCurrentCredits(int studentId) {
        return regService.getCurrentCredits(studentId);
    }

    // Kalan kredi
    public int getRemainingCredits(int studentId) {
        return regService.getRemainingCredits(studentId);
    }

    // Maksimum kredi
    public int getMaxCredits() {
        return regService.getMaxCredits();
    }

    // ========== DÖNEM BAZLI DERS İŞLEMLERİ ==========

    // Öğrencinin dönemine uygun dersleri getir
    public List<Course> getCoursesBySemester(int totalSemester) {
        return courseService.getCoursesBySemester(totalSemester);
    }
}
