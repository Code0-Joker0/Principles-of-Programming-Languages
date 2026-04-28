package Gateway;

import TransactionPackage.Transaction;

public class Razorpay implements PaymentGateway {
    @Override
    public void initiatePayment(Transaction t) {
        System.out.println("[Razorpay] Initiating UPI/bank transfer for: " + t.getUserName());
        System.out.println("[Razorpay] Processing ₹" + t.getAmount() + " via Razorpay gateway...");
        System.out.println("[Razorpay] Payment confirmed. Ref ID: RZP-" + t.getTransactionId());
        t.setStatus("SUCCESS");
    }
}
