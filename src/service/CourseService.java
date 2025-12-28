package service;

import repository.CourseRepository;
import repository.RegistrationRepository;
import model.Course;
import model.Registration;
import util.CourseTree;
import util.PrerequisiteChain;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

public class CourseService {
    
    private CourseRepository courseRepo;
    private RegistrationRepository regRepo;
    
    // ===== HASHMAP: Ders kodu ile O(1) hızlı erişim =====
    private Map<String, Course> courseCodeCache = new HashMap<>();
    private Map<Integer, Course> courseIdCache = new HashMap<>();
    
    // ===== BINARY SEARCH TREE: Sıralı ders erişimi =====
    private CourseTree courseBST = new CourseTree();
    
    // Cache durumu
    private boolean cacheInitialized = false;
    
    public CourseService() {
        this.courseRepo = new CourseRepository();
        this.regRepo = new RegistrationRepository();
        initializeCache();  // Cache'i başlat
    }
    
    // ===== HASHMAP CACHE YÖNETİMİ =====
    
    /**
     * HashMap cache'i başlat - Tüm dersleri yükle
     * Bu işlem uygulama başlangıcında bir kez yapılır
     */
    private void initializeCache() {
        if (cacheInitialized) return;
        
        List<Course> allCourses = courseRepo.findAll();
        for (Course course : allCourses) {
            // HashMap'e ekle - O(1)
            courseCodeCache.put(course.getCode(), course);
            courseIdCache.put(course.getId(), course);
            
            // BST'ye ekle - O(log n)
            courseBST.insert(course);
        }
        
        cacheInitialized = true;
    }
    
    /**
     * Cache'i yenile (ders eklendiğinde/silindiğinde/güncellendiğinde)
     */
    public void refreshCache() {
        courseCodeCache.clear();
        courseIdCache.clear();
        courseBST = new CourseTree();
        cacheInitialized = false;
        initializeCache();
    }
    
    /**
     * HashMap ile O(1) ders kodu araması
     * @param code Ders kodu
     * @return Course veya null
     */
    public Course getCourseByCodeFast(String code) {
        Course course = courseCodeCache.get(code);
        if (course == null) {
            course = courseRepo.findByCode(code);
            if (course != null) {
                courseCodeCache.put(code, course);
                courseIdCache.put(course.getId(), course);
            }
        }
        return course;
    }
    
    /**
     * HashMap ile O(1) ders ID araması
     * @param id Ders ID
     * @return Course veya null
     */
    public Course getCourseByIdFast(int id) {
        Course course = courseIdCache.get(id);
        if (course == null) {
            course = courseRepo.findById(id);
            if (course != null) {
                courseCodeCache.put(course.getCode(), course);
                courseIdCache.put(course.getId(), course);
            }
        }
        return course;
    }
    
    /**
     * Cache boyutunu döndür
     */
    public int getCacheSize() {
        return courseCodeCache.size();
    }
    
    // ===== BST KULLANIMI =====
    
    /**
     * BST ile sıralı ders listesi (alfabetik)
     * @return Sıralı ders listesi
     */
    public List<Course> getCoursesAlphabetically() {
        return courseBST.inorderTraversal();
    }
    
    /**
     * BST ile ders arama
     * @param code Ders kodu
     * @return Course veya null
     */
    public Course searchInBST(String code) {
        return courseBST.search(code);
    }
    
    // ===== LINKEDLIST: ÖN KOŞUL ZİNCİRİ =====
    
    /**
     * LinkedList kullanarak ön koşul zincirini oluştur
     * @param courseId Ders ID
     * @param studentId Öğrenci ID (tamamlama durumu için)
     * @return PrerequisiteChain
     */
    public PrerequisiteChain buildPrerequisiteChain(int courseId, int studentId) {
        Course targetCourse = getCourseByIdFast(courseId);
        if (targetCourse == null) return null;
        
        PrerequisiteChain chain = new PrerequisiteChain(targetCourse);
        buildChainRecursive(courseId, studentId, chain, 0, new java.util.HashSet<>());
        
        return chain;
    }
    
    private void buildChainRecursive(int courseId, int studentId, PrerequisiteChain chain, 
                                      int depth, java.util.Set<Integer> visited) {
        if (visited.contains(courseId)) return;  // Döngü önleme
        visited.add(courseId);
        
        List<Course> prerequisites = courseRepo.getPrerequisites(courseId);
        for (Course prereq : prerequisites) {
            boolean completed = regRepo.hasCompletedCourse(studentId, prereq.getId());
            chain.addLast(prereq, depth, completed);
            
            // Alt ön koşulları da ekle (recursive)
            buildChainRecursive(prereq.getId(), studentId, chain, depth + 1, visited);
        }
    }
    
    // Ders ekle
    public int addCourse(Course course) {
        // Ders kodu kontrolü - HashMap ile O(1)
        if (courseCodeCache.containsKey(course.getCode())) {
            return -1;
        }
        
        int id = courseRepo.insert(course);
        if (id > 0) {
            course.setId(id);
            // Cache'e ekle
            courseCodeCache.put(course.getCode(), course);
            courseIdCache.put(id, course);
            courseBST.insert(course);
        }
        return id;
    }
    
    // Ders güncelle
    public boolean updateCourse(Course course) {
        boolean success = courseRepo.update(course);
        if (success) {
            // Cache'i güncelle
            courseCodeCache.put(course.getCode(), course);
            courseIdCache.put(course.getId(), course);
        }
        return success;
    }
    
    // Ders sil
    public boolean deleteCourse(int courseId) {
        Course course = courseIdCache.get(courseId);
        boolean success = courseRepo.delete(courseId);
        if (success && course != null) {
            // Cache'ten sil
            courseCodeCache.remove(course.getCode());
            courseIdCache.remove(courseId);
            courseBST.delete(course.getCode());
        }
        return success;
    }
    
    // Tüm dersleri getir
    public List<Course> getAllCourses() {
        // Cache'ten döndür (daha hızlı)
        if (!courseIdCache.isEmpty()) {
            return new ArrayList<>(courseIdCache.values());
        }
        return courseRepo.findAll();
    }
    
    // ID ile ders getir - HashMap O(1)
    public Course getCourseById(int id) {
        return getCourseByIdFast(id);
    }
    
    // Kod ile ders getir - HashMap O(1)
    public Course getCourseByCode(String code) {
        return getCourseByCodeFast(code);
    }
    
    // Hocanın derslerini getir
    public List<Course> getCoursesByInstructor(int instructorId) {
        return courseRepo.findByInstructorId(instructorId);
    }
    
    // Ön koşul ekle
    public boolean addPrerequisite(int courseId, int prerequisiteId) {
        return courseRepo.addPrerequisite(courseId, prerequisiteId);
    }
    
    // Ön koşul sil
    public boolean removePrerequisite(int courseId, int prerequisiteId) {
        return courseRepo.removePrerequisite(courseId, prerequisiteId);
    }
    
    // Ön koşulları getir
    public List<Course> getPrerequisites(int courseId) {
        return courseRepo.getPrerequisites(courseId);
    }
    
    // Derse kayıtlı öğrenci sayısı
    public int getEnrollmentCount(int courseId) {
        return courseRepo.getEnrollmentCount(courseId);
    }
    
    // Derste yer var mı?
    public boolean hasAvailableCapacity(int courseId) {
        Course course = courseRepo.findById(courseId);
        if (course == null) return false;
        
        int enrolled = getEnrollmentCount(courseId);
        return enrolled < course.getCapacity();
    }
    
    // Öğrencinin kayıtlı derslerini getir (sadece onaylı)
    public List<Course> getStudentCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        List<Registration> regs = regRepo.findApprovedByStudentId(studentId);
        
        for (Registration reg : regs) {
            Course course = courseRepo.findById(reg.getCourseId());
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }
    
    // Öğrencinin bekleyen veya onaylı tüm derslerini getir (çakışma kontrolü için)
    public List<Course> getStudentPendingAndApprovedCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        List<Registration> regs = regRepo.findByStudentId(studentId);
        
        for (Registration reg : regs) {
            // Sadece PENDING veya APPROVED olanları al
            if ("PENDING".equals(reg.getStatus()) || "APPROVED".equals(reg.getStatus())) {
                Course course = courseRepo.findById(reg.getCourseId());
                if (course != null) {
                    courses.add(course);
                }
            }
        }
        return courses;
    }
    
    // Ders çakışması kontrolü (hem PENDING hem APPROVED kayıtlar kontrol edilir)
    public Course checkTimeConflict(int studentId, int newCourseId) {
        Course newCourse = courseRepo.findById(newCourseId);
        if (newCourse == null) return null;
        
        // Hem bekleyen hem onaylı kayıtları kontrol et
        List<Course> studentCourses = getStudentPendingAndApprovedCourses(studentId);
        
        for (Course existing : studentCourses) {
            if (hasTimeConflict(newCourse, existing)) {
                return existing; // Çakışan ders
            }
        }
        return null; // Çakışma yok
    }
    
    // İki ders arasında saat çakışması var mı?
    private boolean hasTimeConflict(Course c1, Course c2) {
        // Farklı günlerde ise çakışma yok
        if (!c1.getDayOfWeek().equals(c2.getDayOfWeek())) {
            return false;
        }
        
        // Aynı gün, saat kontrolü
        int start1 = timeToMinutes(c1.getStartTime());
        int end1 = timeToMinutes(c1.getEndTime());
        int start2 = timeToMinutes(c2.getStartTime());
        int end2 = timeToMinutes(c2.getEndTime());
        
        // Çakışma: biri diğerinin içinde başlıyor
        return (start1 < end2 && end1 > start2);
    }
    
    // "09:30" -> 570 dakika
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }
    
    // Dönem bazlı dersleri getir
    public List<Course> getCoursesBySemester(int semester) {
        return courseRepo.findBySemester(semester);
    }
    
    // Hocanın aynı gün ve saatte başka dersi var mı kontrol et
    public Course checkInstructorTimeConflict(int instructorId, String dayOfWeek, String startTime, String endTime) {
        List<Course> instructorCourses = courseRepo.findByInstructorId(instructorId);
        
        for (Course existing : instructorCourses) {
            // Farklı günse sorun yok
            if (!existing.getDayOfWeek().equals(dayOfWeek)) {
                continue;
            }
            
            // Aynı gün, saat kontrolü
            int newStart = timeToMinutes(startTime);
            int newEnd = timeToMinutes(endTime);
            int existingStart = timeToMinutes(existing.getStartTime());
            int existingEnd = timeToMinutes(existing.getEndTime());
            
            // Çakışma var mı?
            if (newStart < existingEnd && newEnd > existingStart) {
                return existing; // Çakışan ders bulundu
            }
        }
        return null; // Çakışma yok
    }
    
    // Hocanın aynı gün ve saatte başka dersi var mı (güncelleme için - kendi dersini hariç tut)
    public Course checkInstructorTimeConflictForUpdate(int courseId, int instructorId, String dayOfWeek, String startTime, String endTime) {
        List<Course> instructorCourses = courseRepo.findByInstructorId(instructorId);
        
        for (Course existing : instructorCourses) {
            // Güncellenen dersin kendisini atla
            if (existing.getId() == courseId) {
                continue;
            }
            
            // Farklı günse sorun yok
            if (!existing.getDayOfWeek().equals(dayOfWeek)) {
                continue;
            }
            
            // Aynı gün, saat kontrolü
            int newStart = timeToMinutes(startTime);
            int newEnd = timeToMinutes(endTime);
            int existingStart = timeToMinutes(existing.getStartTime());
            int existingEnd = timeToMinutes(existing.getEndTime());
            
            // Çakışma var mı?
            if (newStart < existingEnd && newEnd > existingStart) {
                return existing; // Çakışan ders bulundu
            }
        }
        return null; // Çakışma yok
    }
}
