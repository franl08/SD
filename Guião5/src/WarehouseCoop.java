import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class WarehouseCoop {
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
            int i = 0;
            String[] prods = (String[]) items.toArray();
            while(i < prods.length) { // assinala que pretende o produto e verifica se ele existe
                Product prod = get(prods[i]);
                if(prod.quantity < 1) {
                    while (prod.quantity < 1) prod.c.await(); // se não existir/não tiver em stock tem de esperar até ter e ser acordado
                    i = 0; // necessário reiniciar o loop porque vai querer verificar se os diversos itens continuam com stock
                }
            }

            for(String s : items) map.get(s).quantity--; // efetua o consumo dos diversos produtos desejados

        } finally {
            lock.unlock();
        }
    }
}