package tpp;

import javax.swing.*;

import jade.core.Agent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

public class HouseOwnerConsoleApp extends Agent{

    public static void main(String[] args) {
        JFrame frame = new JFrame("تسجيل مالك المنزل");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(10, 2));

        JLabel usernameLabel = new JLabel("أدخل اسم المستخدم:");
        JTextField usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("أدخل كلمة المرور:");
        JPasswordField passwordField = new JPasswordField();

        JLabel locationLabel = new JLabel("أدخل موقع المنزل:");
        JTextField locationField = new JTextField();

        JLabel priceLabel = new JLabel("أدخل سعر الكراء:");
        JTextField priceField = new JTextField();

        JLabel minDurationLabel = new JLabel("أدخل أقل مدة كراء (بالأيام):");
        JTextField minDurationField = new JTextField();

        JLabel maxDurationLabel = new JLabel("أدخل أقصى مدة كراء (بالأيام):");
        JTextField maxDurationField = new JTextField();

        JLabel maxNegotiationsLabel = new JLabel("أدخل عدد محاولات التفاوض المسموح بها:");
        JTextField maxNegotiationsField = new JTextField();

        JButton submitButton = new JButton("تسجيل");

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        frame.add(usernameLabel);
        frame.add(usernameField);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(locationLabel);
        frame.add(locationField);
        frame.add(priceLabel);
        frame.add(priceField);
        frame.add(minDurationLabel);
        frame.add(minDurationField);
        frame.add(maxDurationLabel);
        frame.add(maxDurationField);
        frame.add(maxNegotiationsLabel);
        frame.add(maxNegotiationsField);
        frame.add(submitButton);
        frame.add(scrollPane);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection connection = Database.getConnection();
                    UserService userService = new UserService(connection);
                    HouseService houseService = new HouseService(connection);

                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    User owner = new User(username, password, "owner");
                    int ownerId = userService.registerUser(owner);
                    outputArea.append("✅ تم تسجيلك كمالك منزل! رقمك: " + ownerId + "\n");

                    String location = locationField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    int minDuration = Integer.parseInt(minDurationField.getText());
                    int maxDuration = Integer.parseInt(maxDurationField.getText());
                    int maxNegotiations = Integer.parseInt(maxNegotiationsField.getText());

                    House house = new House(ownerId, location, price, minDuration, maxDuration, maxNegotiations);
                    houseService.addHouse(house);
                    outputArea.append("✅ تم تسجيل المنزل بنجاح!\n");

                    connection.close();
                } catch (SQLException ex) {
                    outputArea.append("❌ خطأ في الاتصال أو التنفيذ: " + ex.getMessage() + "\n");
                } catch (NumberFormatException ex) {
                    outputArea.append("❌ خطأ في المدخلات: تأكد من إدخال القيم بشكل صحيح.\n");
                }
            }
        });

        frame.setVisible(true);
    }
}
