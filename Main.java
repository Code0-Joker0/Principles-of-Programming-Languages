import Gateway.*;
import Multithreading.PaymentTask;
import TransactionPackage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static TransactionManager manager = new TransactionManager();
    private static List<Thread> activeThreads = new ArrayList<>();
    private static int nextId = 1;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        manager.loadFromFile();
        int choice;
        do {
            printMenu();
            choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> createTransaction();
                case 2 -> processPayment();
                case 3 -> viewTransaction();
                case 4 -> updateTransaction();
                case 5 -> deleteTransaction();
                case 6 -> manager.displayAllTransactions();
                case 7 -> saveAndWait();
                case 8 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 8);
        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n===== Payment Gateway System =====");
        System.out.println("1. Create Transaction");
        System.out.println("2. Process Payment (start thread)");
        System.out.println("3. View Transaction");
        System.out.println("4. Update Transaction");
        System.out.println("5. Delete Transaction");
        System.out.println("6. View All Transactions");
        System.out.println("7. Save to File");
        System.out.println("8. Exit");
        System.out.println("==================================");
    }

    private static void createTransaction() {
        System.out.print("Enter user name: ");
        String name = sc.nextLine().trim();
        double amount = readDouble("Enter amount: ");
        String gateway = selectGateway();
        Transaction t = new Transaction(nextId++, name, amount, gateway);
        manager.addTransaction(t);
    }

    private static void processPayment() {
        int id = readInt("Enter Transaction ID to process: ");
        try {
            Transaction t = manager.getTransaction(id);
            if (!t.getStatus().equals("PENDING")) {
                System.out.println("Transaction already " + t.getStatus() + ". Cannot reprocess.");
                return;
            }
            PaymentGateway gateway = resolveGateway(t.getGatewayUsed());
            PaymentProcessor processor = new PaymentProcessor(gateway);
            PaymentTask task = new PaymentTask(t, processor, manager);
            Thread thread = new Thread(task, "Thread-TXN-" + id);
            activeThreads.add(thread);
            thread.start();
            System.out.println("Thread started: " + thread.getName() + " | Alive: " + thread.isAlive());
        } catch (TransactionNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void viewTransaction() {
        int id = readInt("Enter Transaction ID: ");
        try {
            System.out.println(manager.getTransaction(id));
        } catch (TransactionNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateTransaction() {
        int id = readInt("Enter Transaction ID to update: ");
        double amount = readDouble("Enter new amount: ");
        String gateway = selectGateway();
        try {
            manager.updateTransaction(id, amount, gateway);
        } catch (TransactionNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteTransaction() {
        int id = readInt("Enter Transaction ID to delete: ");
        try {
            manager.deleteTransaction(id);
        } catch (TransactionNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void saveAndWait() {
        System.out.println("Waiting for active threads to finish...");
        for (Thread t : activeThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        activeThreads.clear();
        manager.saveToFile();
    }

    private static String selectGateway() {
        System.out.println("Select Gateway: 1. Stripe  2. PayPal  3. Razorpay");
        int choice = readInt("Choice: ");
        return switch (choice) {
            case 2 -> "PayPal";
            case 3 -> "Razorpay";
            default -> "Stripe";
        };
    }

    private static PaymentGateway resolveGateway(String name) {
        return switch (name) {
            case "PayPal" -> new Paypal();
            case "Razorpay" -> new Razorpay();
            default -> new Stripe();
        };
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) {
            sc.nextLine();
            System.out.print("Invalid. " + prompt);
        }
        int val = sc.nextInt();
        sc.nextLine();
        return val;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextDouble()) {
            sc.nextLine();
            System.out.print("Invalid. " + prompt);
        }
        double val = sc.nextDouble();
        sc.nextLine();
        return val;
    }
}
