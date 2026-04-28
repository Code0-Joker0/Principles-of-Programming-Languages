package Multithreading;

import Gateway.PaymentProcessor;
import TransactionPackage.Transaction;
import TransactionPackage.TransactionNotFoundException;
import TransactionPackage.TransactionManager;

public class PaymentTask implements Runnable {
    private Transaction transaction;
    private PaymentProcessor processor;
    private TransactionManager manager;

    public PaymentTask(Transaction transaction, PaymentProcessor processor, TransactionManager manager) {
        this.transaction = transaction;
        this.processor = processor;
        this.manager = manager;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Starting payment for Transaction ID: " + transaction.getTransactionId());
        try {
            Thread.sleep(1000);
            processor.process(transaction); // sets status on the transaction object directly
            System.out.println("[" + threadName + "] Completed Transaction ID: " + transaction.getTransactionId() + " | Status: " + transaction.getStatus());
        } catch (InterruptedException e) {
            try {
                manager.updateStatus(transaction.getTransactionId(), "FAILED");
            } catch (TransactionNotFoundException ex) {
                System.out.println("[" + threadName + "] Could not mark FAILED: " + ex.getMessage());
            }
            System.out.println("[" + threadName + "] Transaction " + transaction.getTransactionId() + " interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[" + threadName + "] Unexpected error: " + e.getMessage());
        }
    }
}
