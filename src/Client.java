import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client extends Application{

    BufferedReader in;
    PrintWriter out;

    String hostName = "192.168.10.1";
    int portNumber = 4444;
    boolean ready=false;
    String userName;

    StackPane root = new StackPane();

    //CONNECT STAGE

    GridPane wrapperPane = new GridPane();
    Label hostLable = new Label("Host:");
    Label portLable = new Label("Port:");
    Label titleLabel = new Label("Online Quiz");
    TextField hostField = new TextField();
    TextField portField = new TextField();
    Button hostNextButton = new Button("Continue");


    //QUIZ STAGE
    GridPane quizWrapperPane = new GridPane();
    FlowPane userPane = new FlowPane();
    TextArea serverOutput = new TextArea();
    TextArea userArea = new TextArea();
    TextField userInput = new TextField();
    Button sendButton = new Button("Send");

    //USERNAME POPUP
    Popup userPop = new Popup();
    GridPane popPane = new GridPane();
    Label insertUserLable = new Label("Insert a username:");
    TextField userNameField = new TextField();
    Button insertUserNameButton = new Button("Continue");


    @Override
    public void start(Stage primaryStage) throws Exception{


        //SETTING UP CONNECT STAGE
        System.out.println("start has begun");
        root.getChildren().add(wrapperPane);
        wrapperPane.add(titleLabel, 2, 0);
        wrapperPane.add(hostLable, 1, 1);
        wrapperPane.add(portLable, 1, 2);
        wrapperPane.add(hostField, 2,1);
        wrapperPane.add(portField, 2,2);
        wrapperPane.add(hostNextButton,2,3);

        root.setPadding(new javafx.geometry.Insets(50, 60, 10, 60));


        //SETTING UP QUIZ STAGE
        quizWrapperPane.add(serverOutput, 1, 1);
        quizWrapperPane.add(userArea,2,1);
        quizWrapperPane.add(userInput,1,2);
        userInput.setPrefWidth(300);
        serverOutput.setPrefHeight(450);
        serverOutput.setEditable(false);
        serverOutput.setWrapText(true);
        userArea.setMaxWidth(100);
        userArea.setEditable(false);
        userInput.setEditable(false);
        serverOutput.setMaxWidth(300);

        //SETTING UP POPUP
        userPop.getContent().add(popPane);
        popPane.add(insertUserLable, 1, 1);
        popPane.add(userNameField, 1,2);
        popPane.add(insertUserNameButton, 1, 3);


        primaryStage.setScene(new Scene(root, 400, 450));
        primaryStage.show();

        //PORT/HOST NEXT BUTTON LISTENER
        hostNextButton.setOnAction((event) -> {
            hostName = hostField.getText();
            portNumber = Integer.parseInt(portField.getText());
            setUpQuiz();
            userPop.show(primaryStage);
        });


        //POPUP NEXT BUTTON LISTENER
        insertUserNameButton.setOnAction((event) -> {
            userName=userNameField.getText();
            userPop.hide();
            userInput.setEditable(true);
            userInput.requestFocus();
            serverOutput.appendText("Welcome to Quiz Online "+userName+"!\n");
            serverOutput.appendText("Get 10 points in order to win the match by answering the questions first.\nA question will appear shortly.\n");
            exec.execute(() -> {
                try {
                    connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });


        serverOutput.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                serverOutput.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });
    }

    //Creates thread for connection method
    private final Executor exec = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t ;
    });


    //CHANGING PANE FROM CONNECT TO QUIZ
    public void setUpQuiz() {
        System.out.println("Setup quiz screen");
        root.getChildren().remove(wrapperPane);
        root.getChildren().add(quizWrapperPane);
        root.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));
        userInput.requestFocus();
        ready=true;
        System.out.println("Quiz screen ready");
    }

    public Client() {
        userInput.setOnAction((event) -> {
            if(userInput.getText().equals("/quit")){
                serverOutput.setText("Session has ended...");
                out.println(userInput.getText());
                userInput.setText("");
            }else {
                out.println(userInput.getText());
                userInput.setText("");
            }
        });
    }


    //CONNECTION METHOD
    public void connect() throws IOException {

        String serverAddress = hostName;
        Socket socket = new Socket(serverAddress, portNumber);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);


        //SERVER INPUT HANDLER
        while (true) {
            String line = in.readLine();

            if (line.startsWith("SUBMITNAME")) {
                out.println(userName);

            } else if (line.startsWith("MESSAGE")) {
                Platform.runLater(()->serverOutput.appendText(line.substring(8) + "\n"));

            } else if (line.startsWith("QUESTION")) {
                Platform.runLater(()->serverOutput.appendText("\n" + "-----" + "\n" + line.substring(8) + "\n" + "-----" + "\n"));

            } else if (line.startsWith("CORRECTANSWER")) {
                Platform.runLater(()->serverOutput.appendText(line.substring(14) + "\n"));
            } else if (line.startsWith("USERUPDATE")) {
                System.out.println("Getting userlist");
                String list = (line.substring(11)).replace(";", "\n");
                Platform.runLater(()->userArea.setText(list));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}