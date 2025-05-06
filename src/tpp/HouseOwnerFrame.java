package tpp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;

public class HouseOwnerFrame extends JFrame {
    private JTextField usernameField, locationField, priceField, minDurationField, maxDurationField, maxNegotiationsField;
    private JPasswordField passwordField;

    public HouseOwnerFrame() {
        setTitle("تسجيل مالك منزل");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 1, 10, 10));

        // إنشاء الحقول
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        locationField = new JTextField();
        priceField = new JTextField();
        minDurationField = new JTextField();
        maxDurationField = new JTextField();
        maxNegotiationsField = new JTextField();

        // إضافة المكونات للواجهة
        add(new JLabel("اسم المستخدم:"));
        add(usernameField);
        add(new JLabel("كلمة المرور:"));
        add(passwordField);
        add(new JLabel("موقع المنزل:"));
        add(locationField);
        add(new JLabel("سعر الكراء:"));
        add(priceField);
        add(new JLabel("أقل مدة كراء (أيام):"));
        add(minDurationField);
        add(new JLabel("أقصى مدة كراء (أيام):"));
        add(maxDurationField);
        add(new JLabel("عدد محاولات التفاوض:"));
        add(maxNegotiationsField);

        JButton registerButton = new JButton("تسجيل");
        add(registerButton);

        // عند الضغط على زر التسجيل
        registerButton.addActionListener(e -> registerOwner());

        setVisible(true);
    }

    private void registerOwner() {
        try {
            Connection connection = Database.getConnection();
            UserService userService = new UserService(connection);
            HouseService houseService = new HouseService(connection);

            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String location = locationField.getText();
            double price = Double.parseDouble(priceField.getText());
            int minDuration = Integer.parseInt(minDurationField.getText());
            int maxDuration = Integer.parseInt(maxDurationField.getText());
            int maxNegotiations = Integer.parseInt(maxNegotiationsField.getText());

            User owner = new User(username, password, "owner");
            int ownerId = userService.registerUser(owner);

            House house = new House(ownerId, location, price, minDuration, maxDuration, maxNegotiations);
            houseService.addHouse(house);

            JOptionPane.showMessageDialog(this, "✅ تم تسجيلك بنجاح! رقمك: " + ownerId, "نجاح", JOptionPane.INFORMATION_MESSAGE);
            connection.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ خطأ في الاتصال: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❌ تأكد من إدخال أرقام صحيحة في الحقول الرقمية.", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new HouseOwnerFrame();
    }
}
