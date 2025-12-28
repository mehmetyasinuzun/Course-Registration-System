package database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseSetup {

    public static void initialize() {
        createTables();
        addNewColumns();  // Add new columns (for existing DB)
        insertDefaultAdmin();
        insertDefaultCatalogCourses();  // Seed Data - Default course catalog
        System.out.println("✓ Database is ready!");
    }

    private static void createTables() {
        Connection conn = DatabaseConnection.getConnection();

        String[] tables = {
                // Users table (year and semester added)
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT,
                role TEXT NOT NULL,
                student_number TEXT,
                year INTEGER DEFAULT 1,
                semester INTEGER DEFAULT 1
            )
            """,

                // Courses table (semester added)
                """
            CREATE TABLE IF NOT EXISTS courses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                credits INTEGER NOT NULL,
                capacity INTEGER NOT NULL,
                instructor_id INTEGER,
                day_of_week TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                semester INTEGER DEFAULT 0,
                FOREIGN KEY (instructor_id) REFERENCES users(id)
            )
            """,

                // Prerequisites table
                """
            CREATE TABLE IF NOT EXISTS prerequisites (
                course_id INTEGER,
                prerequisite_id INTEGER,
                PRIMARY KEY (course_id, prerequisite_id),
                FOREIGN KEY (course_id) REFERENCES courses(id),
                FOREIGN KEY (prerequisite_id) REFERENCES courses(id)
            )
            """,

                // Registrations table
                """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                course_id INTEGER NOT NULL,
                status TEXT DEFAULT 'PENDING',
                request_date TEXT,
                response_date TEXT,
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (course_id) REFERENCES courses(id)
            )
            """,

                // Completed courses table
                """
            CREATE TABLE IF NOT EXISTS completed_courses (
                student_id INTEGER,
                course_id INTEGER,
                grade TEXT DEFAULT 'CC',
                entry_type TEXT DEFAULT 'REGULAR',
                PRIMARY KEY (student_id, course_id),
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (course_id) REFERENCES course_catalog(id)
            )
            """,

                // Notifications table
                """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """,

                // Course Requests table (instructors' course opening requests)
                """
            CREATE TABLE IF NOT EXISTS course_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                credits INTEGER NOT NULL,
                capacity INTEGER NOT NULL,
                instructor_id INTEGER NOT NULL,
                day_of_week TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                semester INTEGER DEFAULT 0,
                status TEXT DEFAULT 'PENDING',
                request_date TEXT DEFAULT CURRENT_TIMESTAMP,
                response_date TEXT,
                FOREIGN KEY (instructor_id) REFERENCES users(id)
            )
            """,

                // Course Catalog table (all course templates in the system)
                """
            CREATE TABLE IF NOT EXISTS course_catalog (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT UNIQUE NOT NULL,
                name TEXT NOT NULL,
                credits INTEGER NOT NULL,
                semester INTEGER DEFAULT 0
            )
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            System.out.println("  ✓ Tables created");
        } catch (SQLException e) {
            System.out.println("  ✗ Table creation error: " + e.getMessage());
        }
    }

    // Add new columns to an existing database (ALTER TABLE)
    private static void addNewColumns() {
        Connection conn = DatabaseConnection.getConnection();

        String[] alterStatements = {
                "ALTER TABLE users ADD COLUMN year INTEGER DEFAULT 1",
                "ALTER TABLE users ADD COLUMN semester INTEGER DEFAULT 1",
                "ALTER TABLE courses ADD COLUMN semester INTEGER DEFAULT 0",
                "ALTER TABLE course_requests ADD COLUMN semester INTEGER DEFAULT 0",
                "ALTER TABLE completed_courses ADD COLUMN entry_type TEXT DEFAULT 'REGULAR'"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : alterStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // If the column already exists, SQLite throws an error — ignore it
                }
            }
        } catch (SQLException e) {
            // General error
        }
    }

    private static void insertDefaultAdmin() {
        Connection conn = DatabaseConnection.getConnection();

        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertSql = """
            INSERT INTO users (username, password, full_name, email, role)
            VALUES ('admin', 'admin123', 'System Administrator', 'admin@uni.edu', 'ADMIN')
        """;

        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(insertSql);
                System.out.println("  ✓ Default admin created (admin/admin123)");
            } else {
                System.out.println("  ✓ Admin already exists");
            }
        } catch (SQLException e) {
            System.out.println("  ✗ Admin creation error: " + e.getMessage());
        }
    }

    // ========== SEED DATA (Default Course Catalog) ==========
    private static void insertDefaultCatalogCourses() {
        Connection conn = DatabaseConnection.getConnection();

        // First, check if the catalog already has courses
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM course_catalog");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("  ✓ Course catalog already exists (" + rs.getInt(1) + " courses)");
                return;
            }
        } catch (SQLException e) {
            System.out.println("  ✗ Catalog check error: " + e.getMessage());
            return;
        }

        // Seed Data - All courses
        String[][] courses = {
                // 1st SEMESTER
                {"CARP101", "CAREER PLANNING", "2", "1"},
                {"CNG101", "INTRODUCTION TO COMPUTER ENGINEERING", "2", "1"},
                {"CNG103", "INTRODUCTION TO PROGRAMMING", "6", "1"},
                {"ENG101", "ACADEMIC ENGLISH I", "3", "1"},
                {"MATH101", "CALCULUS I", "6", "1"},
                {"MATH103", "LINEAR ALGEBRA", "4", "1"},
                {"PHYS101", "PHYSICS I", "5", "1"},
                {"TURK101", "TURKISH I", "2", "1"},

                // 2nd SEMESTER
                {"CNG104", "PROGRAMMING", "7", "2"},
                {"EEE102", "CIRCUIT THEORY I", "6", "2"},
                {"ENG102", "ACADEMIC ENGLISH II", "4", "2"},
                {"MATH102", "CALCULUS II", "6", "2"},
                {"PHYS102", "PHYSICS II", "5", "2"},
                {"TURK102", "TURKISH II", "2", "2"},

                // 3rd SEMESTER
                {"CNG201", "DATA STRUCTURES", "5", "3"},
                {"CNG203", "OBJECT ORIENTED PROGRAMMING", "6", "3"},
                {"CNG205", "DISCRETE MATHEMATICS", "3", "3"},
                {"HIST101", "ATATURK'S PRINCIPLES AND HISTORY OF TURKISH REVOLUTION I", "2", "3"},
                {"MATH201", "PROBABILITY AND STATISTICS", "4", "3"},
                {"MATH203", "DIFFERENTIAL EQUATIONS", "4", "3"},
                {"OHS201", "OCCUPATIONAL HEALTH AND SAFETY I", "2", "3"},

                // 4th SEMESTER
                {"CNG212", "PROGRAMMING LANGUAGES", "7", "4"},
                {"CNG244", "OPERATING SYSTEMS", "7", "4"},
                {"EEE204", "DIGITAL SYSTEM DESIGN", "6", "4"},
                {"HIST102", "ATATURK'S PRINCIPLES AND HISTORY OF TURKISH REVOLUTION II", "2", "4"},
                {"OHS202", "OCCUPATIONAL HEALTH AND SAFETY II", "2", "4"},

                // 5th SEMESTER
                {"CNG301", "SUMMER PRACTICE I", "5", "5"},
                {"CNG311", "DATABASE SYSTEMS AND APPLICATIONS", "6", "5"},
                {"CNG335", "ALGORITHMS DESIGN AND ANALYSIS", "6", "5"},
                {"CNG361", "WEB PROGRAMMING", "6", "5"},
                {"CNG371", "SOFTWARE ENGINEERING", "5", "5"},
                {"CNG381", "COMPUTER ORGANIZATION", "5", "5"},

                // 6th SEMESTER
                {"CNG304", "COMPUTER ENGINEERING DESIGN PROJECT I", "4", "6"},
                {"CNG342", "DATA COMMUNICATION AND COMPUTER NETWORKS", "6", "6"},
                {"CNG344", "INFORMATION SECURITY", "4", "6"},
                {"CNG346", "MOBILE PROGRAMMING", "6", "6"},
                {"CRE102", "CHILD RIGHTS AND FAMILY EDUCATION", "2", "6"},

                // 7th SEMESTER
                {"CNG403", "AUTOMATA THEORY", "5", "7"},
                {"CNG405", "COMPUTER ENGINEERING DESIGN PROJECT II", "4", "7"},
                {"CNG407", "SUMMER PRACTICE II", "5", "7"},

                // 8th SEMESTER
                {"SEEC001", "SECTORAL EXPERIENCE ELECTIVE COURSE", "30", "8"},

                // COMMON ELECTIVES (SOC) - semester=0 (General)
                {"SOC101", "INTRODUCTION TO BUSINESS", "4", "0"},
                {"SOC102", "CURRENT TOPICS IN PSYCHOLOGY", "4", "0"},
                {"SOC103", "GUITAR I", "4", "0"},
                {"SOC104", "SPECIAL TOPICS IN ECONOMICS, INDUSTRY AND DEVELOPMENT", "4", "0"},
                {"SOC105", "FOREIGN LANGUAGES: BASIC FRENCH", "4", "0"},
                {"SOC106", "FOREIGN LANGUAGES: BASIC GERMAN", "4", "0"},
                {"SOC107", "FOREIGN LANGUAGES: BASIC SPANISH", "4", "0"},
                {"SOC108", "HISTORY OF WAR", "4", "0"},
                {"SOC109", "MEDIA LITERACY", "4", "0"},
                {"SOC110", "COMMUNICATION SKILLS", "4", "0"},
                {"SOC111", "BASIC PHOTOGRAPHY", "4", "0"},
                {"SOC112", "PERSONAL DEVELOPMENT", "4", "0"},
                {"SOC113", "CREATIVE THINKING METHODS AND TECHNIQUES", "4", "0"},
                {"SOC114", "CORRECT AND EFFECTIVE SPEECH", "4", "0"},
                {"SOC115", "SPEED READING", "4", "0"},
                {"SOC116", "PUBLIC RELATIONS", "4", "0"},
                {"SOC117", "BUSINESS LAW AND ETHICS", "4", "0"},
                {"SOC118", "WEB DESIGN", "4", "0"},
                {"SOC119", "ENTREPRENEURSHIP AND BUSINESS PLAN PREPARATION", "4", "0"},
                {"SOC120", "HISTORY OF SCIENCE", "4", "0"},
                {"SOC199", "VOLUNTEERING STUDIES", "4", "0"},
                {"SOC121", "DEFENSE INDUSTRY 101", "4", "0"},
                {"SOC122", "DEFENSE INDUSTRY 401", "4", "0"},
                {"SOC123", "TECHNOLOGY MANAGEMENT", "4", "0"},
                {"SOC124", "HISTORY OF SCIENCE II", "4", "0"},
                {"SOC125", "FIRST AID", "4", "0"},
                {"SOC126", "R&D AWARENESS", "4", "0"},
                {"SOC127", "BUSINESS SOCIAL MEDIA", "4", "0"},
                {"SOC128", "CONTACT FOR FOREIGN TRADE", "4", "0"},
                {"SOC129", "TURKISH DEFENSE INDUSTRY", "4", "0"},
                {"SOC130", "VALUES EDUCATION", "4", "0"},
                {"SOC131", "BUSINESS LAW", "4", "0"},
                {"SOC132", "DICTION IN BUSINESS LIFE", "4", "0"},
                {"SOC133", "ACADEMIC WRITING AND PRESENTATION TECHNIQUE", "4", "0"},
                {"SOC134", "DESIGN FOR PATENT", "4", "0"},
                {"SOC135", "ADDICTION AND FIGHTING ADDICTION", "3", "0"},

                // TECHNICAL ELECTIVES 6YY (TEC) - semester=0 (General)
                {"CNG352", "INTRODUCTION TO PARALLEL PROGRAMMING", "5", "0"},
                {"CNG354", "VISUAL PROGRAMMING", "5", "0"},
                {"CNG356", "NATURAL LANGUAGE PROCESSING", "5", "0"},
                {"CNG358", "DATA MINING", "5", "0"},
                {"CNG360", "CYBER SECURITY", "5", "0"},
                {"CNG362", "CRYPTOLOGY", "5", "0"},
                {"CNG364", "FUZZY LOGIC", "5", "0"},
                {"CNG366", "OPTIMIZATION", "5", "0"},
                {"CNG368", "BIODEFENSE", "5", "0"},
                {"CNG370", "BIOINFORMATICS", "5", "0"},
                {"CNG372", "INTERNET OF THINGS", "5", "0"},
                {"CNG374", "ADVANCED JAVA PROGRAMMING", "5", "0"},
                {"CNG376", "MULTIMEDIA SYSTEMS", "5", "0"},
                {"CNG378", "REAL-TIME OPERATING SYSTEM", "5", "0"},
                {"CNG380", "DISTRIBUTED OPERATING SYSTEM", "5", "0"},
                {"EEE304", "DIGITAL SIGNAL PROCESSING", "5", "0"},

                // TECHNICAL ELECTIVES 7YY (TEC) - semester=0 (General)
                {"CNG457", "MACHINE LEARNING", "5", "0"},
                {"CNG459", "IMAGE PROCESSING", "5", "0"},
                {"EEE413", "ROBOTICS DESIGN", "5", "0"},
                {"CNG455", "R&D AND TECHNOLOGY IN DEFENSE INDUSTRY", "5", "0"},
                {"CNG465", "BLOCKCHAIN TECHNOLOGY", "5", "0"},
                {"CNG467", "CLOUD COMPUTING", "5", "0"},
                {"CNG451", "BIG DATA", "5", "0"},
                {"CNG453", "HIGH PERFORMANCE COMPUTING", "5", "0"},
                {"CNG469", "COMPUTER GRAPHICS", "5", "0"},
                {"CNG461", "NETWORK ADMINISTRATION", "5", "0"},
                {"CNG463", "SYSTEM ADMINISTRATION", "5", "0"},
                {"EEE409", "RADAR SYSTEMS", "5", "0"},
                {"EEE411", "ELECTRONIC WARFARE SYSTEMS", "5", "0"},
                {"EEE415", "SYSTEM ENGINEERING", "5", "0"},
                {"CNG445", "WEB TECHNOLOGIES", "5", "0"},
                {"CNG447", "ADVANCED COMPUTER NETWORKS", "5", "0"},
                {"CNG449", "ROUTING AND SWITCHING PRINCIPLES", "5", "0"},
                {"CNG401", "EMBEDDED SYSTEMS", "5", "0"}
        };

        String sql = "INSERT INTO course_catalog (code, name, credits, semester) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int count = 0;
            for (String[] course : courses) {
                pstmt.setString(1, course[0]);  // code
                pstmt.setString(2, course[1]);  // name
                pstmt.setInt(3, Integer.parseInt(course[2]));  // credits
                pstmt.setInt(4, Integer.parseInt(course[3]));  // semester
                pstmt.executeUpdate();
                count++;
            }
            System.out.println("  ✓ Default course catalog created (" + count + " courses)");
        } catch (SQLException e) {
            System.out.println("  ✗ Catalog creation error: " + e.getMessage());
        }
    }
}
