import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse {
    private final Lock lock = new ReentrantLock();
    private Map<String, Product> map =  new HashMap<String, Product>();

    private class Product {
        private Condition nC = lock.newCondition();
        int quantity = 0;
    }

    private Product get(String item) {
        Product p = map.get(item);
        if (p != null) return p;
        p = new Product();
        map.put(item, p);
        return p;
    }

    public void supply(String item, int quantity) {
        lock.lock();
        try{
            Product p = get(item);
            p.quantity += quantity;
            p.nC.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Errado se faltar algum produto...
    public void consume(Set<String> items) throws InterruptedException{
        lock.lock();
        try{
            for (String s : items) {
                Product p = get(s);
                while (p.quantity < 1) p.nC.await();
                p.quantity--;
            }
        } finally {
            lock.unlock();
        }
    }

}
