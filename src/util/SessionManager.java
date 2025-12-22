package util;

import model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * SessionManager - HashMap veri yapısı kullanarak kullanıcı oturum yönetimi
 * 
 * Bu sınıf, kullanıcı oturumlarını ve cache'lenmiş kullanıcı bilgilerini
 * HashMap veri yapısı ile yönetir. HashMap O(1) zaman karmaşıklığı sağlar.
 * 
 * HashMap kullanım avantajları:
 * - O(1) arama/ekleme/silme işlemleri
 * - Key-Value çiftleri ile hızlı erişim
 * - Kullanıcı ID'si ile doğrudan erişim
 * 
 * @author Course Registration System Team
 */
public class SessionManager {
    
    // HashMap: Kullanıcı ID -> User nesnesi (cache)
    private static HashMap<Integer, User> userCache = new HashMap<>();
    
    // HashMap: Kullanıcı adı -> Kullanıcı ID (hızlı lookup)
    private static HashMap<String, Integer> usernameToIdMap = new HashMap<>();
    
    // HashMap: Session token -> User ID (oturum yönetimi)
    private static HashMap<String, Integer> activeSessions = new HashMap<>();
    
    // Mevcut oturum açmış kullanıcı
    private static User currentUser = null;
    private static String currentSessionToken = null;
    
    /**
     * Kullanıcıyı cache'e ekle
     * @param user Eklenecek kullanıcı
     */
    public static void cacheUser(User user) {
        if (user != null) {
            userCache.put(user.getId(), user);
            usernameToIdMap.put(user.getUsername(), user.getId());
        }
    }
    
    /**
     * Cache'ten kullanıcı getir (ID ile) - O(1)
     * @param userId Kullanıcı ID
     * @return User nesnesi veya null
     */
    public static User getCachedUser(int userId) {
        return userCache.get(userId);
    }
    
    /**
     * Cache'ten kullanıcı getir (username ile) - O(1)
     * @param username Kullanıcı adı
     * @return User nesnesi veya null
     */
    public static User getCachedUserByUsername(String username) {
        Integer userId = usernameToIdMap.get(username);
        if (userId != null) {
            return getCachedUser(userId);
        }
        return null;
    }
    
    /**
     * Kullanıcı cache'te var mı kontrol et - O(1)
     * @param userId Kullanıcı ID
     * @return true eğer cache'te varsa
     */
    public static boolean isUserCached(int userId) {
        return userCache.containsKey(userId);
    }
    
    /**
     * Cache'ten kullanıcı sil
     * @param userId Silinecek kullanıcı ID
     */
    public static void removeCachedUser(int userId) {
        User user = userCache.remove(userId);
        if (user != null) {
            usernameToIdMap.remove(user.getUsername());
        }
    }
    
    /**
     * Tüm cache'i temizle
     */
    public static void clearCache() {
        userCache.clear();
        usernameToIdMap.clear();
    }
    
    /**
     * Cache boyutunu döndür
     * @return Cache'teki kullanıcı sayısı
     */
    public static int getCacheSize() {
        return userCache.size();
    }
    
    /**
     * Oturum aç
     * @param user Giriş yapan kullanıcı
     * @return Session token
     */
    public static String login(User user) {
        if (user == null) return null;
        
        // Kullanıcıyı cache'e ekle
        cacheUser(user);
        
        // Session token oluştur
        String token = generateSessionToken(user);
        activeSessions.put(token, user.getId());
        
        currentUser = user;
        currentSessionToken = token;
        
        return token;
    }
    
    /**
     * Oturumu kapat
     */
    public static void logout() {
        if (currentSessionToken != null) {
            activeSessions.remove(currentSessionToken);
        }
        currentUser = null;
        currentSessionToken = null;
        NavigationManager.clearHistory();
    }
    
    /**
     * Mevcut oturumdaki kullanıcıyı döndür
     * @return Mevcut kullanıcı veya null
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Oturum aktif mi kontrol et
     * @param token Session token
     * @return true eğer oturum aktifse
     */
    public static boolean isSessionValid(String token) {
        return activeSessions.containsKey(token);
    }
    
    /**
     * Aktif oturum sayısını döndür
     * @return Aktif oturum sayısı
     */
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * Session token oluştur
     */
    private static String generateSessionToken(User user) {
        return "SESSION_" + user.getId() + "_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int)(Math.random() * 0xFFFF));
    }
    
    /**
     * Tüm cache'lenmiş kullanıcıları döndür
     * @return Kullanıcı map'i
     */
    public static Map<Integer, User> getAllCachedUsers() {
        return new HashMap<>(userCache);
    }
}
