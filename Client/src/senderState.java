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
public abstract class senderState{
    
    public String name;
    public senderState(String name)
    {
        this.name = name;
    }
    
    public abstract void action(String event, senderSite s, int seqNo, DatagramSocket socket, DatagramPacket packet  , double requiredProb , double RTTinterval);
    
    
}
