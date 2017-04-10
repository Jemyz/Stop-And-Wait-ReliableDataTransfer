/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Geeek
 */
public class ReceiverFSM {
    receiverState wfb;
    receiverState currentState;
    public ReceiverFSM()
    {
        wfb = new wfbstate("wfb");
        currentState = wfb;
    }
    public int eventaction(String event, receiverSite r, int seqNo, DatagramSocket socket, dataPacket packet , InetAddress addr , int portNumber)
    {
        if(event.equals("packetReceived"))return currentState.action(event, r, seqNo, socket, packet , addr , portNumber);
        return 2;
        
    }
    
     public int eventaction(String event, receiverSite r, int seqNo, DatagramSocket socket, ackPacket packet , InetAddress addr , int portNumber)
    {
        if(event.equals("packetReceived"))return currentState.action(event, r, seqNo, socket, packet , addr , portNumber);
        return 2;
        
    }
    
}
