import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadedServerSharedState {
    private static int totalSumClients = 0;
    private static int quantityOfNumbersClients = 0;
    private static int average = 0;
    private static ReentrantLock rl = new ReentrantLock();

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);

            while (true) {
                Socket socket = ss.accept();
                Runnable worker = () -> { // cria uma thread por cliente
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        int totalSum = 0;
                        String line;
                        while ((line = in.readLine()) != null) {
                            int number = Integer.parseInt(line);
                            totalSum += number;
                            out.println(totalSum);
                            out.flush();
                            try{
                                rl.lock();
                                totalSumClients += number;
                                quantityOfNumbersClients++;
                            } finally {
                                rl.unlock();
                            }
                        }
                        try{
                            rl.lock();
                            average = totalSumClients / quantityOfNumbersClients;
                        } finally {
                            rl.unlock();
                        }
                        out.println(average);
                        out.flush();
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                    } catch(Exception ignored){}
                };
                Thread t = new Thread(worker);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}