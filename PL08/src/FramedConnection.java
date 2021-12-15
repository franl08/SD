import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

// MIDDLEWARE
public class FramedConnection implements AutoCloseable{
    private final ReentrantLock rl = new ReentrantLock();
    private final ReentrantLock wl = new ReentrantLock();
    private DataInputStream dis;
    private DataOutputStream dos;

    public FramedConnection(Socket socket) throws IOException{
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void send(byte[] data) throws IOException{
        try{
            wl.lock();
            dos.writeInt(data.length);
            dos.write(data);
            dos.flush();
        } finally {
            wl.unlock();
        }
    }

    public byte[] receive() throws IOException{
        try{
            rl.lock();
            int length = dis.readInt();
            byte[] message = new byte[length];
            dis.readFully(message);
            return message;
        } finally {
            rl.unlock();
        }
    }

    public void close() throws IOException{
        rl.lock();
        wl.lock();
        try {
            this.dis.close();
            this.dos.close();
        } finally {
            rl.unlock();
            wl.unlock();
        }
    }
}
