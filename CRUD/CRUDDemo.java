package CRUD;

import TransactionPackage.*;

// Standalone demo to test CRUD operations without the full console UI
public class CRUDDemo {
    public static void main(String[] args) throws TransactionNotFoundException {
        TransactionManager manager = new TransactionManager();

        manager.addTransaction(new Transaction(1, "Alice", 500.0, "Stripe"));
        manager.addTransaction(new Transaction(2, "Bob", 1200.0, "PayPal"));
        manager.addTransaction(new Transaction(3, "Charlie", 300.0, "Razorpay"));

        System.out.println("--- All Transactions ---");
        manager.displayAllTransactions();

        manager.updateTransaction(1, 750.0, "Razorpay");
        System.out.println("\n--- After Update (ID 1) ---");
        System.out.println(manager.getTransaction(1));

        manager.deleteTransaction(2);
        System.out.println("\n--- After Delete (ID 2) ---");
        manager.displayAllTransactions();

        manager.saveToFile();
    }
}
