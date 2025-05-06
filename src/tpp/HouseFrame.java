package tpp;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HouseFrame extends JFrame {
    private HouseService houseService;
    private Connection connection;

    public HouseFrame(Connection connection) {
        this.connection = connection;
        this.houseService = new HouseService(connection);
        initializeUI();
        loadHouses();
    }

    private void initializeUI() {
        setTitle("المنازل المتاحة");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void loadHouses() {
        try {
            List<House> houses = houseService.getAllAvailableHouses();
            JPanel mainPanel = new JPanel(new BorderLayout());
            JPanel housesPanel = new JPanel();
            housesPanel.setLayout(new BoxLayout(housesPanel, BoxLayout.Y_AXIS));

            if (houses.isEmpty()) {
                housesPanel.add(new JLabel("لا توجد منازل متاحة حالياً", SwingConstants.CENTER));
            } else {
                for (House house : houses) {
                    housesPanel.add(createHouseCard(house));
                    housesPanel.add(Box.createVerticalStrut(15));
                }
            }

            JScrollPane scrollPane = new JScrollPane(housesPanel);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            add(mainPanel);
            revalidate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ فشل في تحميل المنازل: " + e.getMessage());
        }
    }

    private JPanel createHouseCard(House house) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createTitledBorder("معلومات المنزل"));

        // لوحة المعلومات
        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.add(new JLabel("المكان: " + house.getLocation()));
        infoPanel.add(new JLabel("السعر: " + house.getPrice() + " دينار"));
        infoPanel.add(new JLabel("المدة الأدنى: " + house.getMinDuration() + " أشهر"));
        infoPanel.add(new JLabel("المدة القصوى: " + house.getMaxDuration() + " أشهر"));

        // زر "تقديم عرض"
        JButton offerButton = new JButton("تقديم عرض");
        offerButton.addActionListener(e -> openNegotiationFrame(house.getId()));

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(offerButton, BorderLayout.SOUTH);

        return card;
    }
    private void openNegotiationFrame(int houseId) {
        // احصل على clientId من المستخدم الحالي (مثال: من UserService أو UserSession)
        User currentUser = UserSession.getCurrentUser(); // افترض أن UserSession يدير جلسة المستخدم
        int clientId = currentUser.getId();

        NegotiationService service = new NegotiationService(connection);
        new NegotiationFrame(service, houseId, clientId).setVisible(true);
    }
}