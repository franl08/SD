import java.util.concurrent.locks.ReentrantLock;

public class Bank {
    private ReentrantLock rl;

    public Bank(ReentrantLock rl){
        this.rl = rl;
    }

    private static class Account {
        private int balance;
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
        boolean deposit(int value) {
            balance += value;
            return true;
        }
    }

    // Our single account, for now
    private Account savings = new Account(0);

    // Account balance
    public int balance() {
        return savings.balance();
    }

    // Deposit
    boolean deposit(int value) {
        try {
            rl.lock();
            return savings.deposit(value);
        } finally {
            rl.unlock();
        }
    }
}