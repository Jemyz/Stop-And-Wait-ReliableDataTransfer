/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author mahmoud
 */
public class senderFSM {
    senderState wfa;
    senderState wfc;
    senderState currentState;
    
    public senderFSM()
    {
        wfa = new wfastate("wfa");
        wfc = new wfcstate("wfc");
        currentState = wfc;
        
        
    }
    
    public int eventaction(String event, senderSite s, int seqNo, DatagramSocket socket, DatagramPacket packet , double requiredProb , double RTTinterval) 
    {
       if(event.equals("packetSend"))
       {
            currentState = this.wfc;
           currentState.action(event , s , seqNo , socket , packet , requiredProb , RTTinterval);
           currentState = this.wfa;
       }
       
       else if(event.equals("packetReceived1"))
       {
        
           currentState = this.wfc;

       }
       
       else if(event.equals("packetReceived2"))
       {
           currentState = this.wfa;
       }
        
        return 2;
        
    }
    
    
    
}
