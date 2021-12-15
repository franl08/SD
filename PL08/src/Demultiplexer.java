import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable{
    private TaggedConnection tC;
    private ReentrantLock rl = new ReentrantLock();
    private Map<Integer, FrameValue> frames = new HashMap<>();
    private Exception exception = null;

    private class FrameValue{
        int waiters;
        Queue<byte[]> queue;
        Condition c = rl.newCondition();
        public FrameValue(){
            this.waiters = 0;
            this.queue = new ArrayDeque<>();
        }
    }

    public Demultiplexer(TaggedConnection tC){
        this.tC = tC;
    }

    public void start() {
        new Thread(() -> {
            try{
                while(true){
                    TaggedConnection.Frame frame = tC.receive();
                    rl.lock();
                    try{
                        FrameValue fv = frames.get(frame.tag);
                        if(fv == null){
                            fv = new FrameValue();
                            frames.put(frame.tag, fv);
                        }
                        fv.queue.add(frame.data);
                        fv.c.signal();
                    } finally {
                        rl.unlock();
                    }
                }
            } catch (Exception e){
                exception = e;
            }
        }).start();
    };

    public void send(TaggedConnection.Frame frame) throws IOException {
        tC.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException{
        tC.send(tag, data);
    }

    /*
    public byte[] receive(int tag) throws Exception{
        rl.lock();
        FrameValue fv;
        try{
            fv = frames.get(tag);
            if(fv == null){
                fv = new FrameValue();
                frames.put(tag, fv);
            }
            fv.waiters++;
            while(true){
                if(!fv.queue.isEmpty()){
                    fv.waiters--;
                    byte[] data = fv.queue.poll();
                    if(fv.waiters == 0 && fv.queue.isEmpty()) frames.remove(tag);
                    return data;
                }
                if(exception != null) throw exception;
                fv.c.await();
            }
        } finally {
            rl.unlock();
        }
    }
    */

    public TaggedConnection.Frame receive(int tag) throws Exception{
        rl.lock();
        FrameValue fv;
        try{
            fv = frames.get(tag);
            if(fv == null){
                fv = new FrameValue();
                frames.put(tag, fv);
            }
            fv.waiters++;
            while(true){
                if(!fv.queue.isEmpty()){
                    fv.waiters--;
                    byte[] data = fv.queue.poll();
                    if(fv.waiters == 0 && fv.queue.isEmpty()) frames.remove(tag);
                    return new TaggedConnection.Frame(tag, data);
                }
                if(exception != null) throw exception;
                fv.c.await();
            }
        } finally {
            rl.unlock();
        }
    }

    public void close() throws Exception {
        this.tC.close();
    }
}
