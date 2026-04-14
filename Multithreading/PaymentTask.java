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
            Thread.sleep(1000); // simulate processing delay
            processor.process(transaction);
            manager.updateStatus(transaction.getTransactionId(), transaction.getStatus());
            System.out.println("[" + threadName + "] Completed Transaction ID: " + transaction.getTransactionId() + " | Status: " + transaction.getStatus());
        } catch (InterruptedException e) {
            transaction.setStatus("FAILED");
            System.out.println("[" + threadName + "] Transaction " + transaction.getTransactionId() + " interrupted.");
            Thread.currentThread().interrupt();
        } catch (TransactionNotFoundException e) {
            System.out.println("[" + threadName + "] Error: " + e.getMessage());
        }
    }
}
