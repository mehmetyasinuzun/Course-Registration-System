package model;

public class Registration {
    private int id;
    private int studentId;
    private int courseId;
    private String status;  // PENDING, APPROVED, REJECTED
    private String requestDate;
    private String responseDate;

    // Empty constructor
    public Registration() {
        this.status = "PENDING";
    }

    // Constructor with parameters
    public Registration(int studentId, int courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getResponseDate() { return responseDate; }
    public void setResponseDate(String responseDate) { this.responseDate = responseDate; }

    // Status display (English)
    public String getStatusDisplay() {
        switch(status) {
            case "PENDING": return "Pending";
            case "APPROVED": return "Approved";
            case "REJECTED": return "Rejected";
            default: return status;
        }
    }
}
