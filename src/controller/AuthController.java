package controller;

import service.AuthService;
import model.User;

public class AuthController {
    
    private AuthService authService;
    private static User currentUser = null;
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    // Giriş yap
    public User login(String username, String password) {
        User user = authService.login(username, password);
        if (user != null) {
            currentUser = user;
        }
        return user;
    }
    
    // Çıkış yap
    public void logout() {
        currentUser = null;
    }
    
    // Şu anki kullanıcı
    public static User getCurrentUser() {
        return currentUser;
    }
    
    // Giriş yapılmış mı?
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    // Kullanıcı rolü
    public static String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}
