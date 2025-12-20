package model;

public class Admin extends User {
    
    public Admin() {
        setRole("ADMIN");
    }
    
    public Admin(String username, String password, String fullName, String email) {
        super(username, password, fullName, email, "ADMIN");
    }
}
