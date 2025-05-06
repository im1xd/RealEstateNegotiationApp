package tpp;

import java.sql.*; // Import all SQL classes
import java.util.ArrayList;
import java.util.List;
// Remove Scanner if not used for console input
// import java.util.Scanner;
import javax.swing.JOptionPane; // Keep for GUI messages

public class NegotiationService {
    private Connection connection;
    // Remove the Database db field if connection is passed directly
    // private Database db;

    // Constructor accepting Connection (preferred for dependency injection)
    public NegotiationService(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null for NegotiationService");
        }
        this.connection = connection;
    }

    // Constructor accepting Database (less preferred, couples service to Database class)
    // public NegotiationService(Database db) {
    //     if (db == null) {
    //         throw new IllegalArgumentException("Database cannot be null");
    //     }
    //     this.db = db;
    //     try {
    //         this.connection = db.getConnection(); // Get connection from Database instance
    //     } catch (SQLException e) {
    //          throw new RuntimeException("Failed to get connection from Database instance", e);
    //     }
    // }


    // *** Ensure Connection is Valid in methods ***
    private void ensureConnection() throws SQLException {
         if (connection == null || connection.isClosed()) {
              // Try to reconnect if using the Database instance approach,
              // otherwise throw an exception if only connection was provided.
              // if (db != null) {
              //      connection = db.getConnection();
              // } else {
                  throw new SQLException("Database connection is closed or null.");
              // }
         }
    }


    public NegotiationResult startNegotiation(Negotiation negotiation) throws SQLException {
        ensureConnection(); // Check connection

        // Use PreparedStatement for security and efficiency
        String houseSql = "SELECT price, min_duration, max_duration, max_negotiations FROM houses WHERE id = ?";
        try (PreparedStatement selectHouse = connection.prepareStatement(houseSql)) {
            selectHouse.setInt(1, negotiation.getHouseId());
            ResultSet resultSet = selectHouse.executeQuery();

            if (resultSet.next()) {
                double ownerPrice = resultSet.getDouble("price");
                int minDuration = resultSet.getInt("min_duration");
                int maxDuration = resultSet.getInt("max_duration");
                int maxNegotiations = resultSet.getInt("max_negotiations");

                // Get current attempt count (should be 0 or low for initial start)
                // It's better to query the last attempt count for this client/house pair
                int lastAttemptCount = getLastAttemptCount(negotiation.getClientId(), negotiation.getHouseId());
                int currentAttempt = lastAttemptCount + 1; // This is the new attempt number
                negotiation.setAttemptCount(currentAttempt);


                // Basic validation from house rules
                boolean priceOk = (negotiation.getProposedPrice() >= ownerPrice * 0.8); // Example: Allow 80% of price? Or exact match? Adjust logic.
                boolean durationOk = (negotiation.getProposedDuration() >= minDuration &&
                                      negotiation.getProposedDuration() <= maxDuration);

                // Check attempt limit
                 if (currentAttempt > maxNegotiations) {
                     negotiation.setStatus("rejected"); // Or a specific status like "attempts_exceeded"
                     saveNegotiationAttempt(negotiation); // Save the final rejected attempt
                     return new NegotiationResult(false, "❌ لقد تجاوزت الحد الأقصى لعدد محاولات التفاوض (" + maxNegotiations + ").", false);
                 }


                // Decision logic (Simplified: Requires exact match or better. Owner review is separate)
                // In a real system, this might just save as "PENDING" for owner review.
                // Let's assume for now it saves as PENDING unless attempt limit reached.
                 negotiation.setStatus("PENDING");
                 saveNegotiationAttempt(negotiation);
                 return new NegotiationResult(true, "✅ تم إرسال عرضك بنجاح. يرجى انتظار مراجعة المالك.", false); // Success here means offer sent, not accepted.

                /* // OLDER logic based on automatic acceptance/rejection (can be uncommented if needed)
                if (priceOk && durationOk) {
                    negotiation.setStatus("accepted"); // Auto-accept if conditions met? Risky.
                    saveNegotiationAttempt(negotiation);
                    // Maybe update house availability here?
                    return new NegotiationResult(true, "✅ تم قبول العرض تلقائياً!", false); // Indicate auto-acceptance
                } else {
                    // Handle rejection or pending status based on attempts
                     negotiation.setStatus("rejected"); // Or "pending"
                     saveNegotiationAttempt(negotiation);

                     String message = "❌ العرض مرفوض.";
                     if (!priceOk) message += " السعر المقترح ("+negotiation.getProposedPrice()+") أقل من المطلوب.";
                     if (!durationOk) message += " المدة ("+negotiation.getProposedDuration()+") خارج النطاق ["+minDuration+"-"+maxDuration+"].";

                    if (currentAttempt < maxNegotiations) {
                         return new NegotiationResult(false, message + " المحاولة (" + currentAttempt + "/" + maxNegotiations + ").", true);
                    } else {
                         return new NegotiationResult(false, message + " تم استهلاك جميع المحاولات.", false);
                    }
                }
                */

            } else {
                // House not found
                return new NegotiationResult(false, "❌ لم يتم العثور على المنزل بالمعرف المحدد.", false);
            }
        } // try-with-resources ensures PreparedStatement is closed
    }


     // Helper to get the last attempt count
     private int getLastAttemptCount(int clientId, int houseId) throws SQLException {
        ensureConnection();
        String sql = "SELECT MAX(attempt_count) FROM negotiations WHERE client_id = ? AND house_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, houseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Returns 0 if no previous attempts exist
            }
        }
        return 0; // Default to 0 if query fails or no records
     }


    // *** Standardized saveNegotiationAttempt ***
    private void saveNegotiationAttempt(Negotiation negotiation) throws SQLException {
        ensureConnection();
        // Use standardized column names matching Negotiation class properties
        String insertSQL = "INSERT INTO negotiations (" +
            "client_id, house_id, proposed_price, proposed_duration, status, attempt_count, negotiation_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, NOW())"; // Added timestamp

        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setInt(1, negotiation.getClientId());
            stmt.setInt(2, negotiation.getHouseId());
            stmt.setDouble(3, negotiation.getProposedPrice());
            stmt.setInt(4, negotiation.getProposedDuration());
            stmt.setString(5, negotiation.getStatus().toUpperCase()); // Standardize status to upper case
            stmt.setInt(6, negotiation.getAttemptCount());

            stmt.executeUpdate();
        } catch (SQLException e) {
            // Provide more context for debugging
            System.err.println("Error saving negotiation attempt for client " + negotiation.getClientId() + " on house " + negotiation.getHouseId());
            System.err.println("SQL failed: " + insertSQL);
            System.err.println("Error: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            e.printStackTrace();
            // Re-throw the exception to be handled by the caller
            throw e;
        }
    }

    // --- Other methods from your original code ---
    // Ensure they also check the connection and use PreparedStatement

    public Negotiation getNegotiation(int negotiationId) throws SQLException {
        ensureConnection();
        Negotiation negotiation = null;
        String sql = "SELECT * FROM negotiations WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, negotiationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                negotiation = new Negotiation(
                    rs.getInt("client_id"),
                    rs.getInt("house_id"),
                    rs.getDouble("proposed_price"), // Use correct column name
                    rs.getInt("proposed_duration") // Use correct column name
                );
                negotiation.setId(rs.getInt("id"));
                negotiation.setStatus(rs.getString("status"));
                negotiation.setAttemptCount(rs.getInt("attempt_count"));
                // Optionally load timestamp: negotiation.setTimestamp(rs.getTimestamp("negotiation_time"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching negotiation ID " + negotiationId + ": " + e.getMessage());
            throw e; // Re-throw
        }
        return negotiation;
    }

     public List<Negotiation> getAllNegotiationsForClient(int clientId, int houseId) throws SQLException {
        ensureConnection();
        List<Negotiation> list = new ArrayList<>();
        // Use correct column names
        String sql = "SELECT * FROM negotiations WHERE client_id = ? AND house_id = ? ORDER BY attempt_count ASC, negotiation_time ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, houseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Negotiation n = new Negotiation(
                    rs.getInt("client_id"),
                    rs.getInt("house_id"),
                    rs.getDouble("proposed_price"), // Corrected name
                    rs.getInt("proposed_duration") // Corrected name
                );
                n.setId(rs.getInt("id"));
                n.setStatus(rs.getString("status"));
                n.setAttemptCount(rs.getInt("attempt_count"));
                list.add(n);
            }
        } catch (SQLException e) {
             System.err.println("Error fetching negotiations for client " + clientId + ", house " + houseId + ": " + e.getMessage());
             throw e;
        }
        return list;
    }

     public List<Negotiation> getAllNegotiationsForHouse(int houseId) throws SQLException {
         ensureConnection();
        List<Negotiation> list = new ArrayList<>();
         // Use correct column names
        String sql = "SELECT n.*, u.username as client_username FROM negotiations n JOIN users u ON n.client_id = u.id WHERE n.house_id = ? ORDER BY n.status ASC, n.negotiation_time DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Negotiation n = new Negotiation(
                    rs.getInt("client_id"),
                    rs.getInt("house_id"),
                    rs.getDouble("proposed_price"), // Corrected name
                    rs.getInt("proposed_duration") // Corrected name
                );
                n.setId(rs.getInt("id"));
                n.setStatus(rs.getString("status"));
                n.setAttemptCount(rs.getInt("attempt_count"));
                // You could add client username to Negotiation object if needed
                // n.setClientUsername(rs.getString("client_username"));
                list.add(n);
            }
        } catch (SQLException e) {
             System.err.println("Error fetching negotiations for house " + houseId + ": " + e.getMessage());
             throw e;
        }
        return list;
    }

     // Method for owner to accept/reject
    public void updateNegotiationStatus(int negotiationId, String newStatus) throws SQLException {
         ensureConnection();
         String upperStatus = newStatus.toUpperCase(); // Standardize
         // Validate status
         if (!upperStatus.equals("ACCEPTED") && !upperStatus.equals("REJECTED") && !upperStatus.equals("PENDING")) {
              throw new IllegalArgumentException("Invalid status provided: " + newStatus);
         }

        String sql = "UPDATE negotiations SET status = ? WHERE id = ?";
        String selectHouseSql = "SELECT house_id FROM negotiations WHERE id = ?";
        int houseId = -1;

        // Need to get houseId first if accepting, to reject others
         if (upperStatus.equals("ACCEPTED")) {
              try (PreparedStatement stmt = connection.prepareStatement(selectHouseSql)) {
                   stmt.setInt(1, negotiationId);
                   ResultSet rs = stmt.executeQuery();
                   if (rs.next()) {
                        houseId = rs.getInt("house_id");
                   } else {
                        throw new SQLException("Negotiation ID " + negotiationId + " not found.");
                   }
              }
         }


        // Now update the status
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, upperStatus);
            stmt.setInt(2, negotiationId);
            int rowsAffected = stmt.executeUpdate();
             if (rowsAffected == 0) {
                 System.err.println("Warning: No negotiation found with ID " + negotiationId + " to update status.");
             }
        } catch (SQLException e) {
             System.err.println("Error updating negotiation " + negotiationId + " status to " + upperStatus + ": " + e.getMessage());
             throw e;
        }

        // If accepted, reject others and mark house as unavailable
        if (upperStatus.equals("ACCEPTED") && houseId != -1) {
            rejectOtherPendingOffersForHouse(houseId, negotiationId);
            markHouseAsUnavailable(houseId); // Add this method
        }
    }

    // Reject other PENDING offers for the same house
    private void rejectOtherPendingOffersForHouse(int houseId, int acceptedNegotiationId) throws SQLException {
        ensureConnection();
        // Only reject PENDING offers, leave previously accepted/rejected ones alone
        String query = "UPDATE negotiations SET status = 'REJECTED' " +
                       "WHERE house_id = ? AND id != ? AND status = 'PENDING'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, houseId);
            stmt.setInt(2, acceptedNegotiationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
             System.err.println("Error rejecting other offers for house " + houseId + ": " + e.getMessage());
             // Don't necessarily throw, the main update succeeded
        }
    }

     // Mark house as unavailable
    private void markHouseAsUnavailable(int houseId) throws SQLException {
        ensureConnection();
        String sql = "UPDATE houses SET available = false WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, houseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking house " + houseId + " as unavailable: " + e.getMessage());
            // Log error, but don't necessarily fail the whole operation
        }
    }


    // Getting max attempts (already seems okay, just add connection check)
    public int getMaxNegotiationAttempts(int houseId) throws SQLException {
        ensureConnection();
        String query = "SELECT max_negotiations FROM houses WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, houseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("max_negotiations");
                } else {
                    throw new SQLException("لم يتم العثور على المنزل بالمعرف المحدد: " + houseId);
                }
            }
        }
    }

    // Other methods like getAttemptCount, respondToNegotiation, incrementAttemptCount
    // are mostly covered by the logic above or might be redundant. Review if needed.

}