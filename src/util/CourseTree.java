package util;

import model.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

/**
 * CourseTree - Binary Search Tree ve Tree veri yapısı implementasyonu
 * 
 * Bu sınıf, dersleri ders kodu (code) bazında organize eden bir 
 * Binary Search Tree yapısı sağlar. BST, sıralı veri erişimi ve
 * O(log n) arama performansı sunar.
 * 
 * Ayrıca ders hiyerarşisi (yıl/dönem bazlı) için Tree yapısı kullanır.
 * 
 * BST Özellikleri:
 * - Sol alt ağaç: Küçük değerler
 * - Sağ alt ağaç: Büyük değerler
 * - Inorder traversal: Sıralı liste döndürür
 * 
 * @author Course Registration System Team
 */
public class CourseTree {
    
    // ============= BINARY SEARCH TREE IMPLEMENTASYONU =============
    
    /**
     * BST Node sınıfı
     */
    private static class BSTNode {
        Course course;
        BSTNode left;
        BSTNode right;
        
        BSTNode(Course course) {
            this.course = course;
            this.left = null;
            this.right = null;
        }
    }
    
    private BSTNode root;
    private int size;
    
    public CourseTree() {
        this.root = null;
        this.size = 0;
    }
    
    /**
     * Ders ekle (BST kurallarına göre)
     * Ders kodu alfabetik sıralamaya göre yerleştirilir
     * @param course Eklenecek ders
     */
    public void insert(Course course) {
        root = insertRecursive(root, course);
        size++;
    }
    
    private BSTNode insertRecursive(BSTNode node, Course course) {
        if (node == null) {
            return new BSTNode(course);
        }
        
        int comparison = course.getCode().compareTo(node.course.getCode());
        
        if (comparison < 0) {
            node.left = insertRecursive(node.left, course);
        } else if (comparison > 0) {
            node.right = insertRecursive(node.right, course);
        }
        // Eşitse güncelleme yapma (duplicate)
        
        return node;
    }
    
    /**
     * Ders ara (O(log n) - ortalama)
     * @param code Aranacak ders kodu
     * @return Bulunan ders veya null
     */
    public Course search(String code) {
        return searchRecursive(root, code);
    }
    
    private Course searchRecursive(BSTNode node, String code) {
        if (node == null) {
            return null;
        }
        
        int comparison = code.compareTo(node.course.getCode());
        
        if (comparison == 0) {
            return node.course;
        } else if (comparison < 0) {
            return searchRecursive(node.left, code);
        } else {
            return searchRecursive(node.right, code);
        }
    }
    
    /**
     * Ders sil
     * @param code Silinecek ders kodu
     * @return true eğer silindiyse
     */
    public boolean delete(String code) {
        int oldSize = size;
        root = deleteRecursive(root, code);
        return size < oldSize;
    }
    
    private BSTNode deleteRecursive(BSTNode node, String code) {
        if (node == null) {
            return null;
        }
        
        int comparison = code.compareTo(node.course.getCode());
        
        if (comparison < 0) {
            node.left = deleteRecursive(node.left, code);
        } else if (comparison > 0) {
            node.right = deleteRecursive(node.right, code);
        } else {
            // Silinecek düğüm bulundu
            size--;
            
            // Tek çocuk veya çocuksuz
            if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            }
            
            // İki çocuklu: Sağ alt ağacın en küçük değerini bul
            node.course = findMin(node.right).course;
            node.right = deleteRecursive(node.right, node.course.getCode());
        }
        
        return node;
    }
    
    private BSTNode findMin(BSTNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    
    /**
     * Inorder traversal - Alfabetik sıralı liste döndürür
     * @return Sıralı ders listesi
     */
    public List<Course> inorderTraversal() {
        List<Course> result = new ArrayList<>();
        inorderRecursive(root, result);
        return result;
    }
    
    private void inorderRecursive(BSTNode node, List<Course> result) {
        if (node != null) {
            inorderRecursive(node.left, result);
            result.add(node.course);
            inorderRecursive(node.right, result);
        }
    }
    
    /**
     * Preorder traversal
     * @return Preorder ders listesi
     */
    public List<Course> preorderTraversal() {
        List<Course> result = new ArrayList<>();
        preorderRecursive(root, result);
        return result;
    }
    
    private void preorderRecursive(BSTNode node, List<Course> result) {
        if (node != null) {
            result.add(node.course);
            preorderRecursive(node.left, result);
            preorderRecursive(node.right, result);
        }
    }
    
    /**
     * Level-order traversal (BFS) - Queue kullanır
     * @return Level-order ders listesi
     */
    public List<Course> levelOrderTraversal() {
        List<Course> result = new ArrayList<>();
        if (root == null) return result;
        
        // Queue kullanarak BFS
        Queue<BSTNode> queue = new LinkedList<>();
        queue.offer(root);
        
        while (!queue.isEmpty()) {
            BSTNode node = queue.poll();
            result.add(node.course);
            
            if (node.left != null) {
                queue.offer(node.left);
            }
            if (node.right != null) {
                queue.offer(node.right);
            }
        }
        
        return result;
    }
    
    /**
     * BST'nin yüksekliğini hesapla
     * @return Ağaç yüksekliği
     */
    public int getHeight() {
        return getHeightRecursive(root);
    }
    
    private int getHeightRecursive(BSTNode node) {
        if (node == null) {
            return 0;
        }
        int leftHeight = getHeightRecursive(node.left);
        int rightHeight = getHeightRecursive(node.right);
        return Math.max(leftHeight, rightHeight) + 1;
    }
    
    /**
     * BST boyutunu döndür
     * @return Toplam ders sayısı
     */
    public int getSize() {
        return size;
    }
    
    /**
     * BST boş mu?
     * @return true eğer boşsa
     */
    public boolean isEmpty() {
        return root == null;
    }
    
    /**
     * Belirli bir aralıktaki dersleri getir (range query)
     * @param startCode Başlangıç kodu
     * @param endCode Bitiş kodu
     * @return Aralıktaki dersler
     */
    public List<Course> getCoursesInRange(String startCode, String endCode) {
        List<Course> result = new ArrayList<>();
        rangeQueryRecursive(root, startCode, endCode, result);
        return result;
    }
    
    private void rangeQueryRecursive(BSTNode node, String startCode, String endCode, List<Course> result) {
        if (node == null) return;
        
        int startComp = node.course.getCode().compareTo(startCode);
        int endComp = node.course.getCode().compareTo(endCode);
        
        if (startComp > 0) {
            rangeQueryRecursive(node.left, startCode, endCode, result);
        }
        
        if (startComp >= 0 && endComp <= 0) {
            result.add(node.course);
        }
        
        if (endComp < 0) {
            rangeQueryRecursive(node.right, startCode, endCode, result);
        }
    }
    
    // ============= SEMESTER HIERARCHY TREE =============
    
    /**
     * Dönem bazlı ders hiyerarşisi node'u
     */
    public static class SemesterNode {
        public int semester;  // 1-8, 0 = all semesters
        public List<Course> courses;
        public SemesterNode nextSemester;  // LinkedList tarzı bağlantı
        
        public SemesterNode(int semester) {
            this.semester = semester;
            this.courses = new ArrayList<>();
            this.nextSemester = null;
        }
        
        public void addCourse(Course course) {
            courses.add(course);
        }
        
        public String getSemesterName() {
            if (semester == 0) return "All Semesters";
            int year = (semester + 1) / 2;
            int sem = (semester % 2 == 1) ? 1 : 2;
            return "Year " + year + " - Semester " + sem;
        }
    }
    
    // Dönem hiyerarşisi için LinkedList
    private SemesterNode semesterHead;
    
    /**
     * Dönem hiyerarşisini oluştur
     * @param courses Tüm dersler
     */
    public void buildSemesterHierarchy(List<Course> courses) {
        // 8 dönem + 0 (all semesters) için node'lar oluştur
        SemesterNode[] nodes = new SemesterNode[9];
        for (int i = 0; i <= 8; i++) {
            nodes[i] = new SemesterNode(i);
        }
        
        // Dersleri dönemlerine göre dağıt
        for (Course course : courses) {
            int sem = course.getSemester();
            if (sem >= 0 && sem <= 8) {
                nodes[sem].addCourse(course);
            }
        }
        
        // LinkedList bağlantılarını kur
        semesterHead = nodes[1]; // 1. dönemden başla
        for (int i = 1; i < 8; i++) {
            nodes[i].nextSemester = nodes[i + 1];
        }
        // 0 (all semesters) en sona
        nodes[8].nextSemester = nodes[0];
    }
    
    /**
     * Belirli bir dönemin derslerini getir
     * @param semester Dönem numarası (1-8)
     * @return Dönemdeki dersler
     */
    public List<Course> getCoursesBySemester(int semester) {
        SemesterNode current = semesterHead;
        while (current != null) {
            if (current.semester == semester) {
                return current.courses;
            }
            current = current.nextSemester;
        }
        return new ArrayList<>();
    }
}
