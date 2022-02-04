public class Deposit implements Runnable{
    private Bank b;
    private final int nDeps = 1000;
    private final int  depValue = 1000;

    public Deposit(Bank b){
        this.b = b;
    }

    @Override
    public void run() {
        for(int i = 0; i < nDeps; i++)
            b.deposit(depValue);
    }
}
