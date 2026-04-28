package TransactionPackage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private int transactionId;
    private String userName;
    private double amount;
    private String status;       // PENDING / SUCCESS / FAILED
    private String gatewayUsed;
    private String timestamp;

    public Transaction(int transactionId, String userName, double amount, String gatewayUsed) {
        this.transactionId = transactionId;
        this.userName = userName;
        this.amount = amount;
        this.gatewayUsed = gatewayUsed;
        this.status = "PENDING";
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public int getTransactionId()       { return transactionId; }
    public String getUserName()         { return userName; }
    public double getAmount()           { return amount; }
    public String getStatus()           { return status; }
    public String getGatewayUsed()      { return gatewayUsed; }
    public String getTimestamp()        { return timestamp; }

    public void setStatus(String status)           { this.status = status; }
    public void setAmount(double amount)           { this.amount = amount; }
    public void setGatewayUsed(String gatewayUsed) { this.gatewayUsed = gatewayUsed; }

    @Override
    public String toString() {
        return String.format("ID:%-4d | User: %-10s | Amount: $%-8.2f | Gateway: %-10s | Status: %-7s | %s",
                transactionId, userName, amount, gatewayUsed, status, timestamp);
    }
}
