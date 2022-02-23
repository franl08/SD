/* SERVIÇO DE NOMES:
    Porquê? É necessário indicar os endereços dos servidores e serviços que estão a ser usados, no entanto, visto estes serem uma sequência de números "aleatórios" torna-se incoveniente para o uso humano de forma
direta. Precisa-se então de um serviço de diretoria que permita procurar o número de servidores/serviços e encontrar os endereços de forma a serem utilizados.
    ALTERNATIVAS:
        -> Espaço de nomes plano: guarda os nomes e os serviços de diretoria num servidor local (p.e. um servidor que mantenha um map de nomes para endereços) [p.e. em Java RMI]
            * Tem o incoveniente de ser necessário saber o endereço do servidor de diretoria
            * Pode-se utilizar as primitivas de difusão na rede para contactar diretamente os servidores/serviços alvo (cada servidor estará à escuta na rede de perguntas da diretoria, quando surge uma pergunta
envia-se para toda a rede "Quem é que neste momento tem o endereço de um determinado serviço X" e o servidor que nesse momento tiver o serviço a correr poderá responder diretamente o endereço que interesse) [p.e. em ARP]
            * Tanto a nível local como em broadcast, são pouco escaláveis, visto que, na utilização de um servidor único ter-se-ia um ponto único de falha e um gargalo (indisponibilidade do servidor => completa indisponibilidade
do serviço de diretoria), no broadcast a pergunta é enviada a TODOS os servidores o que não é em nada eficiente.
            * Não permitem facilmente que autoridade administrativa seja distribuída, ou seja, que diferentes partes do espaço de nomes sejam geridas por diferentes entidades.
        -> Serviço de nomes hierárquico: 
            * Combate os problemas anteriores (deixa de existir gargalo porque diferentes servidores serão responsáveis por diferentes espaços de nomes E permite-se que a autoridade administrativa seja distribuída,
diferentes entidades podem ser responsáveis por gerir um espaço diferente do domínio de nomes)
            * Nas diversas camadas pode-se ter uma administração diferente
                - Tipicamente, ao nível da raíz mais centralizada e estruturada e conforme se vai avançando vai ficando cada vez mais fácil de modificar e gerir diretamente o espaço de nomes
            * Quando diferentes partes do espaço são geridas por diferentes servidores ainda se tem a vantagem de poder ser consultado de forma iterativa ou recursiva (servidores intermédios podem guardar a informação em cache,
pode provocar que o servidor de nomes na raíz fique bastante sobrecarregado)
            * Muito mais eficiente que o anterior
            * Continua a existir um gargalo e um ponto único de falha no nó raiz, no entanto são aliviados por poder armazenar em cache resultados de perguntas para os níveis anteriores
        -> Tabela de hashing distribuída (partes do espaço das keys são atribuídas a diferentes servidores -> necessário organizá-los através de um conjunto de ligações, pois não sabemos que servidor estará responsável
por cada parte da tabela de hash e deve ser fácil navegar na rede após questionar qualquer um dos servidores para em pouco passos chegar ao servidor desejado) [p.e. BitTorrent]
            * Grande vantagem de não ter raíz -> sem gargalo/ponto único de falha
            * Procura eficiente (cada nó do sistema funciona como a raíz de uma árvores)

    TEMPO E EXCLUSÃO MÚTUA:
        De forma a termos um sistema distribuído é necessários que os seus diversos elementos de computação se coordenem. Esta coordenação pode ser impliícita ou explícita.
        PROBLEMA: Sincronização de Relógios
            Necessário porque os relógios de sítios diferentes podem apresentar um tempo diferente. Esta diferença pode ser grave em diversos cenários, por exemplo, para o caso de ficheiros partilhados com certos algoritmos
(p.e. o Make em que é escolhido o elemento mais recente) são muito afetados pela diferença de relógio.
            -> Para sincronizar dois relógios, estes não devem ser sincronizados de forma brusca (atualizando-os por exemplo através do valor de diferença entre eles [considerando um de referência para o outro]), mas sim com 
pequenos incrementos ao longo de um determinado período de tempo (fazê-lo andar ligeiramente mais rápido/devagar do que o suposto durante um período de tempo)
                * MAS para isto era necessário obter informação instantânea acerca do outro relógio, o quem em sistemas distribuídos não é possível (é feito através de mensagens que demoram uma quantidade de tempo [incerta] na rede)
                    - A melhor opção passaria por arranjar algoritmos que consigam estimar o atraso das mensagens, ou, melhor ainda, que possam funcionar bem apesar dos atrasos das mensagens.
        
            SOLUÇÕES:
                -> Network Time Protocol (NTP) [utilizado generalizadamente nos dispositivos comuns]
                    * Quando existe troca de mensagens entre dois processos vai fazer uma estimativa do atraso da mensagem:
                        1. O processo que está a tentar sincronizar o tempo mede o tempo que demora entre o envio e a resposta e divide por 2 (parte do pressuposto que o tempo de ida e volta é semelhante)
                        2. Repete o processo várias vezes e escolhe aquela em que se demorou menos tempo entre o envia e a resposta (o que existiram menos perturbações na rede -> melhor estimativa possível)
                -> Reference Base Synchronization (RBS) [possível quando temos uma rede que permite uma verdadeira difusão de mensagens -> uma mensagem é enviada fisicamente para todos e é recebida] [menos utilzado porque estas redes são
menos comuns e poderão não conter qualquer relógio de referência]
                    1. Um participante envia uma mensagem através do canal de difusão
                    2. Todos os participantes assinalam o tempo a que receberam essa mensagem de referência
                    3. Esperam que o relógio de referência envie o valor lido a todos os outros
                    4. Calcula-se a diferença entre o valor recebido e o valor lido (estimativa bastante melhor que o NTP, pode não ser 100% certa devido ao tempo que demora a processar a mensagem)

        PROBLEMA: Exclusão Mútua
            -> Queremos garantir o mutex:
                1. Excluir mais do que um thread da secção crítica
                2. Ausência de deadlocks e starvation
            -> A eficiência depende de:
                1. Número de saltos de mensagens necessários até entrar na secção crítica (medida de latência até à entrada na secção crítica)
                2. Balanceamento de carga entre os participantes (quando se vai trocar mensagens, essas serão trocadas com um elemento em particular do grupo ou com os vários elementos do grupo)

            SOLUÇÕES:
                -> "Receita" de um mutex implementado por um único participante + "Receita" de sistemas cliente/servidor de invocação remota
                    * Incovenientes:
                        1. Para entrar na secção crítica é necessário pelo MENOS o tempo de uma ida e volta ao servidor
                        2. 1 processo (servidor) que terá de lidar com pelo MENOS 4 mensagens por cada secção crítica (invocações remotas (lock + resposta + unlock + resposta))
                -> Organização dos processos num anel e circulação de um "testemunho" que dá acesso à secção crítica
                    * A nível de starvation, se houver um processo que continuamente queira utilizar a secção crítica poderá monopolizar o testemunho, deixando os outros em starvation.
                        - Para evitar isso deve-se criar uma regra que o processo poderá utilizar o testemunho 1x para entrar na secção crítica e passá-lo logo a seguir
                    * Vantagens:
                        - Carga simétrica (todos os participantes participam com o mesmo número de mensagens enviadas e recebidas)
                    * Inconveniente:
                        - Tempo de espera para entrar na rede é em média dado pelo nº de participantes / 2.
                        - Obriga a que sejam enviadas mensagens mesmo que nenhum participante esteja a entrar na secção crítica naquele momento
                    * Não existe qualquer fila de espera explícita, sendo que a queue é implícita na forma como são efetuadas as trocas de mensagens
                -> Cada participante, quando pretende entrar na secção crítica, difunde essa intenção a todos os processos.
                    * Deve tirar partido da sincronização de relógios (para evitar que diferentes processos discordem do seu lugar na fila, visto que as partidas e chegadas de mensagens não são instantâneas)
                        - Não resolve completamente (um pedido poderá partir enquanto existe uma mensagem a caminho a informar que pretende entrar -> quando de facto chegar, a mensagem já terá partido)
                            <-> Podemos evitar isto da seguinte forma:
                                1. Assumir que existe um delta que é um limite superior ao (tempo de transmissão + eventual diferença entre os relógios)
                                2. Apenas se considera mensagens candidatas à fila de espera do mutex depois de ter passado, pelo menos, delta (ordenadas pelo seu timestamp)
        
        QUAL A IMPORTÂNCIA DO TEMPO EM SISTEMAS DISTRIBUÍDOS?
            Causalidade -> Um acontecimento precede causalmente outro nos seguintes casos:
                1. Se os dois acontecimentos são no mesmo processo e um é antes do outro.
                2. Se os dois acontecimentos são o envio e a receção da mesma mensagem (nos diferentes processos).
            -> Se um acontecimento A precede causalmente B e B precede causalmente C, então, A precede causalmente C.
            -> Quando temos 2 eventos em que nenhum precede causalmente o outro, diz-se que são eventos concorrentes.

            Seja Clock(i) o relógio no instante i.
            Se i precede causalmente j, temos que Clock(i) < Clock(j) [caso contrário estaria a ser enviada um mensagem do futuro para o passado]
            -> Quando sabemos que não existe qualquer i que Clock(i) < Clock(j), então sabemos que não existe nenhum acontecimento que preceda j
            Será que então não podemos construir um relógio lógico com esta propriedade? [podemos descartar o relógio e fazer com que o relógio lógico não dependa da sincronização]

            RELÓGIO LÓGICO DE LAMPORT
                Comportam-se de acordo com o seguinte conjunto de regras:
                    1. Cada vez que acontece alguma coisa, o processo incrementa o seu relógio ("faz tic-tac sempre que acontece alguma coisa / é um contador de acontecimentos e não uma medida de tempo real")
                    2. Sempre que um processo envia uma mensagem ele tem que colocar o seu relógio lógico nessa mensagem
                    3. Sempre que um processo recebe uma mensagem deve calcular o max(valor do relógio local , valor do relógio da mensagem) + 1 e acertar o seu relógio perante o valor obtido

        REGRESSANDO AO PROBLEMA DE EXCLUSÃO MÚTUA
            Será que podemos alterar o último algoritmo de solução utilizando o relógio lógico:
                -> Assume-se inicialmente que os processos estão continuamente a trocar mensagens
                -> Não podemos utilizar o delta visto que não temos tempo real e qualquer processo pode dar um conjunto ilimitado de passos enquanto as mensagens estão a ser transmitidas
                    * Em cada processo vamos calcular o mínimo de todos os relógios lógicos que conhecemos dos outros processos [no fundo, é o cálculo retroativo do delta]
                -> Ordena-se pelo timestamp e em caso de empate é utilizado o ID do processo para desempate
                -> 1 salto para entrar na fila
                -> Carga balanceada
            O tempo lógico pode ser aplicado em diversos problemas de sistemas distribuídos. 
            Desde que o estado interno e as operações das estruturas dependam exclusivamente da sequência das operações aplicadas, pode ser replicado o algoritmo visto anteriormente, designado por RSM (Replicated
State Machine)

    MULTICAST:
        PROBLEMA: Difusão fiável de mensagens para múltiplos destinos
            Fiável -> Todos os destinos devem receber e processar todas as mensagens
                -> Mas, todos os emissores e recetores podem falhar, ou até mesmo serem desligados da rede
                    * Então, o máximo que podemos assegurar é que todos os destinos corretos contenham exatamente o mesmo conjunto de mensagens e que essas mensagens contenham as mensagens enviadas por emissores corretos
                     - Mas como? [considerando os destinos dispersos à volta do mundo e numerosos]
            SOLUÇÕES:
                -> Técnica semelhante à aplicada na comunicação ponto-a-ponto (TCP/IP)
                    1. Cada mensagem que é enviada é colocada num buffer do emissor e é enviada para todos os recetores
                    2. Em seguida, espera-se pela receção da mensagem de confirmação (acks) dos recetores 
                    3. Após receber todas as confirmações, retira-se a mensagem do buffer
                    * No entanto, esta técnica implica, com o passar do tempo e com um maior número de processos, que o buffer apresente um tamanho muito grande, pois a receção das mensagens de confirmação será
muito demorada, logo, demorará até poder ser retirado o conteúdo do buffer
                        - Não é escalável a um grande número de destinos devido a isto (ack implosion)
                    * Além disso, temos de considerar ainda o caso do próprio emissor falhar -> temos de considerar a possibilidade dos recetores retransmitirem a mensagem entre si, assim será necessária a existência
de um buffer em cada um dos processos para poder guardar as mensagens temporariamente -> necessário que os diferentes recetores enviem acks entre si para poderem libertar o buffer -> ack implosion de O(n^2)
                -> Gossip (análogo a boatos)
                    * Cada vez que um participante pretende disseminar uma mensagem começa por selecionar um subconjunto aleatório de todos os destinos
                    * Cada recetor assumirá um papel semelhante, ou seja, escolherá também um subconjunto de destinos e repete o processo
                    * Após enviar a mensagem, pode descartá-la (não é necessária manter num buffer)
                    - Também conhecido por protocolo Epidémico
                        <-> Emissor = Paciente 0 (aquele que tem a doença em primeiro lugar) = Espalha o rumor
                        <-> Recetor = Infetado = Sabe do rumor
                        <-> Ignora duplicados = Morto/Imune = Já sabia do rumor
                    - Complexidade de log(N), N = tamanho da população
                    - Certos participantes receberão um elevado número de cópias da mesma mensagem
                        <-> Na prática, estes sistemas não enviam a mensagem toda, mas sim apenas um anúncio que a mensagem está disponível (pequena quantidade de dados) e a mensagem só é transmitida num pequeno número
de situações
                        <-> Propriedades fundamentais:
                            1. Sem haver confirmações explícitas tem-se uma elevada certeza que todos recebem a mensagem
                            2. Em média, para cada destino cada mensagem é transmitida exatamente 1 vez

        ACORDO:
            PROBLEMA: Confirmação transacional distribuída
                Considere-se uma transação - sequência de operações que deve ser executada de forma atómica (ou todas as operações são executadas, ou nenhuma delas é executada)
                    -> Tendo um coordenador e tendo diversos participantes, o coordenador deverá enviar uma ordem e cada um dos diversos participantes deverá executá-la
                    -> Mas, e se pelo menos um dos participantes não for capaz de completar a ordem?
                SOLUÇÕES:
                    -> Algoritmo de confirmação em duas fases (2-phase commit [2PC])
                        1. Começa-se por ter um coordenador que conhece os diversos participantes e a transação que deve ser efetuada
                        2. Comunica a ordem aos participantes (mas sem ordenar, apenas comunica que será dada essa ordem)
                        3. Cada um dos participantes prepara-se para executar essa ordem (sem executar) [verifica se tem os recursos necessários disponíveis, se sim, escreve em disco de forma persistente a sua
vontade de cumprir essa ordem, de forma a que mesmo que faça reboot, ao reiniciar veja que se tinha comprometido a cumprir esta ordem]
                        4. Participantes comunicam ao coordenador que está preparada (ou não) para efetuar a ordem
                        5. Coordenador toma uma decisão, se todas as respostas forem afirmativas -> poderá decidir afirmativamente (mas poderá também recusar), caso alguma das respostas seja negativa -> terá de desistir
                        6. Caso tome uma decisão afirmativa -> envia a ordem a todos os participantes
                        7. Participantes executam a ordem
                        8. Participantes informam o coordenador que executaram a ordem
                        9. Coordenador esquece tudo acerca da transação
                        * Caso não obtenha resposta à tentativa de informar a ordem, o coordenador terá um timeout e informará os participantes do cancelamento da ordem
                        * Limitação no caso do coordenador falhar -> se falhar após pedir aos participantes para se prepararem, estes ficarão com uma promessa pendente e terão de ficar bloqueados até receber resposta
do coordenador
                            <-> Não ultrapassável no 2PC
                            <-> Na prática, eventualmente o coordenador recupera e recorda-se que tem uma transação em curso e retoma a transação a partir das promessas dos participantes

            INVOCAÇÃO REMOTA TRANSACIONAL
                Capazes de passar alguma informação adicional (contexto transacional) de forma escondida entre clientes e servidores
*/                  