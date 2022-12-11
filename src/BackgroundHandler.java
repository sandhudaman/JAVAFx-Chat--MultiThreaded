import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class will handle the Broadcast messages from the server
 * to all the clients in ArrayList of connected clients
 */

public class BackgroundHandler implements Runnable {

    // Message to be sent to all clients
    private String message;
    // Constructor to initialize the message
    public BackgroundHandler(String message) {
        this.message = message;
    }
    @Override
    public void run() {
        try {
            // Loop through all the clients in the ArrayList
            // and send the message to each client
            // Dispalay the message on console as well
            for (Socket client : Server.connections) {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.writeUTF(message);
                System.out.println("Sent message to client: "+ client.toString()+  message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}





