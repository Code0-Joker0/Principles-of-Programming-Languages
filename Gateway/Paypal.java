package Gateway;

import TransactionPackage.Transaction;

public class Paypal extends AbstractPayment {
    @Override
    public void initiatePayment(Transaction t) {
        System.out.println("[PayPal] Verifying PayPal account for: " + t.getUserName());
        System.out.println("[PayPal] Transferring $" + t.getAmount() + " via PayPal balance...");
        System.out.println("[PayPal] Transfer complete. Ref ID: PP-" + t.getTransactionId());
        t.setStatus("SUCCESS");
        printDetails(t);
    }
}
