package tpp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerService {
    private Connection connection;

    public CustomerService(Connection connection) {
        this.connection = connection;
    }

    public int registerCustomer(String name) throws SQLException {
        PreparedStatement insertCustomer = connection.prepareStatement(
            "INSERT INTO customers (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insertCustomer.setString(1, name);
        insertCustomer.executeUpdate();
        
        ResultSet generatedKeys = insertCustomer.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }
        throw new SQLException("فشل تسجيل الزبون.");
    }
}
