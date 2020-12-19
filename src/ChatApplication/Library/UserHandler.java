package ChatApplication.Library;

import ChatApplication.Server;

public class UserHandler implements Runnable {
    private final Server server;
    private final User user;

    public UserHandler(Server server, User user) {
        this.server = server;
        this.user = user;
    }

    public void run() {
        try {
            this.server.accountAndEntryManagement(this.user);
            this.server.broadcastMessages(new Message("Server", user + " joined the server."));
            this.server.broadcastAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                // gets client messages
                Message message = (Message) user.getOin().readObject();
//                System.out.println(message);            // todo löschen

                // checks end of thread
                if (message.getText().toLowerCase().equals("close")) {
                    server.removeUser(user);
                    server.broadcastMessages(new Message("Server", user + " has left the server."));
                    server.broadcastAllUsers();
                } else {
                    server.broadcastMessages(message);
                }
            } catch (Exception e) {
                System.out.println(user + " left.");
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(user + " left.");
                return;
            }
        }
    }
}