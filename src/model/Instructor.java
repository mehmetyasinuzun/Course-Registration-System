package model;

public class Instructor extends User {
    
    public Instructor() {
        setRole("INSTRUCTOR");
    }
    
    public Instructor(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, "INSTRUCTOR");
    }
}
