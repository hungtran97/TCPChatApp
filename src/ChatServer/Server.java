/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nartu
 */
public class Server {
    private final int serverPort;
    
    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort){
        this.serverPort = serverPort;
    }
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                System.out.println("About to accept client connection..");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connnection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void removeWorker(ServerWorker serverWorker){
        workerList.remove(serverWorker);
    }
}
