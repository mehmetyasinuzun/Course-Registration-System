package model;

/**
 * Course opening request – Requested by Instructor, approved by Admin
 */
public class CourseRequest {
    private int id;
    private String code;
    private String name;
    private int credits;
    private int capacity;
    private int instructorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int semester;  // 0 = general, 1–8 = semester
    private String status;  // PENDING, APPROVED, REJECTED
    private String requestDate;
    private String responseDate;

    public CourseRequest() {
        this.status = "PENDING";
    }

    public CourseRequest(String code, String name, int credits, int capacity,
                         int instructorId, String dayOfWeek, String startTime, String endTime) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.instructorId = instructorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getResponseDate() { return responseDate; }
    public void setResponseDate(String responseDate) { this.responseDate = responseDate; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    // Status display (English)
    public String getStatusDisplay() {
        return switch (status) {
            case "PENDING" -> "Pending";
            case "APPROVED" -> "Approved";
            case "REJECTED" -> "Rejected";
            default -> status;
        };
    }

    // Schedule display (English)
    public String getScheduleDisplay() {
        String day = switch (dayOfWeek) {
            case "MONDAY" -> "Monday";
            case "TUESDAY" -> "Tuesday";
            case "WEDNESDAY" -> "Wednesday";
            case "THURSDAY" -> "Thursday";
            case "FRIDAY" -> "Friday";
            case "SATURDAY" -> "Saturday";
            case "SUNDAY" -> "Sunday";
            default -> dayOfWeek;
        };
        return day + " " + startTime + "-" + endTime;
    }
}
