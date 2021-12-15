import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable{
    private ReentrantLock rl = new ReentrantLock();
    private ReentrantLock wl = new ReentrantLock();
    private DataInputStream dis;
    private DataOutputStream dos;

    public TaggedConnection(Socket s) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
    }

    public void send(int tag, byte[] data) throws IOException{
        try{
            wl.lock();
            dos.writeInt(tag);
            dos.writeInt(data.length);
            dos.write(data);
            dos.flush();
        } finally {
            wl.unlock();
        }
    }

    public void send(Frame f) throws IOException{
        try{
            wl.lock();
            dos.writeInt(f.tag);
            dos.writeInt(f.data.length);
            dos.write(f.data);
            dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    @Override
    public void close() throws Exception {
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

    public Frame receive() throws IOException{
        int tag;
        int length;
        byte[] message;
        try{
            rl.lock();
            tag = dis.readInt();
            length = dis.readInt();
            message = new byte[length];
            dis.readFully(message);
            return new Frame(tag, message);
        } finally {
            rl.unlock();
        }
    }

    public static class Frame{
        int tag;
        byte[] data;

        Frame(int tag, byte[] data){
            this.tag = tag;
            this.data = data;
        }
    }
}
