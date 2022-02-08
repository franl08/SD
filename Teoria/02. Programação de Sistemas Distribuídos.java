/*  COMUNICAÇÃO:
        Físico: algumas consequências podem ser importantes para SD (p.e. quais os problemas)
        Data link: preocupação com a transmição de forma validada e integral de dados (redundância nos dados)
        Rede: preocupação com a possibilidade de uma mensagem ter de ser encaminhada por vários nodos na rede (encaminhamento e possibilidade de rotas alternativas)
        Transporte: abstração de canais fim a fim, independentemente de serem realizados através de mensagem individuais numa rede com múltiplos saltos
        Sessão: preocupação com o diálogo entre os intervenientes (interesse no estabelecimento de 1 ou + canais de comunicação / ser capazes de constituir canais que mantenham uma ligação mais duradoura do que no transporte)
        ApresentaçãO: tradução entre os diversos formatos utilizados para a comunicação ter sucesso
        Aplicação: diversidade de aplicações que utilizam a rede

        O modelo OSI pode ser utilizado para a programação em camadas, no entanto, não é muito interessante.
            -> Cada camada é considerada como um nível de abstração.

        Em SD o modelo será:
            Aplicação -> utilizam as camadas de middleware
            Middleware -> possivelmente algumas preocupações de transporte, mas sobretudo de sessão, apresentação e aplicação | encapsulam as soluções para os problemas mais difíceis dos sistemas distribuidos | determina a medida como
serão desenvolvidas as aplicações.
            Sistema Operativo -> camada de ligação, rede e transporte | oferece uma interface de programação | interesse no TCP/IP através de Sockets
            Hardware -> aspetos da camada física e de ligação do OSI

    SERIALIZAÇÃO:
        É necessária a utilização do método flush visto que o write apenas escreve para o buffer local, enquanto que o flush é que é responsável pela escrita no socket.
        Os sockets são bi-direcionais, ou seja, aceitam a leitura e a escrita de dados nos dois sentidos. Podemos utilzar shutdownOutput() ou shutdownInput() para tornar uni-direcional, fechando um dos sentidos

        Desafios para a escrita de código de conversão:
            1. Travessia recursiva das estruturas:
                ALTERNATIVAS
                    -> Filtros (escrita manual dos métodos que enumeram cada um dos componentes do objeto) [simples de compreender mas bastante complicada em programas maiores com grande estruturas de dados -> propensa a erros]
                    -> Capacidades de Reflexão presentes nas linguagens (OOS e OIS em JAVA utilizam isto, para evitar que certos campos não sejam serializados eles devem ser declarados como transient)
                    -> Geração automática do código das travessias feita a partir de uma descrição abstrata do tipo de dados (p.e. protobuf)
        A estrutra de dados é definida implicitamente no programa ou existe uma descrição da estrutura de dados?
            -> Nos filtros e na reflexão -> conteúdo das mensagens é inferido (1º caso pela sequência de leitura/escrita, no 2º pela própria definição da estrutura de dados) [bastante conveniente para o desenvolvimento, no entanto
está restrita a uma única linguagem]
            -> Na outra opção não [obriga o programador a utilizar novas ferramentas, mas dá maior liberdade]
        De que forma lidam com versões diferentes do mesmo sistema de dados?
            -> Na forma mais simples de travessia -> quando a estrutura/formato dos dados muda irá provocar problemas, porque ao descodificar os dados como pensa correto irá obter dados corrompidos
            SOLUÇÃO:
                1. Tornar os iténs individuais opcionais ou dar-lhes valor default (p.e. no Protobuf serão apenas lidos os campos que conhece/necessita)
                2. Permitir colocar uma versão da estrutura de dados como um todo (não permite a leitura mas evita a leitura de dados corrompidos) (utilizado pelos Java*Stream)
        Utiliza uma abordagem streaming (conversão feita à medida que é feita a travessia) ou um modelo dos dados serializados criado em memória que pode ser consultado por uma ordem arbitrária?
            -> Na travessia simples -> streaming (+ eficiente pq os dados são apenas copiados 1x e não gasta espaço em memória)
            -> Para a segunda abordagem pode ser utilizado o protobuf ou um método write que não crie o ficheiro de dados em si mas que devolva o JsonObject para depois ser consultado por qualquer ordem  (certas aplicações
conseguem "sobreviver" apenas com os dados em memória, evitando a 2ª cópia dos dados)
        Qual usar?
            -> Se estiver restrito a JAVA a biblioteca Kryo é uma boa alternativa
            -> Caso contrário a escolha será entre o Protobuf e o JSON (o XML seria outra opção mas é menos eficiente que o JSON)

    THREADS:
        SERVIDOR
            Servidor single-threaded -> Atende os pedidos dos diferentes clientes de forma sequencial [só pode atender 1 cliente de cada vez]
            Servidor thread-per-connection -> Cada vez que um cliente se liga ao servidor é criado um thread para processar os seus pedidos [adequado a clientes single-threaded visto que só podemos atender 1 pedido por cliente de cada vez]
            Servidor thread-per-request -> Além de um thread por cliente são necessários ainda threads dentro desse para cada pedido [necessários locks para controlo de concorrência] [necessários identificadores porque os pedidos podem ser efetuados
por qualquer ordem] [overhead adicional pela espera que o thread esteja apto a executar]
            Thread Pool -> tem-se um buffer de threads e quando um pedido chega é fornecido uma thread do buffer [necessários locks] [reduz o custo adicional de criação de threads] [evita a sobrecarga do servidor visto ter um limite de threads, logo de pedidos]
                - E se um pedido gerar uma resposta para diversos clientes?
                    * Impedir que em cada socket estejam a escrever threads oriundos de diferentes clientes -> associar a cada sessão um segundo thread e ter uma fila de respostas em direção a esse cliente, logo o thread irá adicionar a resposta à fila de espera
e o segundo thread acorda e sequencialmente escreve as respostas dirigidas ao cliente.

        CLIENTE
            Single-threaded -> Existe 1 thread da aplicação que tem um pedido para enviar ao servidor, esperando depois pela resposta
            Multi-threaded:
                SOLUÇÕES
                    * Limitar os pedidos de serviço numa secção crítica com locks [limitativo porque só permite 1 pedido de cada vez]
                    * Inserção de um Dispatcher [permitir que os vários threads efetuam vários pedidos, 1 de cada vez, as respostas serão lidas por um thread para colocar num buffer e cada thread deverá ler a resposta ao seu pedido]
                        - O dispatcher pode ser generalizado para um modelo simétrico, permitindo assim que o servidor também possa efetuar pedidos ao cliente [callbacks]
            
        Em Sistemas Distribuídos temos duas novas questões comparativamente a Sistemas Operativos:
            -> Onde é que um thread está a executar num determinado momento?
            -> Em que medida é que ele atravessa as fronteiras entre processos/máquinas?

        MIGRAÇÃO DE CÓDIGO
            Exemplos:
                -> SQL - o cliente não opera diretamente sobre os dados fornecendo apenas queries acerca deles.
                -> Aplicações WEB com JavaScript - o servidor fornece o código ao cliente, mas é o cliente que o executa

            Desafios conflituosos:
                -> Segurança: importante restringir aquilo que o código migrado possa fazer
                -> Eficiência: mais low level possível

            Solução: VIRTUALIZAÇÃO
                Exemplos:
                    -> Sistemas que compilam as linguagens para um byte code intermédio (p.e. Java, JavaScript) (permite ainda ultrapassar as dificuldades que possam surgir devido a diferentes arquiteturas, SOs e bibliotecas) 
(código é executado em cima de um sistema que faz a tradução para código nativo)
                    -> Virtualização do próprio sistema (p.e. Xen, VMWare) (sistema é corrido debaixo de um sistema de virtualização que garante o controlo de acesso)
                        * É necessário ainda o passo das infraestruturas de Cloud Computing (combinam virtualização com a capacidade de programar a própria infraestrutura) (criação/configuração de novos servidores é feita através
de um contexto de sistema distribuído e como resposta a pedidos de clientes) (permitem dar início a servidores em localizações precisas que podem ser depois utilizados para correr componentes de aplicações distribuídas)

    INVOCAÇÃO REMOTA:
        Middleware de invocação remota serve para esconder as interações entre clientes/servidores na invocação de procedimentos/métodos remotos. Consegue encapsular:
            -> Comunicação entre sockets
            -> Serialização
            -> Estratégias de threading (tanto no cliente, como no servidor)
            -> Serviços de nome
        Pode ser escrito mecanicamente através de uma implementação da interface. As diferentes alternativas são:
            1. Começar por escrever o código numa linguagem de programação (p.e. Java) e depois o sistema de invocação remota irá automaticamente gerar o stub e o servidor a partir do código existente (Java RMI)
            2. Escrever uma descrição na abstrata numa linguagem de descrição de interfaces que poderá depois ser compilada para diferentes linguagens e diferentes sistemas (Protobuf gRPC)
        Tipicamente os parâmetros são cópias tanto do cliente para o servidor como do servidor para o cliente.
            -> Em alguns casos, alguns sistemas permitam que alguns parâmetros sejam copiados de volta (p.e. um objeto passado como parâmetro, este é serializado e enviado ao servidor, permite que o servidor modifique esse objeto e depois
é copiado de volta para o cliente)
            -> Os parâmetros podem ser etiquetados como de entrada, saída ou ambos
        
*/