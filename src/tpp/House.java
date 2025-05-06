package tpp;

public class House {
    private int id;
    private int ownerId; // id مالك المنزل
    private String location;
    private double price;
    private int minDuration;
    private int maxDuration;
    private int maxNegotiationCount;

    public House(int id, int ownerId, String location, double price, int minDuration, int maxDuration, int maxNegotiationCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.location = location;
        this.price = price;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.maxNegotiationCount = maxNegotiationCount;
    }

    public House(int ownerId, String location, double price, int minDuration, int maxDuration, int maxNegotiationCount) {
        this.ownerId = ownerId;
        this.location = location;
        this.price = price;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.maxNegotiationCount = maxNegotiationCount;
    }

    // Getters و Setters
    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public int getMinDuration() { return minDuration; }
    public int getMaxDuration() { return maxDuration; }
    public int getMaxNegotiationCount() { return maxNegotiationCount; }

    public void setId(int id) { this.id = id; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public void setLocation(String location) { this.location = location; }
    public void setPrice(double price) { this.price = price; }
    public void setMinDuration(int minDuration) { this.minDuration = minDuration; }
    public void setMaxDuration(int maxDuration) { this.maxDuration = maxDuration; }
    public void setMaxNegotiationCount(int maxNegotiationCount) { this.maxNegotiationCount = maxNegotiationCount; }
}
