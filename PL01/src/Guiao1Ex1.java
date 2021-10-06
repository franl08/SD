class Increment implements Runnable {
    public void run() {
        final long I = 100;

        for (long i = 0; i < I; i++)
            System.out.println(i);
    }
}

public class Guiao1Ex1 {
    public static void main(String[] args) throws InterruptedException {
        final int N = 10;
        Thread[] threads = new Thread[N];

        // Criação das Threads
        for(int i = 0; i < N; i++) threads[i] = new Thread(new Increment());

        // Início das Threads
        for(int i = 0; i < N; i++) threads[i].start();

        // Espera das threads
        for(int i = 0; i < N; i++) threads[i].join();

        System.out.println("The end.");
    }
}
