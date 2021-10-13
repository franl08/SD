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
    private Lock rl;
    Account(int balance) {
      this.balance = balance;
      this.rl = new ReentrantLock();
    }
    int balance() {
      this.rl.lock();
      try{
        return balance;
      } finally {
        this.rl.unlock();
      }
    }
    boolean deposit(int value) {
      this.rl.lock();
      try {
        balance += value;
        return true;
      } finally {
        this.rl.unlock();
      }
    }
    boolean withdraw(int value) {
      if (value > balance)
        return false;
      this.rl.lock();
      try {
        balance -= value;
        return true;
      } finally {
        this.rl.unlock();
      }
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
    //this.rl.lock();
    //try{
      return av[id].balance();
    //} finally {
      //this.rl.unlock();
    //}
  }

  // Deposit
  boolean deposit(int id, int value) {
    if (id < 0 || id >= slots)
      return false;
    //this.rl.lock();
    //try {
      return av[id].deposit(value);
    //} finally {
      //this.rl.unlock();
    //}
  }

  // Withdraw; fails if no such account or insufficient balance
  public boolean withdraw(int id, int value) {
    if (id < 0 || id >= slots)
      return false;
    //this.rl.lock();
    //try{
      return av[id].withdraw(value);
    //} finally {
      //this.rl.unlock();
    //}
  }

  public boolean transfer(int from, int to, int value){
    if(from < 0 || from >= slots || to < 0 || to >= slots)
      return false;
    //this.rl.lock();
    //try{
      return withdraw(from, value) && deposit(to, value);
    //} finally {
      //this.rl.unlock();
    //}
  }

  public int totalBalance(){
    int total = 0;
    //this.rl.lock();
    //try{
      for(int i = 0; i < this.slots; i++) total += av[i].balance();
      return total;
    //} finally {
      //this.rl.unlock();
    //}
  }
}

// Cenas em comentário são as do exercício 2