package service;

import repository.UserRepository;
import repository.CourseCatalogRepository;
import repository.RegistrationRepository;
import model.*;
import util.SessionManager;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    
    private UserRepository userRepo;
    private CourseCatalogRepository catalogRepo;
    private RegistrationRepository regRepo;
    
    // ===== HASHMAP: Kullanıcı cache =====
    private static Map<Integer, User> userIdCache = new HashMap<>();
    private static Map<String, User> usernameCache = new HashMap<>();
    
    public UserService() {
        this.userRepo = new UserRepository();
        this.catalogRepo = new CourseCatalogRepository();
        this.regRepo = new RegistrationRepository();
    }
    
    // ===== HASHMAP CACHE YÖNETİMİ =====
    
    /**
     * Kullanıcıyı cache'e ekle
     */
    private void cacheUser(User user) {
        if (user != null) {
            userIdCache.put(user.getId(), user);
            usernameCache.put(user.getUsername(), user);
            SessionManager.cacheUser(user);
        }
    }
    
    /**
     * Cache'ten kullanıcı al (ID ile) - O(1)
     */
    public User getUserByIdFast(int id) {
        User user = userIdCache.get(id);
        if (user != null) {
            return user;
        }
        
        user = SessionManager.getCachedUser(id);
        if (user != null) {
            userIdCache.put(id, user);
            usernameCache.put(user.getUsername(), user);
            return user;
        }
        
        user = userRepo.findById(id);
        if (user != null) {
            cacheUser(user);
        }
        return user;
    }
    
    /**
     * Cache'ten kullanıcı al (username ile) - O(1)
     */
    public User getUserByUsernameFast(String username) {
        User user = usernameCache.get(username);
        if (user != null) {
            return user;
        }
        
        user = userRepo.findByUsername(username);
        if (user != null) {
            cacheUser(user);
        }
        return user;
    }
    
    /**
     * Cache'i temizle
     */
    public void clearCache() {
        userIdCache.clear();
        usernameCache.clear();
    }
    
    /**
     * Kullanıcıyı cache'ten kaldır
     */
    public void invalidateUserCache(int userId) {
        User user = userIdCache.remove(userId);
        if (user != null) {
            usernameCache.remove(user.getUsername());
        }
    }
    
    /**
     * Cache boyutu
     */
    public int getCacheSize() {
        return userIdCache.size();
    }
    
    // ===== MEVCUT METODLAR (Güncellendi) =====
    
    // Öğrenci ekle (sınıf ve dönem ile)
    public int addStudent(String username, String password, String fullName, String email, String studentNumber, int year, int semester) {
        // Username kontrolü - HashMap ile O(1)
        if (usernameCache.containsKey(username) || userRepo.findByUsername(username) != null) {
            System.out.println("Bu kullanıcı adı zaten kullanılıyor!");
            return -1;
        }
        
        Student student = new Student(username, password, fullName, email, studentNumber, year, semester);
        int studentId = userRepo.insert(student);
        
        // Öğrenci başarıyla eklenirse, bulunduğu döneme kadar tüm dersleri completed olarak ekle
        if (studentId > 0) {
            student.setId(studentId);
            cacheUser(student);  // Cache'e ekle
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
        // Username kontrolü - HashMap ile O(1)
        if (usernameCache.containsKey(username) || userRepo.findByUsername(username) != null) {
            System.out.println("Bu kullanıcı adı zaten kullanılıyor!");
            return -1;
        }
        
        Instructor instructor = new Instructor(username, password, fullName, email);
        int instructorId = userRepo.insert(instructor);
        
        if (instructorId > 0) {
            instructor.setId(instructorId);
            cacheUser(instructor);  // Cache'e ekle
        }
        
        return instructorId;
    }
    
    // Kullanıcı güncelle
    public boolean updateUser(User user) {
        boolean success = userRepo.update(user);
        if (success) {
            cacheUser(user);  // Cache'i güncelle
        }
        return success;
    }
    
    // Kullanıcı sil
    public boolean deleteUser(int userId) {
        boolean success = userRepo.delete(userId);
        if (success) {
            invalidateUserCache(userId);  // Cache'ten sil
        }
        return success;
    }
    
    // ID ile kullanıcı getir - HashMap O(1)
    public User getUserById(int id) {
        return getUserByIdFast(id);
    }
    
    // Username ile kullanıcı getir - HashMap O(1)
    public User getUserByUsername(String username) {
        return getUserByUsernameFast(username);
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
