package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {



    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;

    @FXML
    private HBox clientPanel;
    @FXML
    private HBox msgPanel;
    @FXML
    private Button btnSend;
    @FXML
    private TextField textField;
    @FXML
    private ListView clientList;
    @FXML
    private TextArea textArea;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField loginField;
    @FXML
    private HBox authPanel;


    private void connect() {
        try {
            this.socket  = new Socket("localhost", 8189);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            setAuth(false);
            new Thread(()->{
                try {
                    while (true){// ждем сообщение об успешной авторизации

                        final String msgFromServer = in.readUTF();
                        if(msgFromServer.startsWith("/authok")){
                            setAuth(true);
                            break;
                        }
                        textArea.appendText(msgFromServer+"\n");

                    }
                    while (true){
                        final  String msgFromServer = in.readUTF();
                        if ("/end".equalsIgnoreCase(msgFromServer)) {
                            break;
                        }
                    }
                } catch (IOException e) {
                        e.printStackTrace();
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void setAuth(boolean isAuthSuccess) {
        authPanel.setVisible(!isAuthSuccess);
        authPanel.setManaged(!isAuthSuccess);

        msgPanel.setVisible(isAuthSuccess);
        msgPanel.setManaged(isAuthSuccess);

        clientPanel.setVisible(isAuthSuccess);
        clientPanel.setManaged(isAuthSuccess);
    }

    public void sendAuth(ActionEvent actionEvent) {
        try {
            out.writeUTF("/auth"+ loginField.getText()+" " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
    }
}
