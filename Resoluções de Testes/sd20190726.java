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
   2. A estruturação em camadas de uma aplicação distribuída revela-se uma grande mais valia, pois, desta forma é simplificada a distinção entre cliente e servidor, bem como é melhorada a escalabilidade
do sistema (visto que determinados componentes podem ser substituídos por outros sem grande prejuízo adicional).
      Geralmente, as aplicação distribuídas optam pela utilização do modelo em 3 camadas, derivado do modelo 'n' camadas, onde o sistema cliente/servidor é desenvolvido retirando a lógica do programa 
do lado do cliente, sendo, portanto, o servidor o único capaz de lhe aceder. As três camadas deste modelo são a de apresentação, de negócio e de dados.
   3. O protocolo de exclusão mútua é assente no cumprimento de 2 grandes requisitos: garantir que, no máximo, 1 thread se encontra dentro de uma secção crítica e excluir a possibilidade de starvation ou
deadlocks. 
      Para isto, a exclusão mútua tem base na assunção que o sistema consiste em N processos em que cada processo se encontra no seu próprio processador. De forma a cumprir os dois grandes requisitos,
qualquer processo que queira entrar numa secção crítica deverá, em primeiro lugar, enviar um pedido para o fazer ao processo coordenador e ficar bloqueado à entrada da secção enquanto não obtiver resposta 
afirmativa. Além disso, quando acabar a sua execução dentro da região crítica, deverá enviar uma mensagem ao processo coordenador a informar para que este liberte a zona, permitindo assim a entrada de novos
processos.
      Num protocolo de exclusão mútua centralizado, existirá apenas um servidor responsável pelo cumprimento da exclusão mútua do programa, sendo o administrador. Assim sendo, como desvantagens podemos
considerar que existe um ponto único de falha, ou seja, se o servidor tiver uma falha, todo o sistema colapsará e a existência de um gargalo, sobretudo em sistemas de maiores dimensões, pois apenas um servidor
se encontrará responsável pela resolução dos diversos pedidos. No entanto, a nível de vantagens podemos considerar que garante os requisitos da exclusão mútua, que se trata de um algoritmo justo, visto atender
os pedidos por ordem de chegada e que é simples e fácil de implementar.
*/
