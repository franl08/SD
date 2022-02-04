import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Bank {
    private ReentrantLock rl = new ReentrantLock();

    private static class Account {
        private ReentrantLock lock = new ReentrantLock();
        private int balance;
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

    private Map<Integer, Account> map = new HashMap<>();
    private int nextId = 0;

    // create account and return account id
    public int createAccount(int balance) {
        try {
            rl.lock();
            Account c = new Account(balance);
            int id = nextId;
            nextId += 1;
            map.put(id, c);
            return id;
        } finally {
            rl.unlock();
        }
    }

    // close account and return balance, or 0 if no such account
    public int closeAccount(int id) {
        Account c;
        try {
            rl.lock();
            c = map.remove(id);
            if (c == null)
                return 0;
            c.lock.lock();
        } finally {
            rl.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lock.unlock();
        }
    }

    // account balance; 0 if no such account
    public int balance(int id) {
        Account c;
        try {
            rl.lock();
            c = map.get(id);
            if (c == null)
                return 0;
            c.lock.lock();
        } finally {
            rl.unlock();
        }
        try {
            return c.balance();
        } finally {
            c.lock.unlock();
        }
    }

    // deposit; fails if no such account
    public boolean deposit(int id, int value) {
        Account c;
        try {
            rl.lock();
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        } finally {
            rl.lock();
        }
        try {
            return c.deposit(value);
        } finally {
            c.lock.unlock();
        }
    }

    // withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        Account c;
        try {
            rl.lock();
            c = map.get(id);
            if (c == null)
                return false;
            c.lock.lock();
        } finally {
            rl.unlock();
        }
        try {
            return c.withdraw(value);
        } finally {
            c.lock.unlock();
        }
    }

    // transfer value between accounts;
    // fails if either account does not exist or insufficient balance
    public boolean transfer(int from, int to, int value) {
        Account cfrom, cto;
        try {
            rl.lock();
            cfrom = map.get(from);
            cto = map.get(to);
            if (cfrom == null || cto == null)
                return false;
            if(from >= to){
                cfrom.lock.lock();
                cto.lock.lock();
            }
            else{
                cto.lock.lock();
                cfrom.lock.lock();
            }
        } finally {
            rl.unlock();
        }
        try {
            return cfrom.withdraw(value) && cto.deposit(value);
        } finally {
            cfrom.lock.unlock();
            cto.lock.unlock();
        }
    }

    // sum of balances in set of accounts; 0 if some does not exist
    public int totalBalance(int[] ids) {
        List<Account> acs = new ArrayList<>();
        try{
            rl.lock();
            for(int i : Arrays.stream(ids).toArray()){
                Account c = map.get(i);
                if(c == null) return 0;
                acs.add(c);
            }
            for(Account c : acs) c.lock.lock();
        } finally {
            rl.unlock();
        }
        try {
            int total = 0;
            for (int i : ids) {
                Account c = map.get(i);
                if (c == null)
                    return 0;
                total += c.balance();
            }
            return total;
        }
        finally {
            for(Account c : acs) c.lock.unlock();
        }
    }
}