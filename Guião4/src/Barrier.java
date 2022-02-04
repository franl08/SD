import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barrier {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private int N;
    private int nInvocs;

    public Barrier(int n){
        this.N = n;
        this.nInvocs = 0;
    }

    public void await() throws InterruptedException{
        try{
            lock.lock();
            nInvocs++;
            if(nInvocs < N)
                while(nInvocs < N) condition.await();
            else condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
