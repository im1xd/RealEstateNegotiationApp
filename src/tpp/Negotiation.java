package tpp;

public class Negotiation {
    private int id;
    private int clientId;
    private int houseId;
    private double proposedPrice;
    private int proposedDuration;
    private String status = "pending";
    private int attemptCount = 0;

    public Negotiation(int clientId, int houseId, double proposedPrice, int proposedDuration) {
        this.clientId = clientId;
        this.houseId = houseId;
        this.proposedPrice = proposedPrice;
        this.proposedDuration = proposedDuration;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClientId() { return clientId; }
    public int getHouseId() { return houseId; }
    public double getProposedPrice() { return proposedPrice; }
    public void setProposedPrice(double price) { this.proposedPrice = price; }
    public int getProposedDuration() { return proposedDuration; }
    public void setProposedDuration(int duration) { this.proposedDuration = duration; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int count) { this.attemptCount = count; }
}