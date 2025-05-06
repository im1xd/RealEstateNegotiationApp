package tpp;

import java.sql.SQLException;

public class HouseOwnerAgent {
    private int ownerId;
    private NegotiationService negotiationService;

    public HouseOwnerAgent(int ownerId, NegotiationService negotiationService) {
        this.ownerId = ownerId;
        this.negotiationService = negotiationService;
    }

    public int getOwnerId() {
        return ownerId;
    }

    // استقبال العرض من الزبون
    public void receiveOffer(int negotiationId, ClientAgent clientAgent, int houseId, double proposedPrice, int proposedDuration) throws SQLException {
        // إنشاء التفاوض (بناءً على المعطيات التي تم إرسالها من الزبون)
        Negotiation negotiation = negotiationService.getNegotiation(negotiationId);

        // تنفيذ التحقق من العرض
        if (negotiation != null) {
            negotiation.setStatus("Offer Received");

            // قبول العرض إذا كان السعر مناسبًا
            if (proposedPrice <= 5000) { // على سبيل المثال، عرض السعر أقل من 5000 يتم قبوله
                acceptNegotiation(negotiation);
            } else {
                rejectNegotiation(negotiation);
            }
        }
    }

    // قبول التفاوض
    public void acceptNegotiation(Negotiation negotiation) {
        negotiation.setStatus("Accepted");
        System.out.println("تم قبول العرض من قبل صاحب المنزل.");
    }

    // رفض التفاوض
    public void rejectNegotiation(Negotiation negotiation) {
        negotiation.setStatus("Rejected");
        System.out.println("تم رفض العرض من قبل صاحب المنزل.");
    }
}
