/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mahmoud
 */
public class wfcstate extends senderState{

    public wfcstate(String name) {
        super(name);
    }

    @Override
    public void action(String event, senderSite s, int seqNo, DatagramSocket socket, DatagramPacket packet ,  double requiredProb , double RTTinterval) {
       
       boolean b = s.calcLostProb(requiredProb);
        System.out.println("\nProbability check " + b + "\n");
       if(b)
       {
           try {
            socket.send(packet);
           } catch (IOException ex) {
               Logger.getLogger(wfcstate.class.getName()).log(Level.SEVERE, null, ex);
           }
                    
       }
       
        try {
               socket.setSoTimeout((int) RTTinterval);
           } catch (SocketException ex) {
               Logger.getLogger(wfcstate.class.getName()).log(Level.SEVERE, null, ex);
           }
        
        
        
    }

   
    
    
  
    
}
