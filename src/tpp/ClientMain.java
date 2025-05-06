package tpp;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. الاتصال بقاعدة البيانات
                    Database.connect();
                    
                    // 2. الحصول على الاتصال
                    Connection connection = Database.getConnection();
                    
                    // 3. إنشاء واجهة المستخدم
                    LoginRegisterFrame frame = new LoginRegisterFrame(connection);
                   
                    frame.setVisible(true);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null,
                            "❌ فشل في الاتصال بقاعدة البيانات:\n" + e.getMessage(),
                            "خطأ", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }
}