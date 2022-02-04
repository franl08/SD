import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class FramedConnection implements AutoCloseable{
    private final ReentrantLock lockSend = new ReentrantLock();
    private final ReentrantLock lockReceive = new ReentrantLock();
    private final DataInputStream in;
    private final DataOutputStream out;

    public FramedConnection(Socket s) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void send(byte[] data) throws IOException{
        try{
            this.lockSend.lock();
            this.out.writeInt(data.length);
            this.out.write(data);
            this.out.flush();
        } finally {
            this.lockSend.unlock();
        }
    }

    public byte[] receive() throws IOException{
        try{
            this.lockReceive.lock();
            int size = this.in.readInt();
            byte[] data = new byte[size];
            this.in.readFully(data);
            return data;
        } finally {
            this.lockReceive.unlock();
        }
    }

    public void close() throws IOException{
        try {
            this.lockSend.lock();
            this.lockReceive.lock();
            this.in.close();
            this.out.close();
        } finally {
            this.lockSend.unlock();
            this.lockSend.unlock();
        }
    }
}
