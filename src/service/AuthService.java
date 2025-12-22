package service;

import repository.UserRepository;
import model.User;
import util.SessionManager;
import util.NavigationManager;

public class AuthService {
    
    private UserRepository userRepo;
    
    public AuthService() {
        this.userRepo = new UserRepository();
    }
    
    // Giriş yap
    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        
        if (user == null) {
            return null;
        }
        
        if (!user.getPassword().equals(password)) {
            return null;
        }
        
        SessionManager.login(user);
        NavigationManager.clearHistory();
        
        return user;
    }
    
    // Çıkış yap
    public void logout() {
        SessionManager.logout();
    }
    
    // Mevcut kullanıcıyı getir (SessionManager cache'ten)
    public User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }
    
    // Username kullanılıyor mu?
    public boolean isUsernameTaken(String username) {
        // Önce cache'e bak
        if (SessionManager.getCachedUserByUsername(username) != null) {
            return true;
        }
        return userRepo.findByUsername(username) != null;
    }
}
