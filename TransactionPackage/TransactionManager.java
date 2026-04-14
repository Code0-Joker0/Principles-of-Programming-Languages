package TransactionPackage;

import java.io.*;
import java.util.HashMap;

public class TransactionManager {
    private HashMap<Integer, Transaction> transactions = new HashMap<>();
    private static final String FILE_PATH = "transactions.txt";

    public synchronized void addTransaction(Transaction t) {
        transactions.put(t.getTransactionId(), t);
        System.out.println("Transaction added: " + t.getTransactionId());
    }

    public synchronized Transaction getTransaction(int id) throws TransactionNotFoundException {
        Transaction t = transactions.get(id);
        if (t == null) throw new TransactionNotFoundException(id);
        return t;
    }

    public synchronized void updateTransaction(int id, double newAmount, String newGateway)
            throws TransactionNotFoundException {
        Transaction t = getTransaction(id);
        t.setAmount(newAmount);
        t.setGatewayUsed(newGateway);
        System.out.println("Transaction updated: " + id);
    }

    public synchronized void updateStatus(int id, String status) throws TransactionNotFoundException {
        getTransaction(id).setStatus(status);
    }

    public synchronized void deleteTransaction(int id) throws TransactionNotFoundException {
        if (transactions.remove(id) == null) throw new TransactionNotFoundException(id);
        System.out.println("Transaction deleted: " + id);
    }

    public synchronized void displayAllTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        transactions.values().forEach(System.out::println);
    }

    public synchronized void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))) {
            for (Transaction t : transactions.values()) {
                pw.println(t.getTransactionId() + "," + t.getUserName() + "," +
                        t.getAmount() + "," + t.getGatewayUsed() + "," +
                        t.getStatus() + "," + t.getTimestamp());
            }
            System.out.println("Transactions saved to " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public synchronized void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("No saved file found.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                int id = Integer.parseInt(parts[0].trim());
                Transaction t = new Transaction(id, parts[1].trim(),
                        Double.parseDouble(parts[2].trim()), parts[3].trim());
                t.setStatus(parts[4].trim());
                transactions.put(id, t);
            }
            System.out.println("Transactions loaded from " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }
}
