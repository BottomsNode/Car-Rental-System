import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CarRentalSystem {
    static DatabaseConnection dbConnection = new DatabaseConnection();
    static Scanner scanner = new Scanner(System.in);

    
    // ----------------------------------------------------------
    // ---------------------- User Methods ----------------------
    // ----------------------------------------------------------



    public static boolean registerUser() {
        try {
            System.out.println("Enter your name:");
            String name = scanner.nextLine();
            System.out.println("Enter your email:");
            String email = scanner.nextLine();
            System.out.println("Enter your phone number:");
            String phoneNumber = scanner.nextLine();
            System.out.println("Enter a password:");
            String password = scanner.nextLine();

            Connection conn = dbConnection.getConnection();

            // Check if the email address is already registered
            String query = "SELECT * FROM users WHERE email = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // Email address is already registered
                System.out.println("Email address is already registered.");
                return false;
            }

            // Email address is not registered, so insert the new user into the table
            query = "INSERT INTO users (name, email, phoneNumber, password) VALUES (?, ?, ?, ?)";
            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, phoneNumber);
            preparedStatement.setString(4, password);
            preparedStatement.executeUpdate();

            System.out.println("User registered successfully.");
            return true;
        } catch (SQLException e) {
            System.out.println("An error occurred during user registration process.");
            e.printStackTrace();
            return false;
        }
    }

    public static void userLogin() {
        try {
            System.out.println("Enter your email:");
            String email = scanner.nextLine();
            System.out.println("Enter your password:");
            String password = scanner.nextLine();

            if (loginUser(email, password)) {
                System.out.println("Login successful. Welcome, " + email + "!");

                while (true) {
                    System.out.println("\nUser Menu:");
                    System.out.println("1. View Cars");
                    System.out.println("2. Book Cars");
                    System.out.println("3. Make a Review");
                    System.out.println("4. Show Bookings");
                    System.out.println("5. Show Reviews");
                    System.out.println("6. Show Payments");
                    System.out.println("7. Logout");

                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1:
                            viewAllCars();
                            break;
                        case 2:
                            bookCar(email);
                            break;
                        case 3:
                            addReview(email);
                            break;
                        case 4:
                            viewUserBookings(email);
                            break;
                        case 5:
                            showReviewsByUser(email);
                            break;
                        case 6:
                            showPaymentsByUser(email);
                            break;
                        case 7:
                            System.out.println("Logged out.");
                            return;
                        default:
                            System.out.println("Invalid choice.");
                    }
                }
            } else {
                System.out.println("Login failed. Please check your credentials.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred during user login process.");
            e.printStackTrace();
        }
    }

    public static boolean loginUser(String email, String password) {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("An error occurred during user login process.");
            e.printStackTrace();
            return false;
        }
    }

    public static void addReview(String email) {
        try {
            Connection conn = dbConnection.getConnection();
            int carId = selectCarForReview();
            if (carId == -1) {
                return;
            }

            System.out.println("Enter your rating (1-5):");
            int rating = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter your review:");
            String reviewText = scanner.nextLine();

            int userId = getUserIdByEmail(email);

            String query = "INSERT INTO reviews (user_id, car_id, rating, review_text, review_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, carId);
            preparedStatement.setInt(3, rating);
            preparedStatement.setString(4, reviewText);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Review added successfully.");
            } else {
                System.out.println("Failed to add review.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while adding review.");
            e.printStackTrace();
        }
    }

    public static int selectCarForReview() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM cars";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("Select a car for review:");
            int count = 1;
            while (resultSet.next()) {
                System.out.println(count + ". Car ID: " + resultSet.getInt("id") +
                        ", Make: " + resultSet.getString("make") +
                        ", Model: " + resultSet.getString("model") +
                        ", Year: " + resultSet.getInt("year") +
                        ", Price: " + resultSet.getDouble("price"));
                count++;
            }

            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice >= count) {
                System.out.println("Invalid choice. Please select a valid car.");
                return -1;
            }
            return choice;
        } catch (SQLException e) {
            System.out.println("An error occurred while selecting car for review.");
            e.printStackTrace();
            return -1;
        }
    }

    public static void viewAllCars() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM cars";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Car ID: " + resultSet.getInt("id"));
                System.out.println("Make: " + resultSet.getString("make"));
                System.out.println("Model: " + resultSet.getString("model"));
                System.out.println("Year: " + resultSet.getInt("year"));
                System.out.println("Price: " + resultSet.getDouble("price"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while viewing all cars.");
            e.printStackTrace();
        }
    }

    public static void bookCar(String email) {
        try {
            int carChoice = selectCar();
            if (carChoice == -1) {
                return;
            }
            Connection conn = dbConnection.getConnection();
            String carQuery = "SELECT * FROM cars LIMIT ?, 1";
            PreparedStatement carStatement = conn.prepareStatement(carQuery);
            carStatement.setInt(1, carChoice - 1);
            ResultSet carResultSet = carStatement.executeQuery();
            if (carResultSet.next()) {
                int carId = carResultSet.getInt("id");
                double carPrice = carResultSet.getDouble("price");
                String query = "INSERT INTO bookings (user_id, car_id) VALUES (?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(query, new String[] { "id" });
                preparedStatement.setInt(1, getUserIdByEmail(email));
                preparedStatement.setInt(2, carId);
                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                int bookingId = -1;
                if (generatedKeys.next()) {
                    bookingId = generatedKeys.getInt(1);
                }
                if (bookingId != -1) {
                    double paymentAmount = carPrice;
                    if (processPayment(bookingId, paymentAmount, conn)) {
                        System.out.println("Car booked successfully.");
                    } else {
                        System.out.println("Payment processing failed.");
                    }
                } else {
                    System.out.println("Failed to retrieve booking ID.");
                }
            } else {
                System.out.println("Failed to retrieve car details.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while booking car.");
            e.printStackTrace();
        }
    }

    public static boolean processPayment(int bookingId, double amount, Connection conn) {
        try {
            String query = "INSERT INTO payments (booking_id, amount) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, bookingId);
            preparedStatement.setDouble(2, amount);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("An error occurred while processing payment.");
            e.printStackTrace();
            return false;
        }
    }

    public static int selectCar() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM cars";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            int carCount = 0;
            while (resultSet.next()) {
                carCount++;
                System.out.println(carCount + ". Car ID: " + resultSet.getInt("id"));
                System.out.println("   Make: " + resultSet.getString("make"));
                System.out.println("   Model: " + resultSet.getString("model"));
                System.out.println("   Year: " + resultSet.getInt("year"));
                System.out.println("   Price: " + resultSet.getDouble("price"));
                System.out.println("-------------------------");
            }
            if (carCount == 0) {
                System.out.println("No cars available for booking.");
                return -1;
            }
            System.out.println("Enter the number corresponding to the car you want to book:");
            int carChoice = Integer.parseInt(scanner.nextLine());
            if (carChoice < 1 || carChoice > carCount) {
                System.out.println("Invalid choice.");
                return -1;
            }
            return carChoice;
        } catch (SQLException e) {
            System.out.println("An error occurred while selecting car.");
            e.printStackTrace();
            return -1;
        }
    }

    public static void viewUserBookings(String email) {
        int userId = getUserIdByEmail(email);
        if (userId == -1) {
            System.out.println("User not found.");
            return;
        }
        try (Connection conn = dbConnection.getConnection()) {
            String query = "SELECT b.id AS booking_id, c.make AS car_make, c.model AS car_model, c.year AS car_year, c.price AS car_price "
                    +
                    "FROM bookings b " +
                    "JOIN cars c ON b.car_id = c.id " +
                    "WHERE b.user_id = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.isBeforeFirst()) {
                        System.out.println("No bookings found for the user.");
                    } else {
                        while (resultSet.next()) {
                            System.out.println("Booking ID: " + resultSet.getInt("booking_id"));
                            System.out.println("Car Make: " + resultSet.getString("car_make"));
                            System.out.println("Car Model: " + resultSet.getString("car_model"));
                            System.out.println("Car Year: " + resultSet.getInt("car_year"));
                            System.out.println("Car Price: " + resultSet.getDouble("car_price"));
                            System.out.println("-------------------------");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while viewing user's bookings.");
            e.printStackTrace();
        }
    }

    public static void showReviewsByUser(String email) {
        try {
            Connection conn = dbConnection.getConnection();
            int userId = getUserIdByEmail(email);
            if (userId == -1) {
                System.out.println("User not found.");
                return;
            }

            String query = "SELECT r.id, c.make AS car_make, c.model AS car_model, r.rating, r.review_text, r.review_date "
                    +
                    "FROM reviews r " +
                    "JOIN cars c ON r.car_id = c.id " +
                    "WHERE r.user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No reviews found for the user.");
            } else {
                while (resultSet.next()) {
                    System.out.println("Review ID: " + resultSet.getInt("id"));
                    System.out.println("Car Make: " + resultSet.getString("car_make"));
                    System.out.println("Car Model: " + resultSet.getString("car_model"));
                    System.out.println("Rating: " + resultSet.getInt("rating"));
                    System.out.println("Review Text: " + resultSet.getString("review_text"));
                    System.out.println("Review Date: " + resultSet.getTimestamp("review_date"));
                    System.out.println("-------------------------");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while viewing user's reviews.");
            e.printStackTrace();
        }
    }

    public static void showPaymentsByUser(String email) {
        try {
            Connection conn = dbConnection.getConnection();
            int userId = getUserIdByEmail(email);
            if (userId == -1) {
                System.out.println("User not found.");
                return;
            }

            String query = "SELECT p.id AS payment_id, p.amount, p.payment_date " +
                    "FROM payments p " +
                    "JOIN bookings b ON p.booking_id = b.id " +
                    "WHERE b.user_id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("No payments found for the user.");
            } else {
                while (resultSet.next()) {
                    System.out.println("Payment ID: " + resultSet.getInt("payment_id"));
                    System.out.println("Amount: " + resultSet.getDouble("amount"));
                    System.out.println("Payment Date: " + resultSet.getTimestamp("payment_date"));
                    System.out.println("-------------------------");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while showing user's payments.");
            e.printStackTrace();
        }
    }





    
    // ----------------------------------------------------------
    // ---------------------- Admin Methods ----------------------
    // ----------------------------------------------------------
    public static void viewAllUsers() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("User ID: " + resultSet.getInt("id"));
                System.out.println("Email: " + resultSet.getString("email"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Phone: " + resultSet.getString("phoneNumber"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.out.println("(ADMIN) An error occurred while viewing all users.");
            e.printStackTrace();
        }
    }

    public static void viewAllPayments() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM payments";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Payment ID: " + resultSet.getInt("id"));
                System.out.println("Booking ID: " + resultSet.getInt("booking_id"));
                System.out.println("Amount: " + resultSet.getDouble("amount"));
                System.out.println("Payment Date: " + resultSet.getTimestamp("payment_date"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.out.println("(ADMIN) An error occurred while viewing all payments.");
            e.printStackTrace();
        }
    }

    public static void viewAllReviews() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM reviews";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Review ID: " + resultSet.getInt("id"));
                System.out.println("User ID: " + resultSet.getInt("user_id"));
                System.out.println("Car ID: " + resultSet.getInt("car_id"));
                System.out.println("Rating: " + resultSet.getInt("rating"));
                System.out.println("Review Text: " + resultSet.getString("review_text"));
                System.out.println("Review Date: " + resultSet.getTimestamp("review_date"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.out.println("(ADMIN) An error occurred while viewing all reviews.");
            e.printStackTrace();
        }
    }

    public static void adminLogin() {
        System.out.println("Enter your email:");
        String email = scanner.nextLine();
        System.out.println("Enter your password:");
        String password = scanner.nextLine();

        if (email.equals("admin@carrental.com") && password.equals("admin123")) {
            System.out.println("Welcome Admin");
            while (true) {
                System.out.println("\nAdmin Menu:");
                System.out.println("1. View All Users");
                System.out.println("2. View All Cars");
                System.out.println("3. View All Bookings");
                System.out.println("4. View All Payments");
                System.out.println("5. View All Reviews");
                System.out.println("6. Logout");
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        viewAllUsers();
                        break;
                    case 2:
                        viewAllCars();
                        break;
                    case 3:
                        viewAllBookings();
                        break;
                    case 4:
                        viewAllPayments();
                        break;
                    case 5:
                        viewAllReviews();
                        break;
                    case 6:
                        System.out.println("Logged out.");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } else {
            System.out.println("Admin login failed. Please check your credentials.");
        }
    }

    public static void userAction() {
        clearScreen();
        System.out.println("Are you a new user or an existing user?");
        System.out.println("1. New User (Register)");
        System.out.println("2. Existing User (Login)");

        int userChoice = Integer.parseInt(scanner.nextLine());

        switch (userChoice) {
            case 1:
                if (registerUser()) {
                    System.out.println("Registration successful.");
                } else {
                    System.out.println("Registration failed.");
                }
                break;
            case 2:
                userLogin();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    public static void adminAction() {
        clearScreen();
        System.out.println("Admin Login");

        adminLogin();
    }

    public static void viewAllBookings() {
        try {
            Connection conn = dbConnection.getConnection();
            String query = "SELECT * FROM bookings";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("Booking ID: " + resultSet.getInt("id"));
                System.out.println("User ID: " + resultSet.getInt("user_id"));
                System.out.println("Car ID: " + resultSet.getInt("car_id"));
                System.out.println("Booking Date: " + resultSet.getTimestamp("booking_date"));
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.out.println("(ADMIN) An error occurred while viewing all bookings.");
            e.printStackTrace();
        }
    }

    public static int getUserIdByEmail(String email) {
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
            System.out.println("An error occurred while getting user ID by email.");
            e.printStackTrace();
            return -1;
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nWelcome to Car Rental System");
            System.out.println("Are you a User or an Admin?");
            System.out.println("1. User");
            System.out.println("2. Admin");
            System.out.println("3. Exit");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    userAction();
                    break;
                case 2:
                    adminAction();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}