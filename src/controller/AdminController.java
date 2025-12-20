package controller;

import service.*;
import repository.CourseCatalogRepository;
import model.*;
import java.util.List;

public class AdminController {

    private UserService userService;
    private CourseService courseService;
    private RegistrationService regService;
    private CourseRequestService requestService;
    private CourseCatalogRepository catalogRepo;

    public AdminController() {
        this.userService = new UserService();
        this.courseService = new CourseService();
        this.regService = new RegistrationService();
        this.requestService = new CourseRequestService();
        this.catalogRepo = new CourseCatalogRepository();
    }

    // ========== DASHBOARD STATS ==========
    public int getTotalStudents() {
        return userService.getAllStudents().size();
    }

    public int getTotalInstructors() {
        return userService.getAllInstructors().size();
    }

    public int getTotalCourses() {
        return courseService.getAllCourses().size();
    }

    public int getPendingCount() {
        return regService.getPendingRegistrations().size();
    }

    public int getPendingCourseRequestCount() {
        return requestService.getPendingRequests().size();
    }

    // ========== USER MANAGEMENT ==========
    public List<User> getAllStudents() {
        return userService.getAllStudents();
    }

    public List<User> getAllInstructors() {
        return userService.getAllInstructors();
    }

    public User getUserById(int id) {
        return userService.getUserById(id);
    }

    public int addStudent(String username, String password, String fullName, String email, String studentNumber, int year, int semester) {
        return userService.addStudent(username, password, fullName, email, studentNumber, year, semester);
    }

    public int addInstructor(String username, String password, String fullName, String email) {
        return userService.addInstructor(username, password, fullName, email);
    }

    public boolean deleteUser(int userId) {
        return userService.deleteUser(userId);
    }

    // ========== COURSE MANAGEMENT ==========
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    public Course getCourseById(int id) {
        return courseService.getCourseById(id);
    }

    public int getEnrollmentCount(int courseId) {
        return courseService.getEnrollmentCount(courseId);
    }

    public boolean deleteCourse(int courseId) {
        return courseService.deleteCourse(courseId);
    }

    // Open a course from catalog (Admin direct action)
    public String openCourseFromCatalogWithResult(int catalogId, int capacity, int instructorId, String dayOfWeek, String startTime, String endTime) {
        CourseCatalog catalog = catalogRepo.findById(catalogId);
        if (catalog == null) {
            return "ERROR: Course catalog not found!";
        }

        // Check instructor time conflict
        Course conflicting = courseService.checkInstructorTimeConflict(instructorId, dayOfWeek, startTime, endTime);
        if (conflicting != null) {
            return "ERROR: Instructor has a conflicting course: " + conflicting.getCode() + " (" + conflicting.getScheduleDisplay() + ")";
        }

        Course course = new Course(
            catalog.getCode(),
            catalog.getName(),
            catalog.getCredits(),
            capacity,
            instructorId,
            dayOfWeek,
            startTime,
            endTime
        );
        
        // Set semester from catalog
        course.setSemester(catalog.getSemester());

        int courseId = courseService.addCourse(course);
        if (courseId > 0) {
            return "SUCCESS: Course opened successfully.";
        } else {
            return "ERROR: Could not open course (Code might already exist).";
        }
    }

    // ========== COURSE CATALOG ==========
    public List<CourseCatalog> getAllCatalogCourses() {
        return catalogRepo.findAll();
    }

    public int addCatalogCourse(String code, String name, int credits, int semester) {
        CourseCatalog catalog = new CourseCatalog(code, name, credits, semester);
        return catalogRepo.insert(catalog);
    }

    public boolean updateCatalogCourse(CourseCatalog catalog) {
        return catalogRepo.update(catalog);
    }

    public boolean deleteCatalogCourse(int id) {
        return catalogRepo.delete(id);
    }

    // ========== COURSE REQUESTS ==========
    public List<CourseRequest> getPendingCourseRequests() {
        return requestService.getPendingRequests();
    }

    public boolean approveCourseRequest(int requestId) {
        return requestService.approveRequest(requestId);
    }

    public boolean rejectCourseRequest(int requestId) {
        return requestService.rejectRequest(requestId);
    }

    // ========== REGISTRATIONS ==========
    public List<Registration> getPendingRegistrations() {
        return regService.getPendingRegistrations();
    }

    public boolean approveRegistration(int regId) {
        // We need studentId and courseId for notification, but regService.approveRegistration handles it internally?
        // Wait, regService.approveRegistration(int registrationId, int studentId, int courseId)
        // But AdminView calls adminController.approveRegistration(reg.getId())
        // So I need to fetch the registration first to get studentId and courseId.
        
        Registration reg = regService.getRegistrationById(regId);
        if (reg != null) {
            return regService.approveRegistration(regId, reg.getStudentId(), reg.getCourseId());
        }
        return false;
    }

    public boolean rejectRegistration(int regId) {
        Registration reg = regService.getRegistrationById(regId);
        if (reg != null) {
            return regService.rejectRegistration(regId, reg.getStudentId(), reg.getCourseId());
        }
        return false;
    }

    // ========== TRANSCRIPT & CREDITS ==========
    public List<TranscriptEntry> getStudentTranscript(int studentId) {
        return regService.getStudentTranscript(studentId);
    }

    public double calculateStudentGPA(int studentId) {
        return regService.calculateGPA(studentId);
    }

    public int getCompletedCredits(int studentId) {
        return regService.getCompletedCredits(studentId);
    }

    public int getCompletedCourseCount(int studentId) {
        return regService.getCompletedCourseCount(studentId);
    }

    public boolean deleteCompletedCourse(int studentId, int courseId) {
        return regService.deleteCompletedCourse(studentId, courseId);
    }

    public boolean addCompletedCourseWithGrade(int studentId, String courseCode, String grade, String entryType) {
        return regService.addCompletedCourseWithGrade(studentId, courseCode, grade, entryType);
    }

    public boolean updateCompletedCourse(int studentId, int courseId, String grade, String entryType) {
        return regService.updateCompletedCourse(studentId, courseId, grade, entryType);
    }
    
    // ========== FAILED COURSES ==========
    public int getTotalFailedCourseCount(int studentId) {
        return regService.getTotalFailedCourseCount(studentId);
    }
}
