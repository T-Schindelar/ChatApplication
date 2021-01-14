package ChatApplication.Library;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements java.io.Serializable {
    private final String timestamp;
    private final String client;
    private final Mode mode;
    private final String text;
    private final String room;

    public Message(String client, Mode mode, String text, String room) {
        this.timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        this.client = client;
        this.mode = mode;
        this.text = text;
        this.room = room;
    }

    public Message(String client, Mode mode, String text) {
        this.timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        this.client = client;
        this.mode = mode;
        this.text = text;
        this.room = "Lobby";
    }

    // getter
    public String getClient() {
        return client;
    }
    public Mode getMode() {
        return mode;
    }
    public String getText() {
        return text;
    }
    public String getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + client + ": " + text;
    }
}
