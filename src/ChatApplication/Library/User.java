package ChatApplication.Library;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

public class User {
    private final Socket client;
    private String name;
    private final ObjectOutputStream oout;
    private final ObjectInputStream oin;

    public User(Socket client, String name, ObjectInputStream oin) throws IOException {
        this.client = client;
        this.name = name;
        this.oin = oin;
        this.oout = new ObjectOutputStream(client.getOutputStream());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    // getter
    public ObjectInputStream getOin() {
        return oin;
    }
    public ObjectOutputStream getOout() {
        return oout;
    }
    public String getName() {
        return name;
    }

    // setter
    public void setName(String name) {
        this.name = name;
    }
}
