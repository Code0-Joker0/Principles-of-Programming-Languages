package TransactionPackage;

public class TransactionNotFoundException extends Exception {
    public TransactionNotFoundException(int id) {
        super("Transaction with ID " + id + " not found.");
    }
}
