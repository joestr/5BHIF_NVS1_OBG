package xyz.joestr.school._5bhif.medianfilter_knecht;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the server which handles incoming KNECHT operations.
 * 
 * @author Joel
 */
public class SocketConnection extends Thread {
    
    private Queue<String> messagesToSend;
    private Queue<String> receivedMessages;
    
    ServerSocket serverSocket;
  
    @Override
    public void start() {
        
        try {
            this.messagesToSend = new ConcurrentLinkedQueue<>();
            this.receivedMessages = new ConcurrentLinkedQueue<>();
            
            Socket socket = new Socket("192.168.193.199", 55555);
            
            DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socketOut.writeBytes("hu");
                
                
                String receivedMessage = socketIn.readLine();
                
                if(receivedMessage != null) {
                    System.out.println(receivedMessage);
                }
            
        } catch (Exception ex) {
            Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Queue<String> getMessagesToSend() {
        return messagesToSend;
    }

    public Queue<String> getReceivedMessages() {
        return receivedMessages;
    }    
}