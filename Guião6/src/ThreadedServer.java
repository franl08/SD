import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {

    public static void main(String[] args) throws IOException{
        try {
            ServerSocket ss = new ServerSocket(12345);

            while (true) {
                Socket socket = ss.accept();
                Runnable worker = () -> { // cria uma thread por cliente
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        int quantityOfNumbers = 0;
                        int totalSum = 0;
                        String line;
                        while ((line = in.readLine()) != null) {
                            int number = Integer.parseInt(line);
                            totalSum += number;
                            quantityOfNumbers++;
                            out.println(totalSum);
                            out.flush();
                        }

                        out.println(totalSum / quantityOfNumbers);
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