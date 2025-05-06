package tpp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OwnerDashboardFrame extends JFrame {

    private Connection connection;
    private int ownerId;
    private HouseService houseService;
    private NegotiationService negotiationService; // Needed for OwnerReviewFrame

    private JTable housesTable;
    private DefaultTableModel tableModel;
    private JButton addHouseButton;
    private JButton reviewOffersButton;
    private JButton refreshButton; // Added refresh button

    public OwnerDashboardFrame(Connection connection, int ownerId) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
        this.ownerId = ownerId;
        this.houseService = new HouseService(connection);
        // Initialize NegotiationService, assuming it takes Connection
        this.negotiationService = new NegotiationService(connection);

        setTitle("لوحة تحكم المالك - المستخدم ID: " + ownerId);
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Or DISPOSE_ON_CLOSE if login should reappear
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadOwnerHouses();
    }

    private void initComponents() {
        // Table to display owner's houses
        tableModel = new DefaultTableModel(new String[]{"ID المنزل", "الموقع", "السعر", "المدة (Min-Max)", "Max Negot."}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        housesTable = new JTable(tableModel);
        housesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow selecting only one row
        JScrollPane scrollPane = new JScrollPane(housesTable);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Add gaps
        addHouseButton = new JButton("➕ إضافة منزل جديد");
        reviewOffersButton = new JButton("✉️ مراجعة عروض المنزل المحدد");
        refreshButton = new JButton("🔄 تحديث القائمة"); // Added refresh button

        addHouseButton.addActionListener(e -> openAddHouseFrame());
        reviewOffersButton.addActionListener(e -> openReviewOffersFrame());
        refreshButton.addActionListener(e -> loadOwnerHouses()); // Add action for refresh

        buttonPanel.add(addHouseButton);
        buttonPanel.add(reviewOffersButton);
        buttonPanel.add(refreshButton);

        // Add components to frame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        // Add some padding around the main content
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void loadOwnerHouses() {
        try {
            List<House> ownerHouses = houseService.getHousesByOwner(ownerId);
            // Clear previous data
            tableModel.setRowCount(0);

            if (ownerHouses.isEmpty()) {
                // Optionally display a message if no houses
                tableModel.addRow(new Object[]{"لا يوجد", "منازل", "مسجلة", "باسمك", "حالياً"});
                 reviewOffersButton.setEnabled(false); // Disable review button if no houses
            } else {
                for (House house : ownerHouses) {
                    tableModel.addRow(new Object[]{
                            house.getId(),
                            house.getLocation(),
                            house.getPrice(),
                            house.getMinDuration() + " - " + house.getMaxDuration(),
                            house.getMaxNegotiationCount()
                    });
                }
                 reviewOffersButton.setEnabled(true); // Enable review button if houses exist
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ فشل في تحميل منازل المالك:\n" + e.getMessage(),
                    "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openAddHouseFrame() {
        // Open the frame for adding a new house
        // We need to pass the ownerId and connection
        AddHouseFrame addFrame = new AddHouseFrame(connection, ownerId);
        addFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app
         // Add a listener to refresh the table when the AddHouseFrame closes
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadOwnerHouses(); // Refresh the list after adding
            }
        });
        addFrame.setVisible(true);
    }

    private void openReviewOffersFrame() {
        int selectedRow = housesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "الرجاء تحديد منزل من القائمة أولاً لمراجعة عروضه.",
                    "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if the selected row is the "no houses" message
         Object idValue = tableModel.getValueAt(selectedRow, 0);
         if (idValue instanceof String && idValue.equals("لا يوجد")) {
              JOptionPane.showMessageDialog(this,
                    "لا يمكنك مراجعة عروض لصف 'لا يوجد منازل'.",
                    "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
         }


        try {
            // Get the house ID from the selected row (first column)
            int houseId = (int) tableModel.getValueAt(selectedRow, 0);

             // Ensure negotiationService is initialized
            if (negotiationService == null) {
                 JOptionPane.showMessageDialog(this, "خطأ داخلي: خدمة التفاوض غير مهيأة.", "خطأ فني", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // Open the OwnerReviewFrame for the selected house
            OwnerReviewFrame reviewFrame = new OwnerReviewFrame(negotiationService, houseId);
             reviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app
            reviewFrame.setVisible(true);

        } catch (NumberFormatException e) {
             JOptionPane.showMessageDialog(this,
                    "❌ خطأ في قراءة معرف المنزل من الجدول.",
                    "خطأ داخلي", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ حدث خطأ غير متوقع عند فتح نافذة المراجعة:\n" + e.getMessage(),
                    "خطأ", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

     // Remove the main method if it exists
    /*
    public static void main(String[] args) {
         // Use MainApp.java
    }
    */
}