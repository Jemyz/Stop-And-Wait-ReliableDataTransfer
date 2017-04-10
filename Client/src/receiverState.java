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
public abstract class receiverState {

    public String name;

    public receiverState(String name) {
        this.name = name;
    }

    public abstract int action(String event, receiverSite r, int seqNo, DatagramSocket socket, ackPacket packet, InetAddress addr, int portNumber);

    public abstract int action(String event, receiverSite r, int seqNo, DatagramSocket socket, dataPacket packet, InetAddress addr, int portNumber);
}
