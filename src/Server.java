/**
 * I, <Damanpreet_Singh>, student number <000741359>, certify that all code submitted is my own work;
 * that I have not copied it from any other source.
 * I also certify that I have not allowed my work to be copied by others.
 **/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server implements Runnable {

    // initialize the server socket
    private  Socket server;
    // The input stream for incoming messages
    private DataInputStream in;
    // The output stream for outgoing messages
    private DataOutputStream out;

    // Linked blocking queue for messages to be sent to all client
    public static  LinkedBlockingQueue<BackgroundHandler> queue = new LinkedBlockingQueue<BackgroundHandler>();

    // Array list of sockets to keep track of all clients
    public static ArrayList<Socket> connections = new ArrayList<Socket>();

    // Executor service for handling threads in the background task
    static ExecutorService executor = Executors.newCachedThreadPool();

    // The constructor for the Server
    public Server(Socket theSocket) throws IOException {
        server = theSocket;
    }

    @Override
    public void run() {
        String line = "";
        try {
            System.out.println("You Just connected to " + server.getRemoteSocketAddress());

            in = new DataInputStream(server.getInputStream());
            out = new DataOutputStream(server.getOutputStream());

            /* Echo back whatever the client writes until the client exits. */
            while (true) {
                if (in.available() > 0) {          // non blocking
                    line = in.readUTF();           // read line from client
                    if(line.contains("exit")) {
                        //used exit as a poison pill to stop the thread
                        // and remove the client from the array list
                        connections.remove(server);
                        break;
                    }
                    // Background Handler to handle the incoming message adn Broadcast to All
                    BackgroundHandler handler = new BackgroundHandler(line);
                    queue.put(handler);
                    executor.execute(handler);
                }

            }
            // close all connections
            out.close();
            in.close();
            server.close();

        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            System.out.println("You just disconnected from " + server.getRemoteSocketAddress());

        }
    }

    public static void main(String[] args) throws IOException {
        int port = 0;
        ServerSocket mySocket = new ServerSocket(port);  // Create the listening socket for client requests
        while (true) {
            System.out.println("Waiting for client on port "
                    + mySocket.getLocalPort() + "...");
            Socket server = mySocket.accept();            //blocking, awaiting a new client connection
            try {
                // new client connection received, spawn a thread to handle it
                Server s = new Server(server);
                Thread t = new Thread(s);
                t.start();
                connections.add(server);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

