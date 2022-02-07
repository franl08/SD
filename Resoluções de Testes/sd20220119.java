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

/* GRUPO 1
    1. B | C | D
    2. A | D
    3. A 
    4. A | C 
    5. Tendo em conta que o sistema em consideração trata-se de um sistema de criptomoedas em que os diversos pontos nele presentes deverão conter uma cópia do registo de todas as transações
efetuadas e do conteúdo das carteiras do diversos clientes, apesar de surgirem outros problemas, nomeadamente a nível da exclusão mútua e da confirmação transacional distribuída, considero
que o problema distribuído fulcral deste sistema seria a difusão fiável de mensagens, até porque, um correto funcionamento deste problema, visto o sistema presente, permitiria rápidas
resoluções aos outros problemas que pudessem surgir.
        Uma das soluções mais utilizadas para este problema é denomeada por Gossip e tem um funcionamento semelhante ao de um rumor ou de um epidemia. Assim, um ponto estará responsável por
difundir a sua mensagem a um determinado grupo de outros pontos do sistema escolhidos de forma aleatória e assim sucessivamente, permitindo uma propagação rápida da mensagem e fornecendo
elevadas certezas que os diversos pontos presentes no sistema recebem a mensagem sem ser necessário a utilização de confirmações explícitas. Além disso, considero correto realçar que uma
implementação prática deste método faz com que cada ponto do sistema, ao invés de emitir toda a mensagem, emita apenas um anúncio que a mensagem está disponível, permitindo, desta forma
uma menor repetição da receção de uma grande quantidade de dados por pontos que já tenham recebido a mensagem atualizada.
        A solução, a meu ver, apresenta-se implementável no sistema em questão e poderá, conforme dito anteriormente, ajudar a combater outros problemas distribuídos verificando a autenticidade
e possibilidade da execução de determinadas ações entre os demais participantes da mesma. Além disso, tendo em conta que este sistema deverá, em princípio, conter diversos utilizadores espalhados
em diversos pontos do globo, poderá espalhar as mensagens de forma rápida (analogamente a uma epidemia, consoante existirem mais pontos para propagar a epidemia, a epidemia propagar-se-á
de forma mais rápida).

GRUPO 2.
*/

public class Votos implements Votacao{
    private Set<Integer> jaVerificado = new HashSet<>();
    private ReentrantLock lockVerificados = new ReentrantLock();
    private Map<Integer, Integer> votos = new HashMap<>();
    private ReentrantLock lockVotos = new ReentrantLock();
    private Map<Integer, Boolean> cabineEstaOcupada = new HashMap<>();
    private ReentrantLock lockCabine = new ReentrantLock();
    private Condition condVotos = lockVotos.newCondition();
    private Condition condCabines = lockCabine.newCondition();
    private boolean votacaoFechada = false;    

    public boolean verifica(int identidade){
        try{
            lockVerificados.lock();
            boolean verifica = false;
            lockCabine.lock();
            if(!votacaoFechada){
                verifica = !jaVerificado.contains(identidade);
                if(verifica) this.jaVerificado.add(identidade);
            }  
            return verifica;
        } finally{
            lockCabine.unlock();
            lockVerificados.unlock();
        }
    }

    public int getCabineLivre(){
        for(Integer nCabin : cabineEstaOcupada.keySet()) if (!cabineEstaOcupada.get(nCabin)) return nCabin;
        return -1; 
    }

    public boolean todasLivres(){
        int nLivres = 0;
        for(Integer nCabin : cabineEstaOcupada.keySet()) if (!cabineEstaOcupada.get(nCabin)) nLivres++;
        return (nLivres == cabineEstaOcupada.size()); 
    }

    public void ocupaCabine(int nCabin){
        this.cabineEstaOcupada.put(nCabin, true);
    }

    public int esperaPorCabine() throws InterruptedException{
        try{
            lockCabine.lock();
            int res;
            while ((res = getCabineLivre()) == -1) condCabines.await();
            ocupaCabine(res);
            return res;
            }
        finally {
            lockCabine.unlock();
        }
    }

    public void vota(int escolha){
        try{
            lockVotos.lock();
            int nVotos = votos.get(escolha) + 1;
            this.votos.put(escolha, nVotos);
        } finally{
            lockVotos.unlock();
        }
    }

    public void desocupaCabine(int i){
        try{
            lockCabine.lock();
            this.cabineEstaOcupada.put(i, false);
            condCabines.signalAll();
        } finally{
            lockCabine.unlock();
        }
    }

    public int getMaisVotacoes(){
        int res = -1;
        int maxVot = -1;
        for(Integer esc : this.votos.keySet()){
            if(this.votos.get(esc) > maxVot) maxVot = this.votos.get(esc);
            res = esc;
        }
        return res;
    }

    public int vencedor() throws InterruptedException{
        try{
            lockCabine.lock();
            this.votacaoFechada = true;
            while(!todasLivres()) condCabines.await();
            return getMaisVotacoes();
        } finally{
            lockCabine.unlock();
        }
    }
}

public class Server{

    public static void main(String[] args){
        Votos votacao = new Votos();
        ServerSocket ss = new ServerSocket(12345);
        DataInputStream in;
        DataOutputStream out;
        
        while(true){
            Socket s = ss.accept();
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            Runnable worker = () -> {
                try{
                    int identidade = in.readInt();
                    if(votacao.verifica(identidade)){
                        int nCabin = votacao.esperaPorCabine();
                        out.writeUTF("VOTE NA CABINE " + nCabin);
                        out.flush();
                        int voto = in.readInt();
                        votacao.vota(voto);
                        out.writeUTF("VOTO CONFIRMADO");
                        out.flush();
                        votacao.desocupaCabine(nCabin);
                    }
                    else{
                        out.writeUTF("INVALIDO");
                        out.flush();
                    }
                } catch(Exception ignored){}
            };
            Thread t = new Thread(worker);
            t.start();
        }
    }
}