package model;

public class Student extends User {
    private String studentNumber;
    private int year;      // Class year (1, 2, 3, 4)
    private int semester;  // Semester (1 or 2)
    private String department;
    private String faculty;

    public Student() {
        setRole("STUDENT");
        this.year = 1;
        this.semester = 1;
        this.department = "Computer Engineering";
        this.faculty = "Engineering";
    }

    public Student(String username, String password, String fullName,
                   String email, String studentNumber, int year, int semester) {
        super(username, password, fullName, email, "STUDENT");
        this.studentNumber = studentNumber;
        this.year = year;
        this.semester = semester;
        this.department = "Computer Engineering";
        this.faculty = "Engineering";
    }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    // Calculate total semester (e.g. 2nd year 1st semester = 3rd semester)
    public int getTotalSemester() {
        return (year - 1) * 2 + semester;
    }

    // Display class year and semester
    public String getYearSemesterDisplay() {
        return year + ". Year - " + semester + ". Semester";
    }

    @Override
    public String toString() {
        return getFullName() + " (" + studentNumber + ")";
    }
}
