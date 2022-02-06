import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/* GRUPO 1.
    1. A | B | C
    2. C | D
    3. A | C | D
    4. A | B
    5. O middleware de invocação remote tem como principal função esconder as intereações entre clientes e servidores na invocação de procedimentos e métodos remotos. Quando este deve suportar
clientes multi-threaded e invocações concorrentes a ele, surge o desafio da implementação relacionado à execução dos métodos corretos e do fornecimento de respostas às threads corretas. 
       Assim sendo, uma forma de implementação que permite a resolução deste problema será a utilização de um Demultiplexer, tratando-se este de uma abstração que delega o envio de respostas
a uma TaggedConnection permitindo assim que esta associe a cada mensagem uma etiqueta (poderá ser o identificador da thread cliente) e disponibiliza um método de receção de mensagens que recebe
como argumento uma tag da mensagem que se pretende receber, assim sendo, poderá bloquear a thread invocadora até obter resposta para a mensagem com a etiqueta especificada.

    GRUPO 2.
*/

public class ControlVacina implements ControloVacinas{
    private int numFrascosDisp;
    private final int NUM;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Queue<Integer> filaEspera;
    private int ticket;

    public ControlVacina(int n){
        this.numFrascosDisp = 0;
        this.NUM = n;
        this.filaEspera = new PriorityQueue<>();
        this.ticket = 1;
    }

    public boolean podeReceberVacina(int id){
        int numDosesDisp = numFrascosDisp * NUM;
        return id == ticket + (numDosesDisp - 1); 
    }

    public int getID(){
        int ans = 1;
        if(filaEspera != null)
            for(Integer i : filaEspera) if(i > ans) ans = i + 1;
        return ans;
    }

    public void pedirParaVacinar(){
        try{
            lock.lock();
            int id = getID();
            filaEspera.add(id);
            if(filaEspera.size() >= NUM) condition.signalAll();
            while(filaEspera.size() <= NUM && !podeReceberVacina(id)) condition.await();
            ticket++;
            filaEspera.poll();
        } finally{
            lock.unlock();
        }
    }

    public void fornecerFrascos(int frascos){
        try{
            lock.lock();
            this.numFrascosDisp += frascos;
            condition.signalAll();
        } finally{
            lock.unlock();
        }
    }
}

public class Server{
    public static void main(String[] args){
        ControlVacina controlo = new ControlVacina(5);
        ServerSocket ss = new ServerSocket(12345);
        DataInputStream in;
        DataOutputStream out;
        while(true){
            Socket s = ss.accept();
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            Runnable worker = () -> {
                try{
                    String query = in.readUTF();
                    switch(query){
                        case "vacinar":{
                            out.writeUTF("AGUARDE PELA SUA VEZ");
                            out.flush();
                            controlo.pedirParaVacinar();
                            out.writeUTF("VACINADO");
                            out.flush();
                            break;
                        }
                        case "fornecer":{
                            int nDoses = in.readInt();
                            controlo.fornecerFrascos(nDoses);
                            out.writeUTF("RECEBIDOS " + nDoses + " FRASCOS");
                            out.flush();
                            break;
                        }
                        default: {
                            throw new Exception();
                        }
                    }
                } catch(Exception ignored){}
            };
            Thread t = new Thread(worker);
            t.start();
        }
    }    
}
