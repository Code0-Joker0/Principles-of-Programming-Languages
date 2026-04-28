package Gateway;

import TransactionPackage.Transaction;

public class Razorpay extends AbstractPayment {
    @Override
    public void initiatePayment(Transaction t) {
        System.out.println("[Razorpay] Initiating UPI/bank transfer for: " + t.getUserName());
        System.out.println("[Razorpay] Processing \u20b9" + t.getAmount() + " via Razorpay gateway...");
        System.out.println("[Razorpay] Payment confirmed. Ref ID: RZP-" + t.getTransactionId());
        t.setStatus("SUCCESS");
        printDetails(t);
    }
}
