package tpp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HouseService {
    private Connection connection;

    public HouseService(Connection connection) {
        this.connection = connection;
    }

 
    // جلب المنازل الخاصة بمالك معين
    public List<House> getHousesByOwner(int ownerId) throws SQLException {
        List<House> houses = new ArrayList<>();
        String sql = "SELECT * FROM houses WHERE owner_id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, ownerId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            House house = new House(
                rs.getInt("id"),
                rs.getInt("owner_id"),
                rs.getString("location"),
                rs.getDouble("price"),
                rs.getInt("min_duration"),
                rs.getInt("max_duration"),
                rs.getInt("max_negotiations")
            );
            houses.add(house);
        }
        return houses;
    }

    // جلب جميع المنازل المتوفرة للزبون
 // داخل كلاس HouseService
        // ... الكود الحالي ...

    public List<House> getAllAvailableHouses() throws SQLException {
        List<House> houses = new ArrayList<>();
        String sql = "SELECT * FROM houses WHERE available = true";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                House house = new House(
                    rs.getInt("id"),
                    rs.getInt("owner_id"),
                    rs.getString("location"),
                    rs.getDouble("price"),
                    rs.getInt("min_duration"),
                    rs.getInt("max_duration"),
                    rs.getInt("max_negotiations")
                );
                houses.add(house);
            }
        }
        return houses;
    }
        // إضافة منزل جديد

        public void addHouse(House house) throws SQLException {
            String sql = "INSERT INTO houses (owner_id, location, price, min_duration, max_duration, max_negotiations) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, house.getOwnerId());
                stmt.setString(2, house.getLocation());
                stmt.setDouble(3, house.getPrice());
                stmt.setInt(4, house.getMinDuration());
                stmt.setInt(5, house.getMaxDuration());
                stmt.setInt(6, house.getMaxNegotiationCount());
                stmt.executeUpdate();
            }
        }
        
    }


