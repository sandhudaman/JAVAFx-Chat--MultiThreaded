import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * Client side GUI that makes a connection to a server
 *
 * @author YOUR NAME
 */
public class FXClient extends Application {

    private static final int TOTAL_THREADS = Runtime.getRuntime().availableProcessors();
    private static Button submit;
    private static TextField input;
    private static Label output;
    private ScrollPane messages;

    // DataStreams for sending and receiving data
    static DataOutputStream out;
    static DataInputStream in;
    // Socket for the client
    static Socket client;
    // String for the username
    public static String userName;

    /**
     * Start method (use this instead of main).
     *
     * @param stage The FX stage to draw on
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root);
        Canvas canvas = new Canvas(400, 600); // Set canvas Size in Pixels
        stage.setTitle("FXClient"); // Set window title
        root.getChildren().add(canvas);
        stage.setScene(scene);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Initialize all GUI components and add them to the Root
        submit = new Button("Submit");
        input = new TextField();
        output = new Label();
        VBox box = new VBox();
        messages = new ScrollPane();
        root.getChildren().addAll(submit, input, messages);

        // Relocate and set sizes for all GUI components
        input.relocate(10, 500);
        input.setPrefWidth(300);
        submit.relocate(325, 500);
        messages.relocate(10, 10);
        messages.setPrefWidth(390);
        messages.setPrefHeight(440);
        output.setPrefWidth(375);
        output.setPrefHeight(5000);
        // set padding for output label
        output.setPadding(new Insets(10, 10, 10, 10));
        output.setAlignment(Pos.TOP_LEFT);
        output.setWrapText(true);
        box.getChildren().add(output);
        messages.setContent(box);
        messages.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);


        //request port number from user
        Scanner keyboard = new Scanner(System.in);
        System.out.println("What is the port number to connect to: ");
        int port = keyboard.nextInt();
        //request port number from user
        Scanner keyboard1 = new Scanner(System.in);
        System.out.println("What would you line your username to Be: ");
        userName = keyboard1.nextLine();

        // Create a new socket to communicate over
        client = new Socket("localhost", port);
        System.out.println("Just connected to " + client.getRemoteSocketAddress());

        // Create input and output streams that connect with server
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());

        // send message to current user that they have connected
        out.writeUTF(userName + " has connected to the server\n");

        output.setText(in.readUTF());
        // show the GUI
        stage.show();

        // Create a new thread to handle the server's messages

        // Add an action listener to the submit button
        submit.setOnAction(this::handleButtonPress);

        // invoking background listener Task
       invokeBackgroundService();

        // When the window is closed, close the socket by send
        // a message to the server that the user has disconnected
        // and then send a poison pill to the thread pool
        // and then close the socket and exit the program
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                // loop through connection list and close current connection
                try {
                    out.writeUTF(userName + " has disconnected from the server\n");
                    sleep(100);
                    out.writeUTF("exit");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.exit(0);
            }
        });

    }
    /**
     * Method to invoke background Task
     * for listening to server messages
     * @param event The event that triggered the method
     */
    private void invokeBackgroundService() {
        BackgroundTask service = new BackgroundTask(client);          // create new background task by passing socket
        service.valueProperty().addListener(new ChangeListener() {    // add listener to background task
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                output.setText(output.getText() + t1 + "\n");
            }
        } );

        Thread t = new Thread(service);     // create new thread for background task
        t.start();                           // start thread
    }
    /**
     * Handles Submit button presses
     **/
    public void handleButtonPress(ActionEvent actionEvent) {
        // get the text from the input field
        String text = userName + " : " + input.getText();
        // send the text to the server
        try {
            out.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // clear the input field
        input.clear();

    }
    /**
     * The actual main method that launches the app.
     * @param args unused
     */
    public static void main(String[] args) throws Exception {
        launch(args);
        // close all
        try {
            FXClient.out.writeUTF("exit");
            FXClient.out.close();
            FXClient.in.close();
            FXClient.client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            //System.out.println("Client has disconnected from the server..."+ FXClient.client.getRemoteSocketAddress());
        }
    }
}
