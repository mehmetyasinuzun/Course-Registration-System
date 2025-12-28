package model;

public class TranscriptEntry {
    private int studentId;
    private String courseCode;
    private String courseName;
    private int credits;
    private int semester;
    private String grade;
    private String entryType; // REGULAR, TRANSFER, SUMMER_SCHOOL, ERASMUS, SYSTEM_ERROR
    
    // Empty constructor
    public TranscriptEntry() {
        this.grade = "CC";
        this.entryType = "REGULAR";
    }
    
    // Constructor with parameters
    public TranscriptEntry(int studentId, String courseCode, String courseName, int credits, int semester, String grade) {
        this(studentId, courseCode, courseName, credits, semester, grade, "REGULAR");
    }
    
    // Constructor with all parameters
    public TranscriptEntry(int studentId, String courseCode, String courseName, int credits, int semester, String grade, String entryType) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.semester = semester;
        this.grade = grade;
        this.entryType = entryType;
    }
    
    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    
    // Display semester information
    public String getSemesterDisplay() {
        if (semester == 0) return "Genel";
        int year = (semester + 1) / 2;
        int sem = (semester % 2 == 1) ? 1 : 2;
        return "Year " + year + " - Semester " + sem;
    }
    
    // Entry type display
    public String getEntryTypeDisplay() {
        switch(entryType) {
            case "REGULAR": return "Normal";
            case "TRANSFER": return "Transfer";
            case "SUMMER_SCHOOL": return "Summer School";
            case "ERASMUS": return "Erasmus";
            case "SYSTEM_ERROR": return "System Error";
            default: return entryType;
        }
    }
    
    // Grade validation
    public static boolean isValidGrade(String grade) {
        return grade.matches("^(AA|BA|BB|CB|CC|DC|DD|FD|FF)$");
    }
    
    // Is passing grade?
    public boolean isPassingGrade() {
        return grade.matches("^(AA|BA|BB|CB|CC)$");
    }
    
    // Get grade point for GPA calculation
    public double getGradePoint() {
        switch(grade) {
            case "AA": return 4.00;
            case "BA": return 3.50;
            case "BB": return 3.00;
            case "CB": return 2.50;
            case "CC": return 2.00;
            case "DC": return 1.50;
            case "DD": return 1.00;
            case "FD": return 0.50;
            case "FF": return 0.00;
            default: return 0.00;
        }
    }
    
    @Override
    public String toString() {
        return courseCode + " - " + courseName + " (" + credits + " credits, Grade: " + grade + ")";
    }
}
