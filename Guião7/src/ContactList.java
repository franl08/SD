import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class ContactList extends ArrayList<Contact> {

    public void serialize (DataOutputStream out) throws IOException {
        out.writeInt(this.size());
        for(Contact c : this)
            c.serialize(out);
    }

    public static ContactList deserialize (DataInputStream in) throws IOException {
        ContactList c = new ContactList();
        int size = in.readInt();
        for(int i = 0; i < size; i++) c.add(Contact.deserialize(in));
        return c;
    }

}
