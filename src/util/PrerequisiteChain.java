package util;

import model.Course;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * PrerequisiteChain - LinkedList veri yapısı kullanarak ön koşul zinciri yönetimi
 * 
 * Bu sınıf, derslerin ön koşul ilişkilerini LinkedList veri yapısı ile yönetir.
 * LinkedList, sıralı erişim ve dinamik ekleme/silme işlemleri için uygundur.
 * 
 * LinkedList kullanım avantajları:
 * - O(1) ekleme/silme (başa veya sona)
 * - Sıralı iterasyon
 * - Dinamik boyut
 * - Zincir yapısı için ideal
 * 
 * @author Course Registration System Team
 */
public class PrerequisiteChain {
    
    /**
     * Ön koşul zinciri düğümü
     */
    public static class PrerequisiteNode {
        public Course course;
        public int depth;  // Zincirdeki derinlik seviyesi
        public boolean isCompleted;  // Tamamlandı mı?
        
        public PrerequisiteNode(Course course, int depth, boolean isCompleted) {
            this.course = course;
            this.depth = depth;
            this.isCompleted = isCompleted;
        }
        
        @Override
        public String toString() {
            String status = isCompleted ? "✅" : "❌";
            return status + " [Level " + depth + "] " + course.getCode() + " - " + course.getName();
        }
    }
    
    // LinkedList - Ön koşul zinciri
    private LinkedList<PrerequisiteNode> chain;
    
    // Hedef ders
    private Course targetCourse;
    
    // Toplam tamamlanan/tamamlanmayan sayısı
    private int completedCount;
    private int pendingCount;
    
    public PrerequisiteChain(Course targetCourse) {
        this.chain = new LinkedList<>();
        this.targetCourse = targetCourse;
        this.completedCount = 0;
        this.pendingCount = 0;
    }
    
    /**
     * Zincirin başına ön koşul ekle (O(1))
     * @param course Ön koşul dersi
     * @param depth Derinlik seviyesi
     * @param isCompleted Tamamlandı mı
     */
    public void addFirst(Course course, int depth, boolean isCompleted) {
        PrerequisiteNode node = new PrerequisiteNode(course, depth, isCompleted);
        chain.addFirst(node);
        updateCounts(isCompleted);
    }
    
    /**
     * Zincirin sonuna ön koşul ekle (O(1))
     * @param course Ön koşul dersi
     * @param depth Derinlik seviyesi
     * @param isCompleted Tamamlandı mı
     */
    public void addLast(Course course, int depth, boolean isCompleted) {
        PrerequisiteNode node = new PrerequisiteNode(course, depth, isCompleted);
        chain.addLast(node);
        updateCounts(isCompleted);
    }
    
    /**
     * Belirli bir pozisyona ekle
     * @param index Pozisyon
     * @param course Ön koşul dersi
     * @param depth Derinlik seviyesi
     * @param isCompleted Tamamlandı mı
     */
    public void addAt(int index, Course course, int depth, boolean isCompleted) {
        PrerequisiteNode node = new PrerequisiteNode(course, depth, isCompleted);
        chain.add(index, node);
        updateCounts(isCompleted);
    }
    
    private void updateCounts(boolean isCompleted) {
        if (isCompleted) {
            completedCount++;
        } else {
            pendingCount++;
        }
    }
    
    /**
     * İlk elemanı al ve sil (O(1))
     * @return İlk ön koşul
     */
    public PrerequisiteNode pollFirst() {
        return chain.pollFirst();
    }
    
    /**
     * Son elemanı al ve sil (O(1))
     * @return Son ön koşul
     */
    public PrerequisiteNode pollLast() {
        return chain.pollLast();
    }
    
    /**
     * İlk elemana bak (silmeden)
     * @return İlk ön koşul
     */
    public PrerequisiteNode peekFirst() {
        return chain.peekFirst();
    }
    
    /**
     * Son elemana bak (silmeden)
     * @return Son ön koşul
     */
    public PrerequisiteNode peekLast() {
        return chain.peekLast();
    }
    
    /**
     * Zincirin tamamını döndür
     * @return LinkedList
     */
    public LinkedList<PrerequisiteNode> getChain() {
        return chain;
    }
    
    /**
     * Zincir boş mu?
     * @return true eğer boşsa
     */
    public boolean isEmpty() {
        return chain.isEmpty();
    }
    
    /**
     * Zincir boyutu
     * @return Eleman sayısı
     */
    public int size() {
        return chain.size();
    }
    
    /**
     * Tüm ön koşullar tamamlandı mı?
     * @return true eğer tüm ön koşullar tamamlandıysa
     */
    public boolean allCompleted() {
        for (PrerequisiteNode node : chain) {
            if (!node.isCompleted) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tamamlanmayan ön koşulları listele
     * @return Eksik ön koşullar
     */
    public List<PrerequisiteNode> getPendingPrerequisites() {
        List<PrerequisiteNode> pending = new ArrayList<>();
        for (PrerequisiteNode node : chain) {
            if (!node.isCompleted) {
                pending.add(node);
            }
        }
        return pending;
    }
    
    /**
     * Tamamlanan ön koşulları listele
     * @return Tamamlanan ön koşullar
     */
    public List<PrerequisiteNode> getCompletedPrerequisites() {
        List<PrerequisiteNode> completed = new ArrayList<>();
        for (PrerequisiteNode node : chain) {
            if (node.isCompleted) {
                completed.add(node);
            }
        }
        return completed;
    }
    
    /**
     * Hedef ders
     * @return Hedef ders
     */
    public Course getTargetCourse() {
        return targetCourse;
    }
    
    /**
     * Tamamlanma yüzdesi
     * @return Yüzde (0-100)
     */
    public double getCompletionPercentage() {
        if (chain.isEmpty()) return 100.0;
        return (completedCount * 100.0) / chain.size();
    }
    
    /**
     * İki zinciri birleştir
     * @param other Birleştirilecek diğer zincir
     */
    public void merge(PrerequisiteChain other) {
        for (PrerequisiteNode node : other.getChain()) {
            this.addLast(node.course, node.depth, node.isCompleted);
        }
    }
    
    /**
     * Zinciri ters çevir
     */
    public void reverse() {
        LinkedList<PrerequisiteNode> reversed = new LinkedList<>();
        while (!chain.isEmpty()) {
            reversed.addFirst(chain.pollFirst());
        }
        chain = reversed;
    }
    
    /**
     * Belirli bir dersi zincirde ara
     * @param courseCode Aranacak ders kodu
     * @return Bulunan node veya null
     */
    public PrerequisiteNode findByCourseCode(String courseCode) {
        for (PrerequisiteNode node : chain) {
            if (node.course.getCode().equals(courseCode)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Döngüsel bağımlılık kontrolü
     * @param visited Ziyaret edilen dersler
     * @return true eğer döngü varsa
     */
    public boolean hasCycle(Set<String> visited) {
        for (PrerequisiteNode node : chain) {
            if (visited.contains(node.course.getCode())) {
                return true;
            }
            visited.add(node.course.getCode());
        }
        return false;
    }
    
    /**
     * Derinliğe göre grupla
     * @return Derinlik bazlı gruplandırılmış liste
     */
    public List<List<PrerequisiteNode>> groupByDepth() {
        List<List<PrerequisiteNode>> groups = new ArrayList<>();
        int maxDepth = 0;
        
        // Maksimum derinliği bul
        for (PrerequisiteNode node : chain) {
            maxDepth = Math.max(maxDepth, node.depth);
        }
        
        // Grupları oluştur
        for (int i = 0; i <= maxDepth; i++) {
            groups.add(new ArrayList<>());
        }
        
        // Düğümleri grupla
        for (PrerequisiteNode node : chain) {
            groups.get(node.depth).add(node);
        }
        
        return groups;
    }
}
