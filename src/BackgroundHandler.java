import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BackgroundHandler implements Runnable {

    private String message;
    public BackgroundHandler(String message) {
        this.message = message;
    }
    @Override
    public void run() {
        try {
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





