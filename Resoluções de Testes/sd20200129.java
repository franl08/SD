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
    1. Vantagens: Facilidade na serialização, permite um debug mais fácil, uma pequena alteração no conteúdo dos dados não invalida a deserialização dos mesmos.
       Desvantagens: Necessário fazer parsing, possuem um maior tamanho portanto ocupam maior espaço na rede, devido à falta de encriptação de dados tem-se uma menor segurança de dados
e torna-se mais lenta a deserializar.
    2. A arquitetura de um sistema distribuído orientado por objetos poderá, geralmente, ser dividida em 5 grandes camadas:
        -> Cliente: Contacta com o servidor através do stub
        -> Stub: Permite o contacto com o servidor por parte do cliente convertendo os parâmetros entre eles.
        -> Servidor Entra em contacto com a camada lógica do programa através do skeleton de modo a responder aos pedidos dos clientes.
        -> Skeleton: Serve como um esqueleto que descodifica os pedidos do servidor à camada lógica redirecionando-os para os obejtos corretos
        -> Camda Lógica: Conjunto de módulos/classes referentes à camada lógica do programa que permite obter as respostas necesárias aos diversos pedidos à aplicação.
    3. Uma DHT, também conhecida como tabela de hashing distribuída, é uma das implementações que podem solucionar a necessidade de um serviço de nomes, isto é, que permite procurar o número
de servidores/serviços e obter os seus endereços de forma a serem utilizados (necessário devido à dificuldade das sequências de números aleatórios que são os endereços possam ser utilizadas
de forma direta pelos humanos). Uma DHT consiste numa tabela de hash distribuída onde diferentes partes do espaço de keys da tabela são atribuídas a diferentes servidores, além disso, estas deverão
ainda apresentar ligações a diversos pontos da tabela (pois não é possível saber a que servidor está atribuída determina parte do espaço de keys) permitindo, desta forma, que a rede seja facilmente
navegável.
      Tendo isto em conta, é possível a resolução do problema de serviços de nomes permitindo a inexistência de um espaço administrativo, o que afasta o possível problema de um gargalo ou ponto único 
de falha no servidor administrador. Por fim, permite ainda a obtenção de uma solução eficaz com uma procura eficiente visto que cada nó terá uma funcionalidade semelhante à de uma raíz de uma árvore.
    
    GRUPO 2.
*/

public class Files implements Ficheiros{
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private List<String> modificados;
    private List<String> copyModificados;
    private Set<String> aUsar;
    private Map<Integer, String> filaEspera;
    private boolean doingBackup;

    public Files(){
        this.modificados = new ArrayList<>();
        this.aUsar = new HashSet<>();
        this.filaEspera = new HashMap<>();
        this.doingBackup = false;
    }

    public int getId(){
        int res = 1;
        for (Integer i : filaEspera.keySet()) if (i >= res) res = i + 1;
        return res;
    }

    public boolean podeUsar(int id, String path){
        if(aUsar.contains(path) || (doingBackup && copyModificados.contains(path))) return false;
        else{
            if(!filaEspera.isEmpty())
                for(Integer i : filaEspera.keySet())
                    if(i < id && filaEspera.get(i).equals(path)) return false;
        }        
        return true;    
    }

    public void using(String path){
        try{
            lock.lock();
            int id = getId();
            filaEspera.put(id, path);
            while(!podeUsar(id, path)){
                condition.await();
            }
            filaEspera.remove(id);
            aUsar.add(path);
        } finally{
            lock.unlock();
        }
    }

    public void notUsing(String path, boolean modified){
        try{
            lock.lock();
            if(modified) modificados.add(path);
            aUsar.remove(path);

        } finally{
            lock.unlock();
        }
    }

    public List<String> startBackup(){
        try{
            lock.lock();
            doingBackup = true;
            copyModificados = new ArrayList<>(modificados);
            return modificados;
        } finally{
            lock.unlock();
        }
    }

    public void endBackup(){
        try{
            lock.lock();
            doingBackup = false;
            for(String s : copyModificados) if(modificados.contains(s)) modificados.remove(s);
            condition.signalAll();
        } finally{
            lock.unlock();
        }
    }
}

public class Server{
    public static void main(String[] args){
        Files ficheiros = new Files();
        ServerSocket ss = new ServerSocket(12345);
        DataInputStream in;
        DataOutputStream out;
        while(true){
            Socket s = ss.accept();
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            Runnable worker = () -> {
                try{
                    String message = in.readUTF();
                    switch(message){
                        case "using":{
                            String path = in.readUTF();
                            ficheiros.using(path);
                            break;
                        }
                        case "notusing":{
                            String path = in.readUTF();
                            String bool = in.readUTF();
                            if(bool.equals("true")) ficheiros.notUsing(path, true);
                            else ficheiros.notUsing(path, false);
                            break;
                        }
                        case "backup":{
                            List<String> files = ficheiros.startBackup();
                            if(files == null || files.isEmpty()) out.writeInt(-1);
                            else{
                                out.writeInt(files.size());
                                for(String path : files) out.writeUTF(path);
                            }
                            out.flush();
                            break;
                        }
                        case "endbackup":{
                            ficheiros.endBackup();
                            break;
                        }
                    }
                } catch(Exception ignored){}
            };
            Thread t = new Thread(worker);
            t.start();
        }
    }
}