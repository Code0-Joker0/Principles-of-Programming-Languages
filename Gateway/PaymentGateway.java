package Gateway;

import TransactionPackage.Transaction;

public interface PaymentGateway {
    void initiatePayment(Transaction t); // passed object of transaction class
}

