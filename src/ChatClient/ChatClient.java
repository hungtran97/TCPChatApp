/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author nartu
 */
public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    public static void main(String[] args) throws IOException{
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }
            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + " ===> " + msgBody);
            }
        });
        if(!client.connect()){
            System.out.println("Connect failed!");
        } else {
            System.out.println("Connect successful");
            if(client.login("guest", "guest")){
                System.out.println("Login successful");
                
                client.msg("hung", "Hello world");
            } else {
                System.out.println("Login failed");
            }
        }
        //client.logoff();
    }
    
    public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }
    
    public boolean login(String login, String password) throws IOException{
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        
        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);
        if ("Ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }
    
    public void msg(String sendTo, String msgBody) throws IOException{
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
        
    }
    private void startMessageReader(){
        Thread t = new Thread() {
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }
    private void readMessageLoop() {
        try{
            String line;
            while( (line = bufferedIn.readLine()) != null){
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0){
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnline(tokens);
                    } else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    } else if("msg".equalsIgnoreCase(cmd)){
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }
    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    private void handleOnline(String[] tokens){
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.online(login);
        }
    }
    private void handleOffline(String[] tokens){
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListeners){
            listener.offline(login);
        }
    }
    
    private void handleMessage(String[] tokens){
        String login = tokens[1];
        String msgBody = tokens[2];
        
        for(MessageListener listener : messageListeners){
            listener.onMessage(login, msgBody);
        }
    }
        
    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }
    
    public void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }
    public void removeMessageListener(MessageListener listener){
        messageListeners.remove(listener);
    }
}
