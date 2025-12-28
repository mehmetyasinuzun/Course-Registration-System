import javafx.application.Application;
import database.DatabaseSetup;
import view.LoginView;

/**
 * University Course Registration System – Main Entry Point
 *
 * This file is the starting point of the application.
 * It launches the JavaFX GUI and initializes the database.
 *
 * Run:
 *   java Main
 *
 * Default Login: admin / admin123
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║  University Course Registration       ║");
        System.out.println("║  System Starting...                   ║");
        System.out.println("╚═══════════════════════════════════════╝");

        // 1. Initialize database (tables, admin, 111 course catalog)
        DatabaseSetup.initialize();

        // 2. Launch JavaFX GUI
        Application.launch(LoginView.class, args);
    }
}
