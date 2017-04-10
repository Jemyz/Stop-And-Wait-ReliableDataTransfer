/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author mahmoud
 */
public class wfastate extends senderState{

    public wfastate(String name) {
        super(name);
    }

    @Override
    public void action(String event, senderSite s, int seqNo, DatagramSocket socket, DatagramPacket packet, double requiredProb, double RTTinterval) {

        
    }

    

    
    
}
