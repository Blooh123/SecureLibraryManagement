package com.library.librarymanagement.DB;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.*;

public class Database {
    static Connection connection;
    static Statement statement;
    static String DB_NAME = "secureLibrary";
    static String DB_URL = "jdbc:mysql://localhost/";
    static String USER = "root";
    static String PASS = "";
    static String value;
    static Object[][] list;
    static Object[][] view;

    public void display(SQLException e) {
        System.out.println("SQLEXCEPTION " + e.getMessage());
        System.out.println("SQLSTATE " + e.getSQLState());
        System.out.println("VENDORERROR " + e.getErrorCode());
    }

    public Database() {
        try {
            createDB();
        } catch (Exception e) {
            System.err.println("Unable to find Load");
            System.exit(1);
        }
    }

    public void connectToDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureLibrary", "root", "");
            statement = connection.createStatement();
        } catch (SQLException e) {
            display(e);
        }
    }

    public void createDB() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            String sql = "CREATE DATABASE if not exists secureLibrary";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        String URL = "jdbc:mysql://localhost/secureLibrary";
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            Statement stmt = conn.createStatement();

            // User table
            String users = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(255) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "role VARCHAR(50) NOT NULL" +
                    ")";
            stmt.executeUpdate(users);

            // Books table
            String books = "CREATE TABLE IF NOT EXISTS books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255) NOT NULL," +
                    "author VARCHAR(255) NOT NULL," +
                    "genre VARCHAR(100)," +
                    "availability BOOLEAN DEFAULT TRUE" +
                    ")";
            stmt.executeUpdate(books);

            // Borrow Records table
            String borrowRecords = "CREATE TABLE IF NOT EXISTS borrow_records (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "book_id INT NOT NULL," +
                    "user_id INT NOT NULL," +
                    "borrow_date DATE NOT NULL," +
                    "due_date DATE NOT NULL," +
                    "return_date DATE," +
                    "fine DECIMAL(10, 2) DEFAULT 0," +
                    "FOREIGN KEY (book_id) REFERENCES books(id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")";
            stmt.executeUpdate(borrowRecords);

            connectToDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getRowCount(ResultSet resultSet) throws SQLException {
        int rowCount;
        int currentRow = resultSet.getRow();
        resultSet.last();
        rowCount = resultSet.getRow();
        if (currentRow == 0) {
            resultSet.beforeFirst();
        } else {
            resultSet.absolute(currentRow);
        }
        return rowCount;
    }

    public static Object[][] getList(String query) {
        try {
            ResultSet rs = statement.executeQuery(query);
            int numRows = getRowCount(rs);
            int numColumns = rs.getMetaData().getColumnCount();
            list = new Object[numRows][numColumns];
            int rowIndex = 0;
            while (rs.next()) {
                for (int colIndex = 0; colIndex < numColumns; colIndex++) {
                    list[rowIndex][colIndex] = rs.getObject(colIndex + 1);
                }
                rowIndex++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getValue(String query) throws SQLException {
        connectToDB();
        try {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                value = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
            statement.close();
        }
        return value;
    }

    public static void prepare(String query, String... params) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addBook(String title, String author, String genre) {
        String query = "INSERT INTO books (title, author, genre, availability) VALUES (?, ?, ?, true)";
        prepare(query, title, author, genre);
        System.out.println("Book added successfully!");
    }

    public static void updateBookAvailability(int bookId, boolean isAvailable) {
        String query = "UPDATE books SET availability = ? WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, bookId);
            pstmt.executeUpdate();
            System.out.println("Book availability updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Object[][] viewBooks() {
        String query = "SELECT * FROM books";
        return getList(query);
    }

    public static void deleteBook(int bookId) {
        String query = "DELETE FROM books WHERE id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, bookId);
            pstmt.executeUpdate();
            System.out.println("Book deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteUser(int id) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?")){
            preparedStatement.setInt(1,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }
    public  void addUser(String userName, String password, String role) throws SQLException {
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password, role) VALUES(?,SHA1(?),?)",PreparedStatement.RETURN_GENERATED_KEYS)){
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,role);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
            Platform.runLater(()->{
                Alert alert =new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Something went wrong!");
                alert.showAndWait();
            });
        }finally {
            connection.close();
        }
    }
    public void updateUser(int id, String userName, String password, String role) throws SQLException{
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?, password = SHA1(?), role = ? WHERE id = ?")) {
            preparedStatement.setString(1,userName);
            preparedStatement.setString(2,password);
            preparedStatement.setString(3,role);
            preparedStatement.setInt(4,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }
    public void updateUser(int id, String username, String role) throws SQLException {
        connectToDB();
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ?,role = ? WHERE id = ?")) {
            preparedStatement.setString(1,username);
            preparedStatement.setString(2,role);
            preparedStatement.setInt(3,id);
            preparedStatement.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    public boolean checkIfUsernameExists(String username) throws SQLException{
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username = ?")){
            preparedStatement.setString(1, username);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }
    public boolean checkIfActualUser(int id, String username) throws SQLException{
        connectToDB();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE id = ?")){
            preparedStatement.setInt(1, id);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }
        return false;
    }

}
