import java.util.concurrent.locks.Condition;

/*-> Corridas
    Principal problema da computação concorrente. O programa depende da velocidade relativa dos threads.
    Levam a que o programa esteja errado, mesmo que o programa presente em cada thread esteja correto.

-> Exclusão mútua
    Temos que tornar certos passos atómicos [Acesso Exclusivo -> thread exclui-se mútuamente dos outros]. As secções que necessitam disto são ditas de secções críticas.
    Em JAVA:
        Locks -> analogia a um trinco, só permite que 1 thread entre de cada vez na secção crítica
*/
public interface Lock {
    public void lock(); // fecha o trinco -> se estiver fechado e 1 thread tentar entrar, terá de esperar que o trinco seja unlocked
    public void unlock(); // abre o trinco fechado
}
/*
    Deve ser colocado try | finally para garantir que é sempre feito unlock por cada lock que é utilizado à entrada (efetua unlock independentemente de exceções/returns)
    try{
        lock.lock();
        // código da secção critica
    }
    finally{
        lock.unlock();
    }

    PROBLEMA:
        Alice e Bob partilham o lago. A Alice e o Bob têm, cada um, um animal de estimação que pretendem passear no lago, mas estes não podem partilhar o lago ao mesmo tempo.
    PROPRIEDADES:
        Propriedade de Proteção: Algo mau nunca deve acontecer.
        Propriedade de Animação: Indica que o sistema está correto quando algo de bom acontecer.
    Devemos garantir ambas, pois garantir apenas 1 é muito fácil:
        Proteção: Exclusão Mútua (proteção da região crítica / os animais nunca estão ao mesmo tempo no lago)
        Animação: Não há deadlocks (se 1 quiser usar -> ele pode usar | se ambos quiserem usar -> 1 pode usar)
    SOLUÇÕES (será?)

        PROTOCOLO DAS BANDEIRAS
            A Alice tem uma bandeira e o Bob outra. Quando uma bandeira está erguida o outro participante consegue observar.
            Quando a Alice quer colocar o seu animal: (do Bob é simétrico)
                -> Ergue a bandeira
                -> Verifica que o Bob não tem a bandeira erguida -> coloca o seu animal no lago, caso contrário, não coloca
            Mas, ambos podem erguer a bandeira e nenhum deles colocar o animal (porque levantam ao mesmo tempo) -> DEADLOCK
            Alteremos o protocolo do Bob: (não pode ser dos 2 pq o deadlock mantinha-se, assim o Bob cede sempre a vez)
                -> Ergue a bandeira
                -> Enquanto a bandeira da Alice estiver erguida:
                    - Baixa a bandeira
                    - Espera que a Alice baixe a bandeira
                    - Ergue a bandeira
                -> Solta o seu animal
                -> Baixa a bandeira quando o animal regressa
            (Não viola o princípio da exclusão mútua, nem origina deadlocks)
    
    Em código:
        Com 2 threads: [Algoritmo de Peterson -> o 2º thread a chegar é sempre aquele que desiste quando há uma colisão]
            CONVENÇÕES:
                Alice e Bob serão substituídos por 0 e 1, sendo que podemos saber o Thread que está a executar com ThreadID.get(), seja i o thread a executar e j o outro
*/
        class LockOne implements Lock{
            private boolean[] flag = new boolean[2]; // as posições do array refletem o 0 e 1 desejados

            public void lock(){
                flag[i] = true; // thread que pretende usar a secção crítica ergue a bandeira
                victim = i; // indica-se a si próprio como sendo a vítima
                while(flag[j] && victim == i){} // espera enquanto que o outro esteja interessado e seja a vítima (o 1º a chegar nunca precisa de esperar pelo 2º)
            }

            public void unlock(){
                flag[i] = false; // baixa a bandeira correspondente
            }
        }
/*      Como é o segundo thread a chegar que se coloca como vitima evita a Starvation, visto que o outro haverá de se colocar como sendo ele a vítima,
ao contrário do que acontecia com o Bob e a Alice, onde o Bob poderia ficar eternamente à espera
        Mas para mais de 2 threads?
            -> Se tivermos N threads, tendo por base o algoritmo de Petersen, precisamos de para N - 1 threads, assim apenas passará um de cada vez, sendo todos os outros parados (Algoritmos de filtros)
                - Necessárias N - 1 camadas e em cada camada N bandeiras [N - 1][N] (podemos substituir por um vetor que apenas indique o número de linhas ocupadas, guarda o máximo nível em vez das bandeiras)
                - Array de vítimas com N - 1 posições
                Em código:
*/
                class Filter implements Lock{
                    int[] level;  // nível máximo em que cada thread conseguiu erguer uma bandeira (p.e. thread 2 até nível 4 [-,4,-,-,-,-,-,-])
                    int[] victim; // vítima escolhida para cada um dos níveis (p.e. thread 2 até nível 4, logo parado no nível 4 [-,-,-,4,-,-,-])

                    public Filter(int n){
                        level = new int[n];
                        victim = new int[n];

                        for(int i = 1; i < n; i++) level[i] = 0; // inicializa-se todos os threads a 0, ao começar não existe qualquer bandeira erguida
                    }

                    public void lock(){
                        for(int L = 1; L < n; L++){
                            level[i] = L; // ergue a bandeira para nível L
                            victim[L] = i; // atribui-se a si próprio como vítima
                            while((level[k] >= L && k != i) && victim[L] == i); // vai esperar enquanto existir qualquer outro thread tenha a bandeira erguida no mesmo nível e ele seja a vítima
                        }
                    }

                    public void unlock(){
                        level[i] = 0; // baixa as bandeiras de todos os níveis
                    }
                }
/*              RESUMO: Começa no nível L = 0 e, no máximo n - L threads entram no nível L, existe exclusão mútua quando L = n - 1 (último nível)
                Herda todas as propriedades do Algoritmo de Petersen, mas não garante justiça, porque os threads podem-se ultrapassar
                Como é que podemos garantir essa justiça?
            -> Algoritmo da Padaria (nome devido às senhas que existem nestas lojas) (utiliza FIFO)
                - Retirar um número
                - Esperar enquanto os números mais baixos ainda não tiverem sido servidos
                - Como os contadores não garantem exclusão mútua, é necessário tratar do caso em que dois itens tenham exatamente o mesmo "ticket"
                    * Ordem lexicográfica (ticket tirado é constituído por um par (a, i) -> (número retirado [pelo menos tão grande como os que já sairam até aí], identificador do thread))
                        (a, i) > (b, j) => Se a > b || a == b && i > j
                Em pseudocódigo:
*/
                class Bakery implements Lock{
                    boolean flag[];
                    Label[] label;

                    public Bakery(int n){
                        flag = new boolean[n];
                        label = new Label[n];
                        for(int i = 0; i < n; i++){
                            flag[i] = false; // nenhuma bandeira erguida
                            label[i] = 0; // nenhum cliente servido
                        }
                    }

                    public void lock(){
                        flag[i] = true; // indica que está interessado
                        label[i] = max(label[0], ......., label[n - 1]) + 1; // verifica os números que os outros clientes têm e  atribui a si mesmo o máximo que encontre + 1 (ordem arbitrária)
                        while(existe k tal que: flag[k] && (label[i], i) > (label[k], k)); // espera no caso em que entre aqueles que tèm a bandeira erguida exista alguma etiqueta que é menor do que a que possuímos
                    }

                    public void unlock(){
                        flag[i] = false; // thread já não está interessado
                    }

                    // NOTA: o valor das etiquetas continua sempre a aumentar, visto que nunca é diminuido em qualquer etapa
                    // Há SEMPRE um thread que tem a etiqueta mais antiga, devido à ordem lexicográfica
                }
/* Estes algoritmos, no entanto, em situações reais podem falhar...
   Por exemplo, devido à maneira que o funcionamento real de um computador ocorre (escritas são, na verdade, escritas em diversos locais que pode provocar alterações da ordem considerada correta)
   Qual seria a solução? Obrigar que aquilo que se encontra na cache da CPU seja escrito para memória utilizando as chamadas Barreiras de Momória (em Java utilizando volatile ou variáveis de tipo atómico,
por exemplo, AtomicInteger) -> nestes casos a leitura espera até que todas as escritas que precedem o valor que iremos observar nessa variável sejam também observáveis no CPU em questõa (não implica que o 
conteúdo em cache seja esvaziado, mas sim que iremos observar os valores pela forma correta)
    Mas precisamos/devemos usar volatile? Não, pelo menos em SD -> têm um impacto na performance porque um acesso a uma variável deste tipo demora quase o mesmo tempo que uma com locks, logo, mais vale usar locks
    As implementações de Locks utilizadas não são baseadas com os algoritmos anteriormente vistos (precisam de muito espaço e consomem CPU)
    A utilização de ReentrantLock beneficia do escalonamento feito pelo SO, visto que coloca-os em suspensão quando acredita que a espera será de um longo período

    PROGRAMAÇÃO COM LOCKS
        Para tornar os programas que usam muitos threads eficientes é necessários:
            -> Fazer com que a secção crítica seja pequena;
            -> Fazer com que o tempo dispensado em cada secção crítica seja pequeno;
        
        A utilização de múltiplos locks sem o cuidado devido poderá originar deadlocks -> se ambos os objetos tentarem adquirir os locks ao mesmo tempo (cada um terá adquirido um lock distinto e irá tentar
adquirir o outro, esperando infinitamente).
        Solução: Os locks têm de ser adquiridos por uma ordem definida, assim evita-se que cada um adquira um lock distinto, provocando a espera logo no primeiro. A libertação dos locks não necessita de seguir
a mesma ordem.
        O primeiro a adquirir não terá qualquer vantagem, apenas será o primeiro lock a decidir o "resultado", visto que as hipóteses de cada um deles entrar na secção crítica será mesma (aproximadamente FIFO),
apenas seguirá a ordem definida.

        Locks de granularidade mais fina, em geral, aumentam a eficiência do programa, visto que diminuem a probabilidade de 2 threads se encontrarem ao mesmo tempo na mesma secção crítica.
        Mas e se precisarmos de ler alguns dados antes de adquirir o lock seguinte?

        Podemos antecipar a libertação de um lock e retardar a aquisição do outro no caso dos múltiplos locks, bastando para isso libertar o 1º lock após adquirir o 2º lock (Two Phase Locking):
            -> REGRA 1: Todos os lock() precedem unlock() [Primeiro surgem todos os lock(), só depois podem surgir unlock() -> senão um observador colocado a meio poderá detetar pontos incorretos]
            -> REGRA 2: Cada item é acedido para ser lido/escrito dentro do seu lock 
        Temos então duas fases:
            -> Fase 1: Grow (aquisição de locks)
            -> Fase 2: Shrink (libertação de locks)

        Então, as estratégias para reduzir o impacto das secções críticas:
            -> Objetos imutáveis (em JAVA têm as variáveis de instância como final)
            -> Locks de granularidade fina
            -> Two Phase Locking

        Readers-Writers Locks -> Alternativo menos restrita aos locks normais
            -> O mesmo sítio a ser acedido por mais que um leitor não é um problema, pelo que é permitido
            -> Um escritor deverá excluir a sua zona de outras entradas (quer sejam escritores ou leitores)
        Diferentes métodos para leitores e escritores:
*/
        interface ReadWriteLock{
            Lock readLock();
            Lock writeLock();
        }
/*      Temos então alguns "problemas de justiça":
            -> Preferência a leitores?
                * O escritor poderá entrar em starvation
            -> Preferência a escritores?
                * Diminuição da concorrência entre leitores
            -> Leitores e escritores em FIFO
                * Permite que leitores ultrapassem até k escritores na fila
                * Justo e eficiente (visto que é minimamente benéfico para ambas as partes)
        Isto reduz o tempo de espera, mas não reduz a quantidade de locks necessários.

        Um objeto Lock utiliza memória mesmo quando não está em uso, para contrariar isto podemos utilizar um gestor de locks.
        Estes geralmente são combinados com a ideia dos ReaderWriter Locks, proporcionando um modo ao Lock (shared para o caso de leitor, exclusive para o caso de escritor):
*/
        enum Mode{SHARED, EXCLUSIVE};
        interface LockManager{
            void lock(Object name, Mode mode); // procura por "name" no map (se não existir, cria-o e adiciona-o ao map) e locka-o
            void unlock(Object name); // procura por "name" no map e unlocka-o -> se não estiver mais ninguém a utilizar remove-o do map
        }
/*      VARIÁVEIS DE CONDIÇÃO
            Problema geral: Pretende-se esperar até que ocorra um evento noutro thread
            Precisa-se de uma operação atómica que permita efetuar unlock() + suspendMe() tudo ao mesmo tempo (se não for atómica, irá entrar em deadlock, visto que o lock será, em principio, adquirido
por outro thread, pelo que, quando o lock for novamente libertado, ele irá execeutar o suspendMe())
            Em JAVA temos:
*/
        // Atomicamente suspende o thread e liberta o lock
        Lock l = new ReentrantLock();
        Condition c = l.newCondition();
        c.await(); // atomicamente dará unlock l + suspende-se, além disso, readquire o lock quando acorda [devem ser colocados dentro de um while que assim quando acordam testam novamente a condição para ver se podem prosseguir]

        // Acordar os threads suspensos:
        c.signalAll(); // acorda todos os threads suspensos (1 por 1, pois o lock só permite que esteja 1 a executar)
        c.signal(); // acorda um thread suspenso
/*      Nota: o await poderá acordar de forma espontânea (+1 ponto que prova que é NECESSÁRIO estar colocado dentro de um while)
        Código geral:
*/
        void waitForEvent(){
            l.lock();
            //...
            while(!happened)
                c.await();
            //...
            l.unlock();
        }

        void event(){
            l.lock();
            // alterações ao estado
            c.signal(); // ou c.signalAll();
            l.unlock();
        }

/*      Até que ponto o await() apresenta uma espera junta?
            O ReentrantLock apresenta 2 modos de selecionar o próximo thread a ser executado:
                1. Default: qualquer thread que se encontre à espera
                2. Mais justo (deve-se passar True na construção do Lock): Thread à espera há mais tempo (não é garantido o funcionamento total FIFO)
            O Condition.signal() acorda o que está há mais tempo à espera, mas ele precisa de readquirir o lock [tem de competir com os outros que tentem adquirir o lock ao mesmo tempo]

        Para garantir a espera justa é preciso tornar a ordem explícita (através de um algoritmo semelhante ao de Bakery -> com "tickets" e efetuando sempre signalAll()) -> isto provoca uma quebra de eficiência
*/
