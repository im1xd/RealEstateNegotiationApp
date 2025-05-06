package tpp;

import java.sql.SQLException;
import java.util.Scanner;

import jade.core.Agent;

public class ClientAgent extends Agent {
    private NegotiationService negotiationService;

    public ClientAgent(NegotiationService negotiationService) {
        this.negotiationService = negotiationService;
    }

    public void negotiate(int clientId, int houseId) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("أدخل السعر الذي تود اقتراحه: ");
        double proposedPrice = scanner.nextDouble();

        System.out.print("أدخل مدة الإيجار (بالأشهر): ");
        int proposedDuration = scanner.nextInt();

        Negotiation negotiation = new Negotiation(clientId, houseId, proposedPrice, proposedDuration);

        try {
            negotiationService.startNegotiation(negotiation);
            System.out.println("✅ تم إرسال العرض بنجاح. في انتظار الرد من مالك المنزل.");
        } catch (SQLException e) {
            System.out.println("❌ خطأ أثناء إرسال العرض: " + e.getMessage());
        }
    }
}
