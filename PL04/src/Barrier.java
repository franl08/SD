import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private final Lock lock = new ReentrantLock();
    private int N;
    private int nInvocs;
    private int round;
    private final Condition cond = lock.newCondition();

    public Barrier (int N){
        this.N = N;
        this.nInvocs = 0;
        this.round = 0;
    }

    public void await() throws InterruptedException{
        lock.lock();
        try {
            int actualRound = this.round;
            nInvocs++;
            if (nInvocs < N)
                while (actualRound == this.round) {
                    cond.await();
                }
            else{
                cond.signalAll();
                this.round++;
                this.nInvocs = 0;
            }
        } finally {
            lock.unlock();
        }
    }
}

class Main {
    public static void main(String[] args) {
        Barrier b = new Barrier(3);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(2000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(1000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
            try {
                Thread.sleep(3000);
                System.out.println("Vou fazer await");
                b.await();
            } catch (Exception e) {
            }
            System.out.println("await retornou");
        }).start();
    }
}