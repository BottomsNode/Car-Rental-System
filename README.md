# Car Rental System

The Car Rental System is a Java-based application that allows users to view, book, and review cars. Admins can manage users, cars, bookings, payments, and reviews. The system is designed to provide a seamless experience for both users and administrators.

## Features

### User Features
- **Register:** Users can create an account.
- **Login:** Users can log in to their account.
- **View Cars:** Users can view available cars.
- **Book Cars:** Users can book cars.
- **Make a Review:** Users can add reviews for cars.
- **Show Bookings:** Users can view their bookings.
- **Show Reviews:** Users can view their reviews.
- **Show Payments:** Users can view their payments.
- **Logout:** Users can log out of their account.

### Admin Features
- **View All Users:** Admins can view all registered users.
- **View All Cars:** Admins can view all cars in the system.
- **View All Bookings:** Admins can view all bookings made by users.
- **View All Payments:** Admins can view all payments made by users.
- **View All Reviews:** Admins can view all reviews made by users.
- **Logout:** Admins can log out of their account.

## System Requirements

- Java Development Kit (JDK) 8 or higher
- MySQL Database

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/car-rental-system.git
2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).

3. Set up the MySQL database:
 - Create a database named carrentalsystem.
 - Import the provided SQL file to create the necessary tables.

4. Update the database connection details in DatabaseConnection.java:
   private static final String URL = "jdbc:mysql://localhost:3306/carrentalsystem";
   private static final String USER = "your_mysql_username";
   private static final String PASSWORD = "your_mysql_password";

5. Run the CarRentalSystem.java file to start the application.

# Usage
## User Actions:
  - New users can register by choosing the "New User (Register)" option.
  - Existing users can log in by choosing the "Existing User (Login)" option.
  - After logging in, users can view cars, book cars, make reviews, show bookings, show reviews, and show payments.
  - Users can log out by selecting the "Logout" option.

## Admin Actions:
  - Admins can log in using the credentials:
  - Email: admin@carrental.com
  - Password: admin123
  - After logging in, admins can view all users, view all cars, view all bookings, view all payments, and view all reviews.
  - Admins can log out by selecting the "Logout" option.

# Database Schema
## Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phoneNumber VARCHAR(255),
    password VARCHAR(255)
);

## Cars Table
CREATE TABLE cars (
    id INT AUTO_INCREMENT PRIMARY KEY,
    make VARCHAR(255),
    model VARCHAR(255),
    year INT,
    price DOUBLE
);

## Bookings Table
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    car_id INT,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id)
);

## Payments Table
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT,
    amount DOUBLE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

## Reviews Table
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    car_id INT,
    rating INT,
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (car_id) REFERENCES cars(id)
);

# Thank You

Thank you for checking out the Car Rental System project! We hope you find it useful and easy to use. If you have any questions, feedback, or contributions, please feel free to reach out. Happy coding and safe travels!
