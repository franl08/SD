import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ReadPendingException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/*  GRUPO 1.
    1. Vantagens: Sendo mais legível torna-se mais fácil o seu debug, a serialização também se revela mais simples, uma alteração no conteúdo dos dados não provoca erros de deserialização.
       Desvantagens: Mais dados em rede, menos segurança nos dados em rede visto não estarem criptografados, processo mais lento e é necessário fazer parsing.
    2. A transparência de acesso permite que se oculte os recursos que o sistema pode ter acesso e as diferenças entre as representeações de dados. Assim, o utilizador nunca deverá saber
se o recurso acedido é local ou remoto.
      Através do RPC, faz-se com que o sistema não tenha que fornecer a localização dos recursos, sendo os programas em si responsáveis pelos arquivos remotos da mesma maneira que operam
sobre os arquivos locais. Desta maneira, o RPC encapsula as rotinas de acesso e consulta de dados como efetua o controlo de concorrência do sistema distribuído.
    3. A principal dificuldade que surge através da escala geográfica em aplicações cliente/servidor interativas trata-se da latência que poderá surgir entre a utilização do programa
por parte de um utilizador muito distante, algo que não é desejável.
      Uma das soluções mais utilizadas para a resolução deste problema é a replicação de dados, ou seja, a existência de diversas caches dispersas em pontos mais próximos dos recursos/utilizadores,
desta forma, visto que os dados se encontram mais perto, permite que eles não tenham de percorrer uma distância tão grande na rede, diminuindo assim os problemas relativos à latência.

    GRUPO 2.
*/
public class ControloA implements ControloAero{
    private Map<Integer, Boolean> pistas;
    private Set<Integer> avioesParaAterrar;
    private Set<Integer> avioesParaDescolar;
    private ReentrantLock lockPistas = new ReentrantLock();
    private ReentrantLock lockAvioesAterrar = new ReentrantLock();
    private ReentrantLock lockAvioesDescolar = new ReentrantLock();
    private Condition condAvioesAterrar = lockAvioesAterrar.newCondition();
    private Condition condAvioesDescolar = lockAvioesDescolar.newCondition();
    private Integer descolagensSeguidas;

    public ControloA(){
        this.pistas = new HashMap<>();
        this.avioesParaAterrar = new HashSet<>();
        this.avioesParaDescolar = new HashSet<>();
        this.descolagensSeguidas = 0;
    }


    public int getIdParaAterrar(){
        int res = 1;
        for(Integer i : avioesParaAterrar) if(i >= res) res = i + 1;
        return res;
    }

    public int getIdParaDescolar(){
        int res = 1;
        for(Integer i : avioesParaDescolar) if(i >= res) res = i + 1;
        return res;
    }

    public boolean vezDeDescolar(int id){
        if(!avioesParaDescolar.isEmpty())
            for(Integer i : avioesParaDescolar) if(i < id) return false;
        return true;
    }

    public int podeDescolar(){
        try{
            lockPistas.lock();
            if(descolagensSeguidas != 0){
                try{
                    lockAvioesAterrar.lock();
                    if(!avioesParaAterrar.isEmpty()) return -1;
                } finally{
                    lockAvioesAterrar.unlock();
                }
            }
            for(Integer i : pistas.keySet())
                if(pistas.get(i)) return i;
            return -1;
        } finally{
            lockPistas.unlock();
        }    
    }

    public int podeAterrar(){
        try{
            lockPistas.lock();
            if(descolagensSeguidas == 0){
                try{
                    lockAvioesDescolar.lock();
                    if(!avioesParaDescolar.isEmpty()) return -1;
                } finally{
                    lockAvioesDescolar.unlock();
                }
            }
            for(Integer i : pistas.keySet())
                if(pistas.get(i)) return i;
            return -1;
        } finally{
            lockPistas.unlock();
        }    
    }

    public int pedirParaDescolar(){
        try{
            lockAvioesDescolar.lock();
            int id = getIdParaDescolar();
            avioesParaDescolar.add(id);
            int pista;
            while(!vezDeDescolar(id) && (pista = podeDescolar()) == -1){
                condAvioesDescolar.await();
            }
            return pista;
        } finally{
            lockAvioesDescolar.unlock();
        }
    }

    public int pedirParaAterrar(){
        try{
            lockAvioesAterrar.lock();
            int id = getIdParaAterrar();
            avioesParaAterrar.add(id);
            int pista;
            while(vezDeDescolar(id) && (pista = podeAterrar()) == -1){
                condAvioesAterrar.await();
            }
            return pista;
        } finally{
            lockAvioesAterrar.unlock();
        }
    }

    public void descolou(int pista){
        lockPistas.lock();
        this.pistas.put(pista, true);
        lockPistas.unlock();
        condAvioesAterrar.signalAll();
        condAvioesDescolar.signalAll();
    }

    public void aterrou(int pista){
        lockPistas.lock();
        this.pistas.put(pista, true);
        lockPistas.unlock();
        condAvioesAterrar.signalAll();
        condAvioesDescolar.signalAll();
    }
}

public class Server{
    public static void main(String[] args){
        ControloA controlo = new ControloA();
        ServerSocket ss = new ServerSocket(12345);
        DataInputStream in;
        DataOutputStream out;
        while(true){
            Socket s = ss.accept();
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            Runnable worker = () -> {
                try{
                    while(true){
                        String message = in.readUTF();
                        switch (message){
                            case "pedirDescolar": {
                                int pista = controlo.pedirParaDescolar();
                                out.writeUTF("PODE DESCOLAR NA PISTA " + pista);
                                out.flush();
                                break;
                            }
                            case "pedirAterrar":{
                                int pista = controlo.pedirParaAterrar();
                                out.writeUTF("PODE ATERRAR NA PISTA " + pista);
                                out.flush();
                                break;
                            }
                            case "descolou":{
                                int pista = in.readInt();
                                controlo.descolou(pista);
                                break;
                            }
                            case "aterrou":{
                                int pista = in.readInt();
                                controlo.aterrou(pista);
                                break;
                            }
                            default:{
                                throw new Exception();
                            }
                        }
                    }
                } catch(Exception ignored){}    
            };
            Thread t = new Thread(worker);
            t.start();
        }
    }
}