package model;

/**
 * Ders Kataloğu - Sistemdeki tüm ders şablonları
 * Ders kodu, adı ve kredisi burada tanımlanır
 */
public class CourseCatalog {
    private int id;
    private String code;      // Ders kodu (örn: CS101)
    private String name;      // Ders adı
    private int credits;      // Kredi sayısı
    private int semester;     // Hangi dönem dersi (0=genel, 1-8=belirli dönem)
    
    public CourseCatalog() {}
    
    public CourseCatalog(String code, String name, int credits, int semester) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.semester = semester;
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    
    public String getSemesterDisplay() {
        if (semester == 0) return "General";
        int year = (semester + 1) / 2;
        String period = (semester % 2 == 1) ? "Fall" : "Spring";
        return year + ". Year " + period;
    }
    
    @Override
    public String toString() {
        return code + " - " + name + " (" + credits + " Credit)";
    }
}
