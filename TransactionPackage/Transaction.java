package TransactionPackage;

public class Transaction {
    private int transactionId;
    private int userId;
    private int gatewayId;
    private double amount;
    private String status;

    public Transaction(int transactionId, int userId, int gatewayId, double amount, String status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.gatewayId = gatewayId;
        this.amount = amount;
        this.status = status;
    }

    public int getTransactionId() { return transactionId; }
    public int getUserId()        { return userId; }
    public int getGatewayId()     { return gatewayId; }
    public double getAmount()     { return amount; }
    public String getStatus()     { return status; }

    public void setUserId(int userId)     { this.userId = userId; }
    public void setGatewayId(int gId)     { this.gatewayId = gId; }
    public void setAmount(double amount)  { this.amount = amount; }
    public void setStatus(String status)  { this.status = status; }
}
