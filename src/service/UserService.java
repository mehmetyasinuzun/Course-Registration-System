package service;

import repository.UserRepository;
import repository.CourseCatalogRepository;
import repository.RegistrationRepository;
import model.*;
import java.util.List;

public class UserService {
    
    private UserRepository userRepo;
    private CourseCatalogRepository catalogRepo;
    private RegistrationRepository regRepo;
    
    public UserService() {
        this.userRepo = new UserRepository();
        this.catalogRepo = new CourseCatalogRepository();
        this.regRepo = new RegistrationRepository();
    }
    
    // Öğrenci ekle (sınıf ve dönem ile)
    public int addStudent(String username, String password, String fullName, String email, String studentNumber, int year, int semester) {
        // Username kontrolü
        if (userRepo.findByUsername(username) != null) {
            System.out.println("Bu kullanıcı adı zaten kullanılıyor!");
            return -1;
        }
        
        Student student = new Student(username, password, fullName, email, studentNumber, year, semester);
        int studentId = userRepo.insert(student);
        
        // Öğrenci başarıyla eklenirse, bulunduğu döneme kadar tüm dersleri completed olarak ekle
        if (studentId > 0) {
            addCompletedCoursesForStudent(studentId, year, semester);
        }
        
        return studentId;
    }
    
    // Öğrencinin bulunduğu döneme kadar tüm dersleri completed olarak ekle
    private void addCompletedCoursesForStudent(int studentId, int year, int semester) {
        // Toplam dönem hesapla (örn: 2. sınıf 1. dönem = 3. dönem)
        int totalSemester = (year - 1) * 2 + semester;
        
        // Katalogdaki tüm dersleri al
        List<CourseCatalog> allCourses = catalogRepo.findAll();
        
        int addedCount = 0;
        // Öğrencinin döneminden küçük olan tüm dersleri ekle (CC notu ile)
        for (CourseCatalog catalog : allCourses) {
            // Eğer ders öğrencinin döneminden önceyse (ve semester=0 değilse)
            if (catalog.getSemester() > 0 && catalog.getSemester() < totalSemester) {
                // Bu dersi completed olarak ekle (CC notu ve REGULAR tip ile)
                boolean success = regRepo.addCompletedCourseWithGrade(studentId, catalog.getCode(), "CC", "REGULAR");
                if (success) {
                    addedCount++;
                }
            }
        }
        
        System.out.println("  ✓ " + addedCount + " dersi CC notu ile tamamlanmış olarak eklendi (Dönem " + totalSemester + " öncesi)");
    }
    
    // Hoca ekle
    public int addInstructor(String username, String password, String fullName, String email) {
        // Username kontrolü
        if (userRepo.findByUsername(username) != null) {
            System.out.println("Bu kullanıcı adı zaten kullanılıyor!");
            return -1;
        }
        
        Instructor instructor = new Instructor(username, password, fullName, email);
        return userRepo.insert(instructor);
    }
    
    // Kullanıcı güncelle
    public boolean updateUser(User user) {
        return userRepo.update(user);
    }
    
    // Kullanıcı sil
    public boolean deleteUser(int userId) {
        return userRepo.delete(userId);
    }
    
    // ID ile kullanıcı getir
    public User getUserById(int id) {
        return userRepo.findById(id);
    }
    
    // Username ile kullanıcı getir
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }
    
    // Tüm kullanıcılar
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
    
    // Öğrencileri getir
    public List<User> getAllStudents() {
        return userRepo.findByRole("STUDENT");
    }
    
    // Hocaları getir
    public List<User> getAllInstructors() {
        return userRepo.findByRole("INSTRUCTOR");
    }
    
    // Adminleri getir
    public List<User> getAllAdmins() {
        return userRepo.findByRole("ADMIN");
    }
    
    // Şifre güvenlik kontrolü
    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>?/";
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (specialChars.indexOf(c) >= 0) hasSpecial = true;
        }
        
        if (!hasUpperCase) return "Password must contain at least one uppercase letter";
        if (!hasLowerCase) return "Password must contain at least one lowercase letter";
        if (!hasDigit) return "Password must contain at least one digit";
        if (!hasSpecial) return "Password must contain at least one special character (!@#$%^&*...)";
        
        return "VALID";
    }
    
    // Şifre gereksinimlerini kontrol et (her şart için boolean)
    public static class PasswordStrength {
        public boolean hasMinLength;
        public boolean hasUpperCase;
        public boolean hasLowerCase;
        public boolean hasDigit;
        public boolean hasSpecial;
        
        public PasswordStrength(String password) {
            if (password == null) {
                password = "";
            }
            
            this.hasMinLength = password.length() >= 8;
            this.hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
            this.hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
            this.hasDigit = password.chars().anyMatch(Character::isDigit);
            
            String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>?/";
            this.hasSpecial = password.chars().anyMatch(c -> specialChars.indexOf(c) >= 0);
        }
        
        public boolean isValid() {
            return hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecial;
        }
    }
}
