package service;

import repository.CourseRepository;
import repository.RegistrationRepository;
import model.Course;
import model.Registration;
import java.util.List;
import java.util.ArrayList;

public class CourseService {
    
    private CourseRepository courseRepo;
    private RegistrationRepository regRepo;
    
    public CourseService() {
        this.courseRepo = new CourseRepository();
        this.regRepo = new RegistrationRepository();
    }
    
    // Ders ekle
    public int addCourse(Course course) {
        // Ders kodu kontrolü
        if (courseRepo.findByCode(course.getCode()) != null) {
            System.out.println("Bu ders kodu zaten var!");
            return -1;
        }
        return courseRepo.insert(course);
    }
    
    // Ders güncelle
    public boolean updateCourse(Course course) {
        return courseRepo.update(course);
    }
    
    // Ders sil
    public boolean deleteCourse(int courseId) {
        return courseRepo.delete(courseId);
    }
    
    // Tüm dersleri getir
    public List<Course> getAllCourses() {
        return courseRepo.findAll();
    }
    
    // ID ile ders getir
    public Course getCourseById(int id) {
        return courseRepo.findById(id);
    }
    
    // Kod ile ders getir
    public Course getCourseByCode(String code) {
        return courseRepo.findByCode(code);
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
