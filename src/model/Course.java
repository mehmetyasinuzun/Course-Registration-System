package model;

public class Course {
    private int id;
    private String code;
    private String name;
    private int credits;
    private int capacity;
    private int instructorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int semester;  // Semester the course is offered (1–8, 0 = all semesters)

    // Empty constructor
    public Course() {
        this.semester = 0; // Default: all semesters
    }

    // Constructor with parameters
    public Course(String code, String name, int credits, int capacity,
                  int instructorId, String dayOfWeek, String startTime, String endTime) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.capacity = capacity;
        this.instructorId = instructorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.semester = 0;
    }

    // Constructor with semester
    public Course(String code, String name, int credits, int capacity,
                  int instructorId, String dayOfWeek, String startTime, String endTime, int semester) {
        this(code, name, credits, capacity, instructorId, dayOfWeek, startTime, endTime);
        this.semester = semester;
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

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    // Display semester information
    public String getSemesterDisplay() {
        if (semester == 0) return "All Semesters";
        int year = (semester + 1) / 2;
        int sem = (semester % 2 == 1) ? 1 : 2;
        return year + ". Year " + sem + ". Semester";
    }

    // Helper method: display day and time (English)
    public String getScheduleDisplay() {
        String day;
        switch(dayOfWeek) {
            case "MONDAY": day = "Monday"; break;
            case "TUESDAY": day = "Tuesday"; break;
            case "WEDNESDAY": day = "Wednesday"; break;
            case "THURSDAY": day = "Thursday"; break;
            case "FRIDAY": day = "Friday"; break;
            case "SATURDAY": day = "Saturday"; break;
            case "SUNDAY": day = "Sunday"; break;
            default: day = dayOfWeek;
        }
        return day + " " + startTime + "-" + endTime;
    }

    // Get day name in English
    public String getDayDisplayName() {
        switch(dayOfWeek) {
            case "MONDAY": return "Monday";
            case "TUESDAY": return "Tuesday";
            case "WEDNESDAY": return "Wednesday";
            case "THURSDAY": return "Thursday";
            case "FRIDAY": return "Friday";
            case "SATURDAY": return "Saturday";
            case "SUNDAY": return "Sunday";
            default: return dayOfWeek;
        }
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
