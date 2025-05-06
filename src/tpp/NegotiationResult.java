package tpp;

public class NegotiationResult {
    private boolean success;
    private String message;
    private boolean canRetry;

    public NegotiationResult(boolean success, String message, boolean canRetry) {
        this.success = success;
        this.message = message;
        this.canRetry = canRetry;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean canRetry() {
        return canRetry;
    }
}
