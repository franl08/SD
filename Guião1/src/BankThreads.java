import java.util.concurrent.locks.ReentrantLock;

public class BankThreads {
    private final static int NThreads = 10;

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[NThreads];
        Bank b = new Bank(new ReentrantLock());
        for(int i = 0; i < NThreads; i++)
            threads[i] = new Thread(new Deposit(b));

        for(int i = 0; i < NThreads; i++) threads[i].start();

        for(int i = 0; i < NThreads; i++) threads[i].join();

        System.out.println("Final Balance: " + b.balance());

    }
}
