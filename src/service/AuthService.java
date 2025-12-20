package service;

import repository.UserRepository;
import model.User;

public class AuthService {
    
    private UserRepository userRepo;
    
    public AuthService() {
        this.userRepo = new UserRepository();
    }
    
    // Giriş yap
    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        
        if (user == null) {
            return null; // Kullanıcı bulunamadı
        }
        
        if (!user.getPassword().equals(password)) {
            return null; // Şifre yanlış
        }
        
        return user; // Başarılı giriş
    }
    
    // Username kullanılıyor mu?
    public boolean isUsernameTaken(String username) {
        return userRepo.findByUsername(username) != null;
    }
}
