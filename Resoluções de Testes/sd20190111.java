/* GRUPO 1.
    1. Em sistemas distribuídos, é possível distinguir escala geográfica de escala numérica.
       A escala geográfica trata-se da resolução do possível problema que possa surgir a nível da comunicação entre recursos e utilizadores que se encontrem em pontos distintos e afastados que,
poderia causar atrasos na transmissão de dados e, portanto, delay para o utilizador. No entanto, isto não é desejável, ou seja, independentemente da localização física dos recursos/utilizadores,
a comunicação não deverá ser gravemente afetada. Uma das soluções para mitigar este problema é a replicação de dados ou a criação de caches em locais mais próximos dos recursos/utilizadores,
assim sendo, os dados encontrar-se-ão mais próximos do destino e os problemas relativos à latência serão diminuídos.
      Já ao nível da escala numérica, esta trata-se da resolução do possível problema que possa surgir quando um número muito elevado de utilizadores tenta aceder ao sistema de forma concorrente,
podendo provocar grandes atrasos na utilização do sistema entre os diversos utilizadores, algo que não é desejado. De forma a combater isto, uma solução possível é aumentar a quantidade de
hardware/servidores aptos para responder aos diversos utilizadores, desta forma, permite-se que a aplicação se torne escalável a uma maior quantidade de utilizadores.
    2. A transparência de acesso, em sistemas distribuídos, é responsável por ocultar do servidor/cliente os recursos a que este tem acesso e as diferenças na representação de dados, 
podendo estes trabalhar sobre eles como se eles estivessem presentes localmente, quer estes estejam, ou quer estejam a ser acedidos de forma remota, pois, o utilizador não deverá ser capaz
de perceber se o recurso que está a aceder é local ou remoto.
      Através do RPC, faz-se com que o sistema não tenha que fornecer a localização dos recursos, tornando-se os programas responsáveis pela execução dos processos de leitura e escrita de 
arquivos remotos da mesma forma que operam sobre os arquivos locais. Assim, o RPC encapsula as rotinas de acesso e efetua o controlo de concorrência do sistema distribuído.
    3. Uma das formas de mitigar esta incerteza de forma eficiente é através do protocolo de relógios de Lamport.
       Para implementar este algoritmo é necessário que cada processo mantenha um contador que servirá como contador do tempo lógico do programa. A sua atualização surge através das seguintes etapas:
            -> Antes de executar um evento qualquer (enviar uma mensagem, receber uma mensagem ou um evento interno), o processo deve incrementar o seu contador em 1 unidade;
            -> Quando um processo envia uma mensagem, deverá enviar essa mensagem a todos os processos e esta possui um timestamp que indicará o valor do contador de tempo lógico em A até ao momento
do envio da mensagem;
            -> Na receção de uma mensagem, o processo deverá ajustar o seu contador local através do máximo entre a timestamp recebida e o seu contador, incrementando-o em seguida por uma unidade.
*/