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

/*
    1. Num sistema de invocação remota em sistemas distribuídos poderemos considerar quatro grandes componentes principais: o Cliente, o Servidor e o Middleware.
       As suas funções são as seguintes: 
            Cliente -> Responsável pela comunicação com o utilizador, deve apresentar os dados e receber os inputs do utilizador utilizando o Middleware de forma a poder contactar com o servidor.
            Servidor -> Deverá ter acesso à lógica do programa podendo tratar da parte lógica do mesmo. Recebe pedidos dos utilizadores através do middleware e deve fornecer respostas também através dele.
            Middleware -> Esconde as interações entre clientes e servidores na invocação de métodos remotos podendo encapsular os mais diversos 
            Stub -> Permite a conversão de parâmetros entre o cliente e o servidor.
iténs (comunicação entre sockets, serialização, estratégias de threading e serviços de nome).
    2. A migração de código em sistemas distribuídos pretende mitigar o problema de enchimento da rede devido a transferências de quantidades de dados excessivas, para isso permite que seja fornecido código 
ao invés de dados, desta forma, a execução do mesmo é feita apenas numa máquina e a quantidades de dados necessária para transferência na rede é muito menor. Exemplos disto é nas aplicações WEB com utilização 
de JavaScript onde o servidor fornece o código ao cliente, sendo este o responsável pela sua execução.
    3. A transparência de localização permite que seja ocultada a localização de um recurso, ou seja, a sua localização física ou na rede (endereço IP) torna-se desconhecida. 
       O método genérico para a sua obtenção é através de um serviço de nomes que pode ser obtido de diversas maneiras, sendo que, aquela que se apresenta mais vantajosa em grande parte dos casos é a utilização
de uma DHT (Tabela de hashing distribuída), este armazena o número de servidores/serviços através de uma tabela de hash e torna cada servidor responsável por um determinado espaço das chaves da tabela de hash, sendo
que estas se devem organizar através de um conjunto de ligações de forma a permitir uma fácil navegação na rede. Desta forma, evita-se a utilização de uma raíz tornando-se indicado para um espaço de nomes não administrativo
onde não existe qualquer gargalo ou ponto único de falha. Por fim, apresenta uma procura eficiente, pois cada nó do sistema apresentará um funcionamento semelhante ao da raís de uma árvore.
*/

public class Sala implements SalaDeEspera{
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Set<Integer> desistentes;
    private Map<Integer, String> emEspera;
    private int ticket;

    public Sala(){
        this.desistentes = new HashSet<>();
        this.emEspera = new HashMap<>();
        this.ticket = 1;
    }

    public int geraID(){
        int res = 1;
        if(emEspera != null)
            for(Integer i : emEspera.keySet())
                if(i >= res) res = i + 1;
        return res;

    }

    public int getID(String nome){
        if(!emEspera.isEmpty())
            for(Integer i : emEspera.keySet())
                if(emEspera.get(i).equals(nome)) return i; 
        return -1;
    }
    
    public boolean espera(String nome){
        try{
            lock.lock();
            int ID = geraID();
            this.emEspera.put(ID, nome);
            while(this.emEspera.containsKey(ID)){
                condition.await();
            }
            if(desistentes.contains(ID)){
                desistentes.remove(ID);
                return false;
            }
            else{
                ticket++;
                return true;
            }    
        } finally{
            lock.unlock();
        }
    }

    public void desiste(String nome){
        try{
            lock.lock();
            if(!this.emEspera.isEmpty()){
                int id = getID(nome);
                this.emEspera.remove(id);
                this.desistentes.add(id);
                condition.signalAll();
            }
        } finally{
            lock.unlock();
        }
    }

    public String atende(){
        try{
            lock.lock();
            if(this.emEspera.isEmpty()) return null;
            String ans;
            int i;
            for(i = ticket; ; i++){
                String name = this.emEspera.get(i);
                if(name != null){
                    ans = name;
                    break;
                }
            }
            this.emEspera.remove(i);
            condition.signalAll();
            return ans;
        } finally{
            lock.unlock();
        }
    }

    public List<String> atende(int n){
        try{
            lock.lock();
            if(this.emEspera.isEmpty()) return null;
            List<String> ans = new ArrayList<>();
            int ac = 0;
            int i;
            for(i = ticket; i < ticket + this.emEspera.size() && ac < n; i++){
                String name = this.emEspera.get(i);
                if(name != null){
                    ans.add(name);
                    ac++;
                }
            }
            for(String s : ans){
                this.emEspera.remove(getID(s));
                condition.signalAll();
            }
            return ans;    
        } finally{
            lock.unlock();
        }
    }
}

public class Server{
    public static void main(String[] args){
        Sala sala = new Sala();
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
                        switch(message){
                            case "espera":{
                                String nome = in.readUTF();
                                out.writeUTF("ESPERE PELA SUA VEZ");
                                out.flush();
                                if (sala.espera(nome)) 
                                    out.writeUTF("ATENDIDO");
                                else out.writeUTF("NÃO ATENDIDO");
                                out.flush();
                                break;
                            }
                            case "desiste":{
                                String nome = in.readUTF();
                                sala.desiste(nome);
                                out.writeUTF("DESISTIU");
                                out.flush();
                                break;
                            }
                            case "atende":{
                                String cliente = sala.atende();
                                if(cliente != null) out.writeUTF("A ATENDER " + cliente);
                                else out.writeUTF("SEM CLIENTES PARA ATENDER");
                                out.flush();
                                break;
                            }
                            case "atenden":{
                                int N = in.readInt();
                                out.writeUTF("A ATENDER VARIOS");
                                out.flush();
                                List<String> clientes = sala.atende(N);
                                if(clientes != null){
                                    out.writeInt(clientes.size());
                                    for(String str : clientes) out.writeUTF(str);    
                                }
                                else out.writeInt(-1);
                                out.flush();
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