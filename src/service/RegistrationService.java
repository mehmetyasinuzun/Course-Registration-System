package service;

import repository.RegistrationRepository;
import repository.CourseRepository;
import model.Registration;
import model.Course;
import model.TranscriptEntry;
import java.util.List;

public class RegistrationService {

    private static final int MAX_CREDITS = 30;  // Maximum credit limit

    private RegistrationRepository regRepo;
    private CourseRepository courseRepo;
    private CourseService courseService;
    private NotificationService notifService;

    public RegistrationService() {
        this.regRepo = new RegistrationRepository();
        this.courseRepo = new CourseRepository();
        this.courseService = new CourseService();
        this.notifService = new NotificationService();
    }

    // Calculate student's current credit total (PENDING + APPROVED)
    public int getCurrentCredits(int studentId) {
        int totalCredits = 0;
        List<Registration> registrations = regRepo.findByStudentId(studentId);

        for (Registration reg : registrations) {
            if (reg.getStatus().equals("PENDING") || reg.getStatus().equals("APPROVED")) {
                Course course = courseRepo.findById(reg.getCourseId());
                if (course != null) {
                    totalCredits += course.getCredits();
                }
            }
        }
        return totalCredits;
    }

    // Remaining credits
    public int getRemainingCredits(int studentId) {
        return MAX_CREDITS - getCurrentCredits(studentId);
    }

    // Maximum credit limit
    public int getMaxCredits() {
        return MAX_CREDITS;
    }

    // Register for a course
    public String registerForCourse(int studentId, int courseId) {
        return registerForCourse(studentId, courseId, 0);
    }

    // Register for a course (with student semester)
    public String registerForCourse(int studentId, int courseId, int studentTotalSemester) {
        Course course = courseRepo.findById(courseId);
        if (course == null) {
            return "ERROR: Course not found!";
        }

        // 0. PRIORITY CHECK - Failed course registration priority
        // If student has unregistered failed courses AND this is not a failed course retake,
        // check if failed courses are available and warn them
        List<String> unregisteredFailedCodes = regRepo.getFailedCourseCodesWithOpenCourses(studentId);
        boolean isRetakingFailedCourse = regRepo.hasFailedCourse(studentId, course.getCode());

        if (!unregisteredFailedCodes.isEmpty() && !isRetakingFailedCourse) {
            // Student has failed courses they haven't registered for yet
            return "ERROR: You must first register for your failed courses!\n\n" +
                   "🚨 Failed courses awaiting registration:\n" +
                   String.join(", ", unregisteredFailedCodes) + "\n\n" +
                   "You can only register for other courses after registering for all available failed courses.";
        }

        // 1. Semester check - Student cannot register for higher semester courses
        // (Skip this check for failed course retakes - they can retake regardless of semester)
        if (!isRetakingFailedCourse && studentTotalSemester > 0 && course.getSemester() > 0) {
            if (course.getSemester() > studentTotalSemester) {
                int courseYear = (course.getSemester() + 1) / 2;
                String coursePeriod = (course.getSemester() % 2 == 1) ? "Fall" : "Spring";
                return "ERROR: This course is above your semester! (Year " + courseYear + " " + coursePeriod + ")\nYou cannot register for higher semester courses.";
            }
        }

        // 2. Already registered?
        Registration existing = regRepo.findByStudentAndCourse(studentId, courseId);
        if (existing != null) {
            return "ERROR: You have already applied for this course! Status: " + existing.getStatusDisplay();
        }

        // 3. Credit limit check
        int currentCredits = getCurrentCredits(studentId);
        int newTotal = currentCredits + course.getCredits();
        if (newTotal > MAX_CREDITS) {
            return "ERROR: You are exceeding the credit limit! Current: " + currentCredits +
                   ", This course: " + course.getCredits() +
                   ", Maximum: " + MAX_CREDITS + " credits";
        }

        // 3. Capacity check
        if (!courseService.hasAvailableCapacity(courseId)) {
            return "ERROR: Course capacity is full!";
        }

        // 4. Prerequisite check (skip for failed course retakes - they already took it before)
        if (!isRetakingFailedCourse) {
            List<Course> prerequisites = courseRepo.getPrerequisites(courseId);
            for (Course prereq : prerequisites) {
                if (!regRepo.hasCompletedCourse(studentId, prereq.getId())) {
                    return "ERROR: Prerequisite course not completed: " + prereq.getCode() + " - " + prereq.getName();
                }
            }
        }

        // 5. Time conflict check
        Course conflicting = courseService.checkTimeConflict(studentId, courseId);
        if (conflicting != null) {
            return "ERROR: This course conflicts with '" + conflicting.getCode() + "'! (" + conflicting.getScheduleDisplay() + ")";
        }

        // 6. Create registration
        Registration reg = new Registration(studentId, courseId);
        int regId = regRepo.insert(reg);

        if (regId > 0) {
            // Send notification to instructor
            notifService.sendNewRegistrationNotification(course.getInstructorId(), studentId, courseId);
            return "SUCCESS: Your registration request has been received. Awaiting approval.";
        }

        return "ERROR: Registration could not be created!";
    }

    // Approve registration
    public boolean approveRegistration(int registrationId, int studentId, int courseId) {
        boolean success = regRepo.approve(registrationId);

        if (success) {
            // Send notification to student
            notifService.sendApprovalNotification(studentId, courseId, true);
        }

        return success;
    }

    // Reject registration
    public boolean rejectRegistration(int registrationId, int studentId, int courseId) {
        boolean success = regRepo.reject(registrationId);

        if (success) {
            // Send notification to student
            notifService.sendApprovalNotification(studentId, courseId, false);
        }

        return success;
    }

    // Cancel registration (student withdrawal)
    public boolean cancelRegistration(int registrationId) {
        return regRepo.delete(registrationId);
    }

    // Get pending registrations (entire system)
    public List<Registration> getPendingRegistrations() {
        return regRepo.findPending();
    }

    // Get instructor's pending registrations
    public List<Registration> getPendingByInstructor(int instructorId) {
        return regRepo.findPendingByInstructorId(instructorId);
    }

    // Get student's registrations
    public List<Registration> getStudentRegistrations(int studentId) {
        return regRepo.findByStudentId(studentId);
    }

    // Get course registrations
    public List<Registration> getCourseRegistrations(int courseId) {
        return regRepo.findByCourseId(courseId);
    }

    // Get registration by ID
    public Registration getRegistrationById(int id) {
        return regRepo.findById(id);
    }

    // Add completed course
    public boolean addCompletedCourse(int studentId, int courseId) {
        return regRepo.addCompletedCourse(studentId, courseId);
    }

    // Has student completed this course?
    public boolean hasCompletedCourse(int studentId, int courseId) {
        return regRepo.hasCompletedCourse(studentId, courseId);
    }

    // Get student transcript
    public List<TranscriptEntry> getStudentTranscript(int studentId) {
        return regRepo.getTranscript(studentId);
    }

    // Get completed course count
    public int getCompletedCourseCount(int studentId) {
        return regRepo.getCompletedCourseCount(studentId);
    }

    // Get completed credits
    public int getCompletedCredits(int studentId) {
        return regRepo.getCompletedCredits(studentId);
    }

    // Calculate GPA
    public double calculateGPA(int studentId) {
        return regRepo.calculateGPA(studentId);
    }

    // Update completed course (grade and entry type)
    public boolean updateCompletedCourse(int studentId, int courseId, String grade, String entryType) {
        return regRepo.updateCompletedCourse(studentId, courseId, grade, entryType);
    }

    // Delete completed course
    public boolean deleteCompletedCourse(int studentId, int courseId) {
        return regRepo.deleteCompletedCourse(studentId, courseId);
    }

    // ========== FAILED COURSE (ALTTAN DERS) METHODS ==========

    // Get student's failed courses
    public List<TranscriptEntry> getFailedCourses(int studentId) {
        return regRepo.getFailedCourses(studentId);
    }

    // Get failed course codes that have open courses available
    public List<String> getFailedCourseCodesWithOpenCourses(int studentId) {
        return regRepo.getFailedCourseCodesWithOpenCourses(studentId);
    }

    // Get count of unregistered failed courses
    public int getUnregisteredFailedCourseCount(int studentId) {
        return regRepo.getUnregisteredFailedCourseCount(studentId);
    }

    // Check if student has failed a specific course
    public boolean hasFailedCourse(int studentId, String courseCode) {
        return regRepo.hasFailedCourse(studentId, courseCode);
    }

    // Get total failed course count (all failed, not just open ones)
    public int getTotalFailedCourseCount(int studentId) {
        return regRepo.getTotalFailedCourseCount(studentId);
    }

    // Get all failed course codes from transcript
    public List<String> getAllFailedCourseCodes(int studentId) {
        return regRepo.getAllFailedCourseCodes(studentId);
    }

    // Add completed course with grade and entry type
    public boolean addCompletedCourseWithGrade(int studentId, String courseCode, String grade, String entryType) {
        return regRepo.addCompletedCourseWithGrade(studentId, courseCode, grade, entryType);
    }
}
