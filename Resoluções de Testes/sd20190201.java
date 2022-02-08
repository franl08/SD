/* GRUPO 1.
    1. Em sistemas distribuídos é necessária uma distinição entre comunicação síncrona e comunicação assíncrona.
       A primeira caracteriza-se, tal como o nome indica, por ser necessária a sincronização entre o cliente e o servidor. Desta forma, para um correto funcionamento de um programa que utilize
este tipo de comunicação, quando um cliente enviar um pedido ao servidor, deverá bloquear até obter a resposta ao pedido pretendido.
      Por outro lado, a comunicação assíncrona dispensa esta necessidade de sincronização, ou seja, quando um cliente envia um pedido ao servidor, após o ter feito poderá continuar a executar
outras tarefas mesmo sem ter recebido resposta ao pedido enviado. Para isto ser possível, é necessário que a mensagem seja copiada para um buffer/queue de mensagens, sendo que a transmissão destas
mensagens ocorre em paralelo com a execução do emissor. É ainda importante realças que a receção poderá ser uma ação bloqueante (ou não), ou seja, quando recebe uma mensagem poderá bloquear até ter
terminado a receção.
      Para a comunicação síncrona poderá utilizar-se, por exemplo, como middleware o Message Passing em middleware orientado a mensagens.
      Para a comunicação assíncrona poderá utilizar-se, por exemplo, como middleware o Message Queuing em middleware orientado a mensagens.
    2. A transparência de acesso, em sistemas distribuídos, é responsável por ocultar do servidor/cliente os recursos a que este tem acesso e as diferenças na representação de dados, 
podendo estes trabalhar sobre eles como se eles estivessem presentes localmente, quer estes estejam, ou quer estejam a ser acedidos de forma remota, pois, o utilizador não deverá ser capaz
de perceber se o recurso que está a aceder é local ou remoto.
      Através do RPC, faz-se com que o sistema não tenha que fornecer a localização dos recursos, tornando-se os programas responsáveis pela execução dos processos de leitura e escrita de 
arquivos remotos da mesma forma que operam sobre os arquivos locais. Assim, o RPC encapsula as rotinas de acesso e efetua o controlo de concorrência do sistema distribuído.
    3. Os relógios de Lamport possuem diversas aplicações, sendo uma delas, por exemplo, a utilização de uma Base de Dados replicada em diversos pontos do globo.
       A utilização destes relógios surgem devido à falta de exatidão por parte dos relógios de tempo físico que, através de desajustes entre eles, podem ser prejudiciais ao sistema e, além disso,
a sincronização entre eles revela-se ainda complicada e, por vezes, inadequada ou demorada. Assim sendo, Lamport decidiu definir uma relação temporal que medisse não tempo físico, mas sim acontecimentos,
onde aquilo que interessaria como medida temporal do sistema seria aquilo que aconteceu primeiro. Por exemplo:
            - Sejam A e B eventos de um mesmo processo, se A foi executado antes de B, então A -> B.
            - Se A for o evento de envio de uma mensagem de um processo e B o evento de receção dessa mesma mensagem, teríamos também A -> B (visto que A foi executado primeiro).
            - Além disso, se A -> B e B -> C, então, A -> C.
      No entanto, continua a ser necessária alguma medida temporal para os processos poderem saber que estão antes ou depois de um outro processo, assim sendo, a cada evento do sistema é associada uma etiqueta
temporal, de forma a que se A -> B, então a etiqueta de A < etiqueta de B. 
            - Cada processo terá então um relógio associado que nada mais será do que um contador que é incrementado entre cada 2 eventos sucessivos do processo;
            - De forma aos diversos processos conhecerem os instantes temporais dos outros processos:
                + Cada mensagem enviada transporta o instante em que foi enviada.
                + Ao receber uma mensagem, caso o instante da mensagem for mais recente, o processo acerta o seu relógio para esse instante.
*/