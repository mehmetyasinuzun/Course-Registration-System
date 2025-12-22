package util;

import java.util.Stack;

/**
 * NavigationManager - Stack veri yapısı kullanarak sayfa geçmişi yönetimi
 * 
 * Bu sınıf, kullanıcıların uygulama içinde gezinirken sayfa geçmişini 
 * takip etmek için Stack (LIFO - Last In First Out) veri yapısını kullanır.
 * 
 * Stack kullanım mantığı:
 * - Her yeni sayfaya gidildiğinde push() ile stack'e eklenir
 * - Geri dönmek istendiğinde pop() ile son sayfa çıkarılır
 * - peek() ile mevcut sayfa görüntülenir
 * 
 * @author Course Registration System Team
 */
public class NavigationManager {
    
    // Stack veri yapısı - LIFO (Last In First Out) prensibi
    private static Stack<String> pageHistory = new Stack<>();
    
    // Maksimum geçmiş boyutu (bellek optimizasyonu)
    private static final int MAX_HISTORY_SIZE = 50;
    
    // Son ziyaret edilen sayfa bilgisi
    private static String currentPage = "Dashboard";
    
    /**
     * Yeni bir sayfaya git ve stack'e ekle
     * @param pageName Gidilecek sayfa adı
     */
    public static void navigateTo(String pageName) {
        // Aynı sayfaya tekrar gitmemek için kontrol
        if (currentPage != null && currentPage.equals(pageName)) {
            return;
        }
        
        // Mevcut sayfayı stack'e ekle
        if (currentPage != null) {
            pageHistory.push(currentPage);
        }
        
        // Geçmiş çok büyükse en eski kayıtları sil
        while (pageHistory.size() > MAX_HISTORY_SIZE) {
            // Stack'in en altındaki elemanı kaldır
            Stack<String> temp = new Stack<>();
            while (pageHistory.size() > 1) {
                temp.push(pageHistory.pop());
            }
            pageHistory.pop(); // En eski elemanı kaldır
            while (!temp.isEmpty()) {
                pageHistory.push(temp.pop());
            }
        }
        
        currentPage = pageName;
    }
    
    /**
     * Bir önceki sayfaya dön (Stack'ten pop)
     * @return Önceki sayfa adı, yoksa "Dashboard"
     */
    public static String goBack() {
        if (!pageHistory.isEmpty()) {
            currentPage = pageHistory.pop();
            return currentPage;
        }
        currentPage = "Dashboard";
        return "Dashboard";
    }
    
    /**
     * Mevcut sayfayı döndür (Stack'in tepesine bakmadan)
     * @return Mevcut sayfa adı
     */
    public static String getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Bir önceki sayfayı göster (stack'ten çıkarmadan)
     * @return Önceki sayfa adı, yoksa null
     */
    public static String peekPreviousPage() {
        if (!pageHistory.isEmpty()) {
            return pageHistory.peek();
        }
        return null;
    }
    
    /**
     * Geri dönülebilecek sayfa var mı?
     * @return true eğer geçmişte sayfa varsa
     */
    public static boolean canGoBack() {
        return !pageHistory.isEmpty();
    }
    
    /**
     * Navigasyon geçmişini temizle
     */
    public static void clearHistory() {
        pageHistory.clear();
        currentPage = "Dashboard";
    }
    
    /**
     * Geçmişteki sayfa sayısını döndür
     * @return Stack'teki sayfa sayısı
     */
    public static int getHistorySize() {
        return pageHistory.size();
    }
    
    /**
     * Belirli bir sayfaya kadar geri dön
     * @param targetPage Hedef sayfa adı
     * @return true eğer sayfa bulunduysa
     */
    public static boolean goBackTo(String targetPage) {
        while (!pageHistory.isEmpty()) {
            String page = pageHistory.pop();
            if (page.equals(targetPage)) {
                currentPage = page;
                return true;
            }
        }
        // Bulunamadı, Dashboard'a dön
        currentPage = "Dashboard";
        return false;
    }
}
