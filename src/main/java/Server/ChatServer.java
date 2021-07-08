package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;
    private final List<ClientHandler> clients;
    public ChatServer(){
        clients = new ArrayList<>();
        authService = new SimpleAuthService();


        try(ServerSocket serverSocket  = new ServerSocket(8189)){
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Server: Client connected ...");
                new ClientHandler(socket, this);

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void broadcast(String msq){
        for (ClientHandler client: clients) {
            client.sendMessage(msq);
        }
    }
    public void subscribe(ClientHandler clientHandler){

        clients.add(clientHandler);

    }
    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }


    public boolean isNickNameBusy(String nickname) {
        for (ClientHandler client :clients) {
            if(client.getName().equals(nickname)){
                return true;
            }
        }
        return false;
    }
}
