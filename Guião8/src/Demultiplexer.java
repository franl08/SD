import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable{
    private TaggedConnection t;
    private ReentrantLock lock = new ReentrantLock();
    private Map<Integer, FrameValue> frames = new HashMap<>();
    private Exception exception = null;

    public Demultiplexer(TaggedConnection t){
        this.t = t;
    }

    class FrameValue{
        int waiters;
        Queue<byte[]> queue;
        Condition c = lock.newCondition();

        public FrameValue(){
            this.waiters = 0;
            this.queue = new ArrayDeque<>();
        }
    }

    public void start(){
        new Thread(() -> {
            try{
                while(true){
                    TaggedConnection.Frame frame = t.receive();
                    try{
                        lock.lock();
                        FrameValue fv = frames.get(frame.tag);
                        if(fv == null){
                            fv = new FrameValue();
                            frames.put(frame.tag, fv);
                        }
                        fv.queue.add(frame.data);
                        fv.c.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (Exception e){
                this.exception = e;
            }
        }).start();
    }

    public void send(int tag, byte[] data) throws IOException {
        t.send(tag, data);
    }

    public byte[] receive(int tag) throws Exception{
        FrameValue frameValue;
        try{
            lock.lock();
            frameValue = frames.get(tag);
            if(frameValue == null) {
                frameValue = new FrameValue();
                frames.put(tag, frameValue);
            }
            frameValue.waiters++;
            while(true){
                if(!frameValue.queue.isEmpty()){
                    frameValue.waiters--;
                    byte[] data = frameValue.queue.poll();
                    if(frameValue.waiters == 0 && frameValue.queue.isEmpty()) frames.remove(tag);
                    return data;
                }
                if(exception != null) throw exception;
                frameValue.c.await();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws Exception {
        this.t.close();
    }
}
