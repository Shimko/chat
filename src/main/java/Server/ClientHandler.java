package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String name;

    public ClientHandler(Socket socket, ChatServer server) {
        try {
            this.name = "";
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while (true) {
                    try {
                        authenticate();
                        readMessages();
                    } finally {
                        closeConnection();
                    }
                }

            }).start();// :: означает что это ссылка на метод

        }catch (IOException e ){
            throw  new RuntimeException(" не могу создать обработчик для клиента", e);
        }
    }

    public String getName() {
        return name;
    }

    private void authenticate() {
        while (true){
            try {
                final String str = in.readUTF();
                if (str.startsWith("/auth")){ // /auth  login1 pass1
                    final String[] split = str.split("\\s");
                    final String login = split[1];
                    final String pass = split[2];
                    final String nickname = server.getAuthService().getNicknameByLoginAndPassword(login, pass);
                    if(nickname!=null){
                        if(!server.isNickNameBusy(nickname)){
                            sendMessage("/autok" + nickname);
                            this.name = nickname;
                            server.broadcast("пользователь "+ nickname+" зашел в чат");
                            server.subscribe(this);

                        }else{
                            sendMessage("Уже произведен вход в учетную запись");
                        }
                    }else {
                        sendMessage(" Неверные логин или пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw  new RuntimeException(e);
            }
        }
    }

    private void closeConnection() {
        try {
            server.unsubscribe(this );
            in.close();
            out.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
            throw  new RuntimeException(e);
        }
    }

    public void sendMessage(String msq){

        try{
            out.writeUTF(msq);
        }catch (IOException e){
            e.printStackTrace();
            throw  new RuntimeException(e);
        }
    }

    public void readMessages() {
        try {
            while (true) {
                final String strFromClient = in.readUTF();
                if("/end".equals(strFromClient)){
                    return;
                }
                System.out.println("Получено сообщение от " + name +": "+strFromClient);
            }
        }catch (IOException e ){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
