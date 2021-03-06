import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Arrays.asList;

class ContactManager {
    private HashMap<String, Contact> contacts;
    private final ReentrantLock rl = new ReentrantLock();

    public ContactManager() {
        contacts = new HashMap<>();
        // example pre-population
        update(new Contact("John", 20, 253123321, null, asList("john@mail.com")));
        update(new Contact("Alice", 30, 253987654, "CompanyInc.", asList("alice.personal@mail.com", "alice.business@mail.com")));
        update(new Contact("Bob", 40, 253123456, "Comp.Ld", asList("bob@mail.com", "bob.work@mail.com")));
    }

    public void update(Contact c) {
        try{
            rl.lock();
            this.contacts.put(c.name(), c);
        } finally {
            rl.unlock();
        }
    }

    public ContactList getContacts() {
        ContactList cl = new ContactList();
        try{
            rl.lock();
            if(!contacts.isEmpty())
                cl.addAll(contacts.values());
            return cl;
        } finally {
            rl.unlock();
        }
    }
}

class ServerWorker implements Runnable {
    private Socket socket;
    private ContactManager manager;

    public ServerWorker (Socket socket, ContactManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    // @TODO
    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            while(true){
                manager.getContacts().serialize(dos);
                dos.flush();
                Contact c = Contact.deserialize(dis);
                manager.update(c);
            }
        } catch (Exception ignored){} // quando acontece algum erro ou recebe EOF termina a conexão
    }
}



public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ContactManager manager = new ContactManager();

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, manager));
            worker.start();
        }
    }

}