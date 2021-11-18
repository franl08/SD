import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {

    public static Contact parseLine (String userInput) {
        String[] tokens = userInput.split(" ");

        if (tokens[3].equals("null")) tokens[3] = null;

        return new Contact(
                tokens[0],
                Integer.parseInt(tokens[1]),
                Long.parseLong(tokens[2]),
                tokens[3],
                new ArrayList<>(Arrays.asList(tokens).subList(4, tokens.length)));
    }


    public static void main (String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream dis = new DataInputStream(new DataInputStream(socket.getInputStream()));

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            ContactList cl = ContactList.deserialize(dis);
            if(!cl.isEmpty())
                for(Contact c : cl)
                    System.out.println(c);

            String userInput;
            while ((userInput = in.readLine()) != null) {
                Contact newContact = parseLine(userInput);
                newContact.serialize(dos);
                dos.flush();
            }
        } catch (Exception e){
            System.out.println("Something went wrong :(");
        }

        socket.close();
    }
}
