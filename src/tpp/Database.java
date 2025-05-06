package tpp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Connection connection;

    public static void connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/house_db";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ تم الاتصال بقاعدة البيانات!");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver غير موجود!");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }
}