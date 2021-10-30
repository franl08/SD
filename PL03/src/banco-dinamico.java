import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Bank {
    private final ReadWriteLock l = new ReentrantReadWriteLock();

    private static class Account {
        private int balance;
        private final ReadWriteLock rl = new ReentrantReadWriteLock();

        Account(int balance) {
            this.balance = balance;
        }

        int balance() {
            return balance;
        }

        boolean deposit(int value) {
            balance += value;
            return true;
        }

        boolean withdraw(int value) {
            if (value > balance)
                return false;
            balance -= value;
            return true;
        }
    }

    private Map<Integer, Account> map = new HashMap<Integer, Account>();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        Account c = new Account(balance);
        this.l.writeLock().lock();
        int id = nextId;
        nextId += 1;
        map.put(id, c);
        this.l.writeLock().unlock();
        return id;
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        this.l.writeLock().lock();
        try{
            c = map.remove(id);
            if(c == null) return 0;
            c.rl.readLock().lock();
        }
        finally {
            this.l.writeLock().unlock();
        }
        try {
            return c.balance();
        }
        finally {
            c.rl.readLock().unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        this.l.readLock().lock();
        try{
            c = map.get(id);
            if(c == null) return 0;
            c.rl.readLock().lock();
        }
        finally {
            this.l.readLock().unlock();
        }
        try{
            return c.balance();
        }
        finally {
            c.rl.readLock().unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        this.l.readLock().lock();
        try{
            c = map.get(id);
            if(c == null) return false;
            c.rl.writeLock().lock();
        }
        finally {
            this.l.readLock().unlock();
        }
        try {
            return c.deposit(value);
        }
        finally {
            c.rl.writeLock().unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        this.l.readLock().lock();
        try{
            c = map.get(id);
            if (c == null) return false;
            c.rl.writeLock().lock();
        }
        finally {
            this.l.readLock().unlock();
        }
        try{
            return c.withdraw(value);
        }
        finally {
            c.rl.writeLock().unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        this.l.readLock().lock();
        try{
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto == null) return false;
            if(from < to){
                cfrom.rl.writeLock().lock();
                cto.rl.writeLock().lock();
            }
            else{
                cto.rl.writeLock().lock();
                cfrom.rl.writeLock().lock();
            }
        }
        finally {
            this.l.readLock().unlock();
        }
        try {
            try{
                if(!cfrom.withdraw(value)) return false;
            }
            finally {
                cfrom.rl.writeLock().unlock();
            }
            return cto.deposit(value);
        }
        finally {
            cto.rl.writeLock().unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        List<Account> acs = new ArrayList<>();
        this.l.readLock().lock();
        try{
            for(int i : Arrays.stream(ids).sorted().toArray()){
                Account ac = map.get(i);
                if(ac == null) return 0;
                acs.add(ac);
            }
            for(Account c : acs) c.rl.readLock().lock();
        }
        finally {
            this.l.readLock().unlock();
        }
        int total = 0;
        for(Account c : acs){
            total += c.balance();
            c.rl.readLock().unlock();
        }
        return total;
  }

}
