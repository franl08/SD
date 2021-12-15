import java.net.Socket;

public class ThreadedClient {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer m = new Demultiplexer(new TaggedConnection(s));
        m.start();

        Thread[] threads = {

            new Thread(() -> {
                try  {
                    // send request
                    m.send(1, "Ola".getBytes());
                    Thread.sleep(100);
                    // get reply
                    TaggedConnection.Frame frame1 = m.receive(1);
                    assert frame1.tag == 1;
                    byte[] data = frame1.data;
                    System.out.println("(1) Reply: " + new String(data));
                }  catch (Exception ignored) {}
            }),

            new Thread(() -> {
                try  {
                    // send request
                    m.send(3, "Hello".getBytes());
                    Thread.sleep(100);
                    // get reply
                    TaggedConnection.Frame frame3 = m.receive(3);
                    assert frame3.tag == 3;
                    byte[] data = frame3.data;
                    System.out.println("(2) Reply: " + new String(data));
                }  catch (Exception ignored) {}
            }),

            new Thread(() -> {
                try  {
                    // One-way
                    m.send(0, ":-p".getBytes());
                }  catch (Exception ignored) {}
            }),

            new Thread(() -> {
                try  {
                    // Get stream of messages until empty msg
                    m.send(2, "ABCDE".getBytes());
                    for (;;) {
                        TaggedConnection.Frame frame2 = m.receive(2);
                        assert frame2.tag == 2;
                        byte[] data = frame2.data;
                        if (data.length == 0)
                            break;
                        System.out.println("(4) From stream: " + new String(data));
                    }
                } catch (Exception ignored) {}
            }),

            new Thread(() -> {
                try  {
                    // Get stream of messages until empty msg
                    m.send(4, "123".getBytes());
                    for (;;) {
                        TaggedConnection.Frame frame4 = m.receive(4);
                        assert frame4.tag == 4;
                        byte[] data = frame4.data;
                        if (data.length == 0)
                            break;
                        System.out.println("(5) From stream: " + new String(data));
                    }
                } catch (Exception ignored) {}
            })

        };

        for (Thread t: threads) t.start();
        for (Thread t: threads) t.join();
        m.close();
    }
}
