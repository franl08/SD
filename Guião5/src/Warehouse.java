import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Warehouse {
    private final ReentrantLock lock = new ReentrantLock();
    private Map<String, Product> map =  new HashMap<>();

    private class Product {
        Condition c = lock.newCondition();
        int quantity = 0;
    }

    private Product get(String item) {
        try {
            lock.lock();
            Product p = map.get(item);
            if (p != null) return p;
            p = new Product();
            map.put(item, p);
            return p;
        } finally {
            lock.unlock();
        }
    }

    public void supply(String item, int quantity) {
        try {
            lock.lock();
            Product p = get(item);
            p.quantity += quantity;
            p.c.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void consume(Set<String> items) throws InterruptedException{
        try {
            lock.lock();
            for (String s : items) {
                Product prod = get(s);
                while(prod.quantity < 1) prod.c.await();
                prod.quantity--;
            }
        } finally {
            lock.unlock();
        }
    }
}