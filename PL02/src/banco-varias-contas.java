import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bank {
 /* private Lock rl;

  public Bank(Lock rl){
    this.rl = rl;
  }
  */

  private static class Account {
    private int balance;
    private final Lock rl = new ReentrantLock();

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

  // Bank slots and vector of accounts
  private int slots;
  private Account[] av;

  public Bank(int n)
  {
    // this.rl = l;
    slots=n;
    av=new Account[slots];
    for (int i=0; i<slots; i++) av[i]=new Account(0);
  }

  // Account balance
  public int balance(int id) {
    if (id < 0 || id >= slots)
      return 0;
    av[id].rl.lock();
    try{
      return av[id].balance();
    } finally {
      av[id].rl.unlock();
    }
  }

  // Deposit
  boolean deposit(int id, int value) {
    if (id < 0 || id >= slots)
      return false;
    av[id].rl.lock();
    try {
      return av[id].deposit(value);
    } finally {
      av[id].rl.unlock();
    }
  }

  // Withdraw; fails if no such account or insufficient balance
  public boolean withdraw(int id, int value) {
    if (id < 0 || id >= slots)
      return false;
    av[id].rl.lock();
    try{
      return av[id].withdraw(value);
    } finally {
      av[id].rl.unlock();
    }
  }

  public boolean transfer(int from, int to, int value){
    if(from < 0 || from >= slots || to < 0 || to >= slots)
      return false;
    int min = Math.min(from, to);
    int max = Math.max(from, to);
    av[min].rl.lock();
    av[max].rl.lock();
    try{
      if(withdraw(from, value))
        return deposit(to, value);
      return false;
    } finally {
      av[max].rl.unlock();
      av[min].rl.unlock();
    }
  }

  public int totalBalance(){
    int total = 0;
    for(int i = 0; i < this.slots; i++) av[i].rl.lock();
    for(int i = 0; i < this.slots; i++){
      total += av[i].balance();
    }
    for(int i = 0; i < this.slots; i++) av[i].rl.unlock();
    return total;
  }
}

// Cenas em comentário são as do exercício 2