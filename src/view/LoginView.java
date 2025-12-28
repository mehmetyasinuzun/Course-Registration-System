package view;

import controller.AuthController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.User;

public class LoginView extends Application {

    private AuthController authController = new AuthController();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("University Course Registration System");

        // ================= ROOT =================
        HBox root = new HBox();
        root.setPrefSize(800, 550);

        // ================= LEFT PANEL (PHOTO + TEXT) =================
        StackPane leftPane = new StackPane();
        leftPane.setPrefWidth(400);
        
        // Load background image (if available)
        try {
            java.net.URL imageUrl = getClass().getResource("university_bg.jpg");
            if (imageUrl != null) {
                String imagePath = imageUrl.toExternalForm();
                leftPane.setStyle(
                        "-fx-background-image: url('" + imagePath + "');" +
                                "-fx-background-size: cover;" +
                                "-fx-background-position: center center;" +
                                "-fx-background-repeat: no-repeat;"
                );
            } else {
                // Fallback to gradient background
                leftPane.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);"
                );
            }
        } catch (Exception e) {
            // Fallback to gradient background
            leftPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);"
            );
        }

        // Dark overlay on photo (so text is clearly visible)
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.40);");
        overlay.prefWidthProperty().bind(leftPane.widthProperty());
        overlay.prefHeightProperty().bind(leftPane.heightProperty());

        // Left panel text
        VBox leftText = new VBox(10);
        leftText.setAlignment(Pos.CENTER_LEFT);
        leftText.setPadding(new Insets(0, 0, 0, 40));

        Label cap = new Label("🎓");
        cap.setTextFill(Color.WHITE);
        cap.setFont(Font.font(48));

        Label portalTitle = new Label("University Portal");
        portalTitle.setTextFill(Color.WHITE);
        portalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        Label portalSub = new Label("Course Registration System");
        portalSub.setTextFill(Color.WHITE);
        portalSub.setFont(Font.font(16));
        portalSub.setOpacity(0.9);

        leftText.getChildren().addAll(cap, portalTitle, portalSub);

        leftPane.getChildren().addAll(overlay, leftText);

        // ================= RIGHT PANEL (LOGIN FORM) =================
        StackPane rightPane = new StackPane();
        rightPane.setPrefWidth(400);
        rightPane.setStyle("-fx-background-color: #F4F7FE;");
        rightPane.setPadding(new Insets(0, 40, 0, 40)); // only horizontal padding

        VBox loginCard = createLoginCard(); // LOGIN CARD
        rightPane.getChildren().add(loginCard);
        StackPane.setAlignment(loginCard, Pos.CENTER);

        root.getChildren().addAll(leftPane, rightPane);

        Scene scene = new Scene(root, 800, 550);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // ================= LOGIN CARD =================
    private VBox createLoginCard() {

        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(380);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(20);
        shadow.setOffsetY(10);
        card.setEffect(shadow);

        Label iconLabel = new Label("🎓");
        iconLabel.setFont(Font.font(50));

        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.web("#333"));

        Label subtitleLabel = new Label("University Course Registration System");
        subtitleLabel.setFont(Font.font(14));
        subtitleLabel.setTextFill(Color.web("#666"));

        // ---------- FORM ----------
        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);

        // Username
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        usernameLabel.setTextFill(Color.web("#555"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(45);
        usernameField.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 15;"
        );

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        passwordLabel.setTextFill(Color.web("#555"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 0 15;"
        );

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        formBox.getChildren().addAll(usernameBox, passwordBox);

        // ---------- ERROR MESSAGE ----------
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#e74c3c"));
        errorLabel.setFont(Font.font(12));
        errorLabel.setVisible(false);

        // ---------- LOGIN BUTTON ----------
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(45);
        loginButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (!username.isEmpty() && !password.isEmpty()) {
                User user = authController.login(username, password);
                if (user != null) {
                    errorLabel.setVisible(false);
                    openDashboard(user);
                } else {
                    errorLabel.setText("⚠ Invalid username or password!");
                    errorLabel.setVisible(true);
                    passwordField.clear();
                }
            } else {
                errorLabel.setText("⚠ Username and password are required!");
                errorLabel.setVisible(true);
            }
        });

        // Login with Enter key
        passwordField.setOnAction(e -> loginButton.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Label infoLabel = new Label("Default: admin / admin123");
        infoLabel.setFont(Font.font(11));
        infoLabel.setTextFill(Color.web("#999"));

        card.getChildren().addAll(
                iconLabel,
                welcomeLabel,
                subtitleLabel,
                formBox,
                errorLabel,
                loginButton,
                infoLabel
        );

        return card;
    }

    // ================= DASHBOARD =================
    private void openDashboard(User user) {
        switch (user.getRole()) {
            case "ADMIN" -> new AdminView(primaryStage, user).show();
            case "INSTRUCTOR" -> new InstructorView(primaryStage, user).show();
            case "STUDENT" -> new StudentView(primaryStage, user).show();
        }
    }
}
