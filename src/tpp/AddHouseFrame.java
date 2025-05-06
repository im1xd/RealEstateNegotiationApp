package tpp;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.*; // Not needed if using lambdas
import java.sql.Connection;
import java.sql.SQLException;

// Renamed from HouseOwnerFrame
public class AddHouseFrame extends JFrame {
    private JTextField locationField, priceField, minDurationField, maxDurationField, maxNegotiationsField;
    // No username/password fields needed here, owner is already logged in

    private Connection connection;
    private int ownerId; // Added ownerId field
    private HouseService houseService; // Added HouseService

    // Modified Constructor
    public AddHouseFrame(Connection connection, int ownerId) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
        this.ownerId = ownerId;
        this.houseService = new HouseService(connection); // Initialize service

        setTitle("إضافة منزل جديد - المالك ID: " + ownerId);
        setSize(400, 400); // Adjusted size
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Changed below
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null); // Center the frame
        setLayout(new GridLayout(0, 2, 10, 10)); // Use GridLayout
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Add padding


        // إنشاء الحقول
        locationField = new JTextField();
        priceField = new JTextField();
        minDurationField = new JTextField();
        maxDurationField = new JTextField();
        maxNegotiationsField = new JTextField();

        // إضافة المكونات للواجهة (Label then Field)
        add(new JLabel("موقع المنزل:"));
        add(locationField);
        add(new JLabel("سعر الكراء المقترح:"));
        add(priceField);
        add(new JLabel("أقل مدة كراء (أشهر):"));
        add(minDurationField);
        add(new JLabel("أقصى مدة كراء (أشهر):"));
        add(maxDurationField);
        add(new JLabel("عدد محاولات التفاوض:"));
        add(maxNegotiationsField);

        JButton addButton = new JButton("💾 إضافة المنزل");
        // Add empty label as placeholder to align button correctly in GridLayout
        add(new JLabel());
        add(addButton);

        // عند الضغط على زر الإضافة
        addButton.addActionListener(e -> addHouse());

        // setVisible(true); // Visibility should be controlled by the caller (OwnerDashboardFrame)
    }

    // Modified method to add house, not register owner
    private void addHouse() {
        try {
            // Validate connection and service
             if (connection == null || connection.isClosed() || houseService == null) {
                 JOptionPane.showMessageDialog(this, "خطأ داخلي: الاتصال أو الخدمة غير متاحة.", "خطأ فني", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // Get data from fields
            String location = locationField.getText().trim();
            String priceText = priceField.getText().trim();
            String minDurationText = minDurationField.getText().trim();
            String maxDurationText = maxDurationField.getText().trim();
            String maxNegotiationsText = maxNegotiationsField.getText().trim();

            // Basic Validation
            if (location.isEmpty() || priceText.isEmpty() || minDurationText.isEmpty() || maxDurationText.isEmpty() || maxNegotiationsText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى ملء جميع الحقول.", "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse numeric fields with error handling
            double price = Double.parseDouble(priceText);
            int minDuration = Integer.parseInt(minDurationText);
            int maxDuration = Integer.parseInt(maxDurationText);
            int maxNegotiations = Integer.parseInt(maxNegotiationsText);

            // Additional validation (e.g., minDuration <= maxDuration)
             if (minDuration <= 0 || maxDuration <= 0 || price <= 0 || maxNegotiations < 0) {
                  JOptionPane.showMessageDialog(this, "الرجاء إدخال قيم موجبة للسعر والمدة وعدد المحاولات (يمكن أن يكون 0).", "قيم غير صالحة", JOptionPane.WARNING_MESSAGE);
                return;
             }
             if (minDuration > maxDuration) {
                 JOptionPane.showMessageDialog(this, "المدة الدنيا يجب أن تكون أقل من أو تساوي المدة القصوى.", "خطأ في المدة", JOptionPane.WARNING_MESSAGE);
                return;
             }


            // Create House object (using the constructor that takes ownerId)
            House house = new House(ownerId, location, price, minDuration, maxDuration, maxNegotiations);

            // Use HouseService to add the house
            houseService.addHouse(house);

            JOptionPane.showMessageDialog(this, "✅ تم إضافة المنزل بنجاح!", "نجاح", JOptionPane.INFORMATION_MESSAGE);

            // Close this frame after successful addition
            this.dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❌ تأكد من إدخال أرقام صحيحة في حقول السعر والمدة والمحاولات.", "خطأ في الإدخال", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ خطأ في قاعدة البيانات عند إضافة المنزل:\n" + ex.getMessage(), "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
             ex.printStackTrace();
        } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "❌ حدث خطأ غير متوقع:\n" + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
             ex.printStackTrace();
        }
    }

    // Remove the main method
    /*
    public static void main(String[] args) {
        // new HouseOwnerFrame(); // Old way
        // Use MainApp.java now
    }
    */
}