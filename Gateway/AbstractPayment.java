package Gateway;

import TransactionPackage.Transaction;

public abstract class AbstractPayment implements PaymentGateway {

    public void printDetails(Transaction t) {
        System.out.println("Transaction ID: " + t.getTransactionId());
        System.out.println("User: " + t.getUserName());
        System.out.println("Amount: Rs " + t.getAmount());
        System.out.println("Gateway: " + t.getGatewayUsed());
        System.out.println("Status: " + t.getStatus());
    }
}