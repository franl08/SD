import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DefaultServer {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);

        while(true){
            Socket socket = ss.accept();
            Thread t = new Thread(new ThreadServer(socket));
            t.start();
        }
    }
}
