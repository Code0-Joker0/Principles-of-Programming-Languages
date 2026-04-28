package Gateway;

import TransactionPackage.Transaction;

public class PaymentProcessor {
    private PaymentGateway gateway;

    public PaymentProcessor(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public void process(Transaction t) {
        gateway.initiatePayment(t);
    }
}
