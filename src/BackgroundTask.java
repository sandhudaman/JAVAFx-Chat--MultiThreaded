import javafx.concurrent.Task;
import javafx.scene.control.Label;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * BackgroundTask class that extends Task
 * This class is used to create a background task that will run in the background
 * while the GUI is still running to listen for messages from the server
 */
public class BackgroundTask extends Task<String> {
    // The Socket for communication
    private Socket client;
    // The input stream a client will send messages to
    private DataInputStream in;

    // The constructor for the BackgroundTask
    public BackgroundTask(Socket client) {
        this.client = client;
    }
    @Override
    protected String call() throws Exception {
        in = new DataInputStream(client.getInputStream());        // Create the input stream for the client
        while (true) {
            try {
                if (in.available() > 0) {
                    String message = in.readUTF();                 // Read the message from the client
                    //System.out.println("Server response: " + message);
                    // update the message to the UI
                    updateValue(message);                       // Update the message to the GUI
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
