class SayHello implements Runnable{
    @Override
    public void run(){
     System.out.println("Say hello from a thread.");
  }
}

public class Example{
  public static void main(String[] args) throws InterruptedException{
      Thread t = new Thread(new SayHello());
      t.start();
      Thread.sleep(1000);
  }
}
