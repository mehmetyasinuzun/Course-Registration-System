package service;

import repository.CourseRequestRepository;
import repository.CourseRepository;
import model.CourseRequest;
import model.Course;
import java.util.List;

public class CourseRequestService {

    private CourseRequestRepository requestRepo;
    private CourseRepository courseRepo;
    private NotificationService notifService;

    public CourseRequestService() {
        this.requestRepo = new CourseRequestRepository();
        this.courseRepo = new CourseRepository();
        this.notifService = new NotificationService();
    }

    /**
     * Instructor creates a course opening request
     */
    public String createRequest(CourseRequest request) {
        // 1. Course code check
        if (requestRepo.isCodeExists(request.getCode())) {
            return "ERROR: This course code is already in use or has a pending request!";
        }

        // 2. Course name check (min 3 characters)
        if (request.getName() == null || request.getName().trim().length() < 3) {
            return "ERROR: Course name must be at least 3 characters!";
        }

        // 3. Course code format check (at least 2 letters + 2 digits)
        if (!isValidCourseCode(request.getCode())) {
            return "ERROR: Invalid course code format! Example: CS101, MATH201";
        }

        // 4. Time check
        if (!isValidTimeRange(request.getStartTime(), request.getEndTime())) {
            return "ERROR: End time must be after start time!";
        }

        // 5. Check pending request schedule conflict (same instructor, same time)
        CourseRequest conflictingRequest = requestRepo.checkInstructorPendingTimeConflict(
                request.getInstructorId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime()
        );
        if (conflictingRequest != null) {
            return "ERROR: You already have a pending course request at this time!\n\n" +
                    "Course: " + conflictingRequest.getCode() + " - " + conflictingRequest.getName() + "\n" +
                    "Schedule: " + conflictingRequest.getDayOfWeek() + " " +
                    conflictingRequest.getStartTime() + "-" + conflictingRequest.getEndTime();
        }

        // 6. Create request
        int id = requestRepo.insert(request);

        if (id > 0) {
            // Send notification to admins (ID=1 default admin)
            notifService.sendNotification(1, "New Course Request",
                    "New course opening request: " + request.getCode() + " - " + request.getName());
            return "SUCCESS: Your course opening request has been created. Awaiting admin approval.";
        }

        return "ERROR: Request could not be created!";
    }

    /**
     * Admin approves request and creates the course
     */
    public boolean approveRequest(int requestId) {
        CourseRequest request = requestRepo.findById(requestId);
        if (request == null) return false;

        // Approve request
        if (requestRepo.approve(requestId)) {

            // ✅ Create course WITH semester
            Course course = new Course(
                    request.getCode(),
                    request.getName(),
                    request.getCredits(),
                    request.getCapacity(),
                    request.getInstructorId(),
                    request.getDayOfWeek(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getSemester() // ⭐ KRİTİK: semester artık aktarılıyor
            );

            int courseId = courseRepo.insert(course);

            if (courseId > 0) {
                // Send notification to instructor
                notifService.sendNotification(request.getInstructorId(),
                        "Course Request Approved ✓",
                        "'" + request.getCode() + " - " + request.getName() + "' course has been approved and opened!");
                return true;
            }
        }
        return false;
    }

    /**
     * Admin rejects request
     */
    public boolean rejectRequest(int requestId) {
        CourseRequest request = requestRepo.findById(requestId);
        if (request == null) return false;

        if (requestRepo.reject(requestId)) {
            // Send notification to instructor
            notifService.sendNotification(request.getInstructorId(),
                    "Course Request Rejected ✗",
                    "'" + request.getCode() + " - " + request.getName() + "' course request has been rejected.");
            return true;
        }
        return false;
    }

    // Get pending requests
    public List<CourseRequest> getPendingRequests() {
        return requestRepo.findPending();
    }

    // Get instructor's requests
    public List<CourseRequest> getRequestsByInstructor(int instructorId) {
        return requestRepo.findByInstructorId(instructorId);
    }

    // Get request by ID
    public CourseRequest getRequestById(int id) {
        return requestRepo.findById(id);
    }

    // Course code format check
    private boolean isValidCourseCode(String code) {
        if (code == null || code.length() < 4) return false;
        // En az 2 harf ve 2 rakam içermeli
        int letters = 0, digits = 0;
        for (char c : code.toCharArray()) {
            if (Character.isLetter(c)) letters++;
            else if (Character.isDigit(c)) digits++;
        }
        return letters >= 2 && digits >= 2;
    }

    // Time range check for format "08:00-08:40"
    private boolean isValidTimeRange(String startTime, String endTime) {
        try {
            // Extract end time from the time range (e.g., "08:00-08:40" -> "08:40")
            String startEndTime = startTime.contains("-") ? startTime.split("-")[1] : startTime;
            String endEndTime = endTime.contains("-") ? endTime.split("-")[1] : endTime;

            int startMinutes = timeToMinutes(startEndTime);
            int endMinutes = timeToMinutes(endEndTime);

            // End time must be greater than or equal to start time
            return endMinutes >= startMinutes;
        } catch (Exception e) {
            return false;
        }
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
}
