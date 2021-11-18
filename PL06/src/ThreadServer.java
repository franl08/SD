import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadServer implements Runnable {
    Socket socket;
    int ac = 0;
    int readN = 0;

    ThreadServer(Socket s){
        this.socket = s;
    }

    public void run() {
        try {

            while (true) {

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                String line;
                int actualSum = 0;
                while ((line = in.readLine()) != null) {

                    try {
                        int readNumber = Integer.parseInt(line);
                        actualSum += readNumber;
                        ac += readNumber;
                        readN++;
                    }
                    catch (Exception ignored){}

                    out.println(actualSum);
                    out.flush();
                }

                out.println(ac / readN);
                out.flush();
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
