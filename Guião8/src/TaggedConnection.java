import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private DataInputStream in;
    private DataOutputStream out;
    private ReentrantLock sendLock = new ReentrantLock();
    private ReentrantLock receiveLock = new ReentrantLock();

    public TaggedConnection(Socket s) throws IOException {
        this.in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    class Frame {
        int tag;
        byte[] data;

        public Frame(int tag, byte[] data){
            this.tag = tag;
            this.data = data;
        }
    }

    public Frame receive() throws IOException{
        try{
            this.receiveLock.lock();
            int tag = this.in.readInt();
            int size = this.in.readInt();
            byte[] data = new byte[size];
            this.in.readFully(data);
            return new Frame(tag, data);
        } finally {
            this.receiveLock.unlock();
        }
    }

    public void send(int tag, byte[] data) throws IOException{
        try{
            this.sendLock.lock();
            this.out.writeInt(tag);
            this.out.writeInt(data.length);
            this.out.write(data);
            this.out.flush();
        } finally {
            this.sendLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.sendLock.lock();
            this.receiveLock.lock();
            this.in.close();
            this.out.close();
        } finally {
            this.sendLock.unlock();
            this.receiveLock.unlock();
        }
    }
}
