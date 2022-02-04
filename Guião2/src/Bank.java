import java.util.concurrent.locks.ReentrantLock;

class Bank {
    private final ReentrantLock rl = new ReentrantLock();

    private static class Account {
        private int balance;
        Account(int balance) { this.balance = balance; }
        int balance() { return balance; }
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

    // Bank slots and vector of accounts
    private int slots;
    private Account[] av;

    public Bank(int n)
    {
        slots=n;
        av=new Account[slots];
        for (int i=0; i<slots; i++) av[i]=new Account(0);
    }

    // Account balance
    public int balance(int id) {
        try {
            rl.lock();
            if (id < 0 || id >= slots)
                return 0;
            return av[id].balance();
        } finally {
            rl.unlock();
        }
    }

    // Deposit
    boolean deposit(int id, int value) {
        try {
            rl.lock();
            if (id < 0 || id >= slots)
                return false;
            return av[id].deposit(value);
        } finally {
            rl.unlock();
        }
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        try {
            rl.lock();
            if (id < 0 || id >= slots)
                return false;
            return av[id].withdraw(value);
        } finally {
            rl.unlock();
        }
    }

    public boolean transfer(int from, int to, int value){
        try{
            rl.lock();
            boolean flag = withdraw(from, value);
            if(flag) flag = deposit(to, value);
            return flag;
        } finally {
            rl.unlock();
        }
    }

    public int totalBalance(){
        try{
            rl.lock();
            int total = 0;
            for(int i = 0; i < slots; i++) total += av[i].balance();
            return total;
        } finally {
            rl.unlock();
        }
    }
}