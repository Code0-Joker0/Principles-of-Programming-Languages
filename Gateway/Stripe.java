package Gateway;

import TransactionPackage.Transaction;

public class Stripe extends AbstractPayment {
    @Override
    public void initiatePayment(Transaction t) {
        System.out.println("[Stripe] Authenticating card for user: " + t.getUserName());
        System.out.println("[Stripe] Charging $" + t.getAmount() + " via Stripe network...");
        System.out.println("[Stripe] Payment successful. Ref ID: STR-" + t.getTransactionId());
        t.setStatus("SUCCESS");
        printDetails(t);
    }
}
