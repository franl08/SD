import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReusableBarrier {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private int N;
    private int nInvocs;
    private int round; // necessário para colocar na condição do ciclo, reiniciar apenas o nInvocs entra em deadlock

    public ReusableBarrier(int n){
        this.N = n;
        this.nInvocs = 0;
        this.round = 0;
    }

    public void await() throws InterruptedException{
        try{
            lock.lock();
            nInvocs++;
            int actualRound = this.round;
            if(nInvocs < N)
                while(actualRound == this.round) condition.await();
            else{
                condition.signalAll();
                this.nInvocs = 0;
                this.round++;
            }
        } finally {
            lock.unlock();
        }
    }
}
