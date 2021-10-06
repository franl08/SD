import java.util.concurrent.locks.ReentrantLock;

class Bank{
    private ReentrantLock rl;

    Bank(ReentrantLock rl){
        this.rl = rl;
    }

    private static class Account {
        private int balance;

        Account(int balance) {
            this.balance = balance;
        }

        int balance() {
            return balance;
        }

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
        try{
            this.rl.lock(); // bloqueia para a zona crítica ter apenas 1 thread
            return savings.deposit(value);
        }
        finally {
            this.rl.unlock(); // desbloqueia a zona crítica
        }
    }
}

class Deposit implements Runnable{
    private Bank bank;

    Deposit(Bank b){
        this.bank = b;
    }

    public void run(){
        final long  nDeps = 1000;
        final int valueDeps = 100;

        for (int i = 0; i < nDeps; i++)
            this.bank.deposit(valueDeps);
    }
}

public class Guiao1Ex2 {
    public static void main(String[] args) throws InterruptedException {
        final int N = 10;
        ReentrantLock rl = new ReentrantLock(); // garantir exclusão mútua
        Bank bank = new Bank(rl);

        Thread[] threads = new Thread[N];

        for(int i = 0; i < N; i++) threads[i] = new Thread(new Deposit(bank));

        for(int i = 0; i < N; i++) threads[i].start();

        for(int i = 0; i < N; i++) threads[i].join();

        System.out.println("Balance: " + bank.balance());
    }
}

