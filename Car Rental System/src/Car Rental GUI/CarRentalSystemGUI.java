import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CarRentalSystemGUI {
    private static JFrame frame;
    private static JTextField emailField;
    private static JPasswordField passwordField;
    private static JTextArea outputArea;
    private static DatabaseConnection dbConnection = new DatabaseConnection();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CarRentalSystemGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Car Rental System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel emailLabel = new JLabel("Email:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(emailLabel, constraints);

        emailField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(emailField, constraints);

        JLabel passwordLabel = new JLabel("Password:");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(passwordLabel, constraints);

        passwordField = new JPasswordField(20);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userLogin();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(loginButton, constraints);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        constraints.gridx = 1;
        panel.add(registerButton, constraints);

        JButton adminButton = new JButton("Admin");
        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adminLogin();
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        panel.add(adminButton, constraints);

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        panel.add(scrollPane, constraints);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void registerUser() {
        String name = JOptionPane.showInputDialog(frame, "Enter your name:");
        String email = JOptionPane.showInputDialog(frame, "Enter your email:");
        String phoneNumber = JOptionPane.showInputDialog(frame, "Enter your phone number:");
        String password = JOptionPane.showInputDialog(frame, "Enter a password:");

        try {
            Connection conn = dbConnection.getConnection();
            String query = "INSERT INTO users (name, email, phoneNumber, password) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setString(4, password);
            preparedStatement.executeUpdate();

            outputArea.setText("User registered successfully.\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred during user registration.\n");
            ex.printStackTrace();
        }
    }

    private static void userLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                outputArea.setText("Login successful. Welcome, " + email + "!\n");
                showUserMenu(email);
            } else {
                outputArea.setText("Login failed. Please check your credentials.\n");
            }
        } catch (SQLException ex) {
            outputArea.setText("An error occurred during user login.\n");
            ex.printStackTrace();
        }
    }

    private static void showUserMenu(String email) {
        JFrame userFrame = new JFrame("User Menu");
        userFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        userFrame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(5, 1));
        JButton viewCarsButton = new JButton("View Cars");
        viewCarsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllCars();
            }
        });
        panel.add(viewCarsButton);

        JButton bookCarButton = new JButton("Book Car");
        bookCarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bookCar(email);
            }
        });
        panel.add(bookCarButton);

        JButton addReviewButton = new JButton("Add Review");
        addReviewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addReview(email);
            }
        });
        panel.add(addReviewButton);

        JButton viewBookingsButton = new JButton("View Bookings");
        viewBookingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewUserBookings(email);
            }
        });
        panel.add(viewBookingsButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userFrame.dispose();
            }
        });
        panel.add(logoutButton);

        userFrame.add(panel);
        userFrame.setVisible(true);
    }

    private static void viewAllCars() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM cars";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Cars:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", Make: " + resultSet.getString("make") +
                        ", Model: " + resultSet.getString("model") +
                        ", Year: " + resultSet.getInt("year") +
                        ", Price: " + resultSet.getDouble("price") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing cars.\n");
            ex.printStackTrace();
        }
    }

    private static void clearOutputArea() {
        outputArea.setText("");
    }

    private static void bookCar(String email) {
        String carIdString = JOptionPane.showInputDialog(frame, "Enter car ID to book:");
        if (carIdString == null || carIdString.isEmpty()) return; // User canceled or left field empty
        int carId = Integer.parseInt(carIdString);

        try {
            Connection conn = dbConnection.getConnection();
            String query = "INSERT INTO bookings (user_id, car_id) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, getUserIdByEmail(email));
            preparedStatement.setInt(2, carId);
            preparedStatement.executeUpdate();

            outputArea.setText("Car booked successfully.\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while booking car.\n");
            ex.printStackTrace();
        }
    }

    private static void addReview(String email) {
        String carIdString = JOptionPane.showInputDialog(frame, "Enter car ID to review:");
        if (carIdString == null || carIdString.isEmpty()) return; // User canceled or left field empty
        int carId = Integer.parseInt(carIdString);

        String ratingString = JOptionPane.showInputDialog(frame, "Enter rating (1-5):");
        if (ratingString == null || ratingString.isEmpty()) return; // User canceled or left field empty
        int rating = Integer.parseInt(ratingString);

        String reviewText = JOptionPane.showInputDialog(frame, "Enter review text:");
        if (reviewText == null || reviewText.isEmpty()) return; // User canceled or left field empty

        try {
            Connection conn = dbConnection.getConnection();
            String query = "INSERT INTO reviews (user_id, car_id, rating, review_text) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, getUserIdByEmail(email));
            preparedStatement.setInt(2, carId);
            preparedStatement.setInt(3, rating);
            preparedStatement.setString(4, reviewText);
            preparedStatement.executeUpdate();

            outputArea.setText("Review added successfully.\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while adding review.\n");
            ex.printStackTrace();
        }
    }

    private static void viewUserBookings(String email) {
        clearOutputArea();
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM bookings WHERE user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, getUserIdByEmail(email));
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Bookings:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", Car ID: " + resultSet.getInt("car_id") +
                        ", Booking Date: " + resultSet.getTimestamp("booking_date") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing bookings.\n");
            ex.printStackTrace();
        }
    }

    private static void adminLogin() {
        String adminEmail = "admin@carrental.com";
        String adminPassword = "admin123";

        String email = JOptionPane.showInputDialog(frame, "Enter your email:");
        String password = JOptionPane.showInputDialog(frame, "Enter your password:");

        if (email != null && password != null && email.equals(adminEmail) && password.equals(adminPassword)) {
            outputArea.setText("Welcome Admin\n");
            showAdminMenu();
        } else {
            outputArea.setText("Admin login failed. Please check your credentials.\n");
        }
    }

    private static void showAdminMenu() {
        JFrame adminFrame = new JFrame("Admin Menu");
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adminFrame.setSize(400, 300);

        JPanel panel = new JPanel(new GridLayout(5, 1));
        JButton viewUsersButton = new JButton("View Users");
        viewUsersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllUsers();
            }
        });
        panel.add(viewUsersButton);

        JButton viewCarsButton = new JButton("View Cars");
        viewCarsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllCars();
            }
        });
        panel.add(viewCarsButton);

        JButton viewBookingsButton = new JButton("View Bookings");
        viewBookingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllBookings();
            }
        });
        panel.add(viewBookingsButton);

        JButton viewPaymentsButton = new JButton("View Payments");
        viewPaymentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllPayments();
            }
        });
        panel.add(viewPaymentsButton);

        JButton viewReviewsButton = new JButton("View Reviews");
        viewReviewsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAllReviews();
            }
        });
        panel.add(viewReviewsButton);

        adminFrame.add(panel);
        adminFrame.setVisible(true);
    }

    private static void viewAllUsers() {
        clearOutputArea();
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Users:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", Name: " + resultSet.getString("name") +
                        ", Email: " + resultSet.getString("email") +
                        ", Phone Number: " + resultSet.getString("phoneNumber") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing users.\n");
            ex.printStackTrace();
        }
    }

    private static void viewAllBookings() {
        clearOutputArea();
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM bookings";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Bookings:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", User ID: " + resultSet.getInt("user_id") +
                        ", Car ID: " + resultSet.getInt("car_id") +
                        ", Booking Date: " + resultSet.getTimestamp("booking_date") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing bookings.\n");
            ex.printStackTrace();
        }
    }

    private static void viewAllPayments() {
        clearOutputArea();
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM payments";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Payments:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", Booking ID: " + resultSet.getInt("booking_id") +
                        ", Amount: " + resultSet.getDouble("amount") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing payments.\n");
            ex.printStackTrace();
        }
    }

    private static void viewAllReviews() {
        clearOutputArea();
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT r.id, u.name AS user_name, c.make AS car_make, c.model AS car_model, r.rating, r.review_text, r.review_date "
                    + "FROM reviews r JOIN users u ON r.user_id = u.id JOIN cars c ON r.car_id = c.id";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            outputArea.append("Reviews:\n");
            while (resultSet.next()) {
                outputArea.append("ID: " + resultSet.getInt("id") +
                        ", User Name: " + resultSet.getString("user_name") +
                        ", Car Make: " + resultSet.getString("car_make") +
                        ", Car Model: " + resultSet.getString("car_model") +
                        ", Rating: " + resultSet.getInt("rating") +
                        ", Review Text: " + resultSet.getString("review_text") +
                        ", Review Date: " + resultSet.getTimestamp("review_date") + "\n");
            }
            outputArea.append("\n");
        } catch (SQLException ex) {
            outputArea.setText("An error occurred while viewing reviews.\n");
            ex.printStackTrace();
        }
    }

    private static int getUserIdByEmail(String email) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT id FROM users WHERE email = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            outputArea.setText("An error occurred while getting user ID by email.\n");
            e.printStackTrace();
            return -1;
        }
    }
}

class DatabaseConnection {
    private static final String URL = "Change Your Database String/carrentdb";
    private static final String USERNAME = "Change USERNAME";
    private static final String PASSWORD = "Change PASSWORD";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
