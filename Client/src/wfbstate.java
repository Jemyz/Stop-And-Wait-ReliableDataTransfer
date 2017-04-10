/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Geeek
 */
public class wfbstate extends receiverState {

    public wfbstate(String name) {
        super(name);

    }

    @Override
    public int action(String event, receiverSite r, int seqNo, DatagramSocket socket, dataPacket packet, InetAddress addr, int portNumber) {

        int senderSeqNo = packet.seqNo;

        if (seqNo == senderSeqNo && r.validateCKSum(packet)) {
            try {
                socket.send(r.getDatagramPacket(r.makeACKPacket(seqNo), addr, portNumber));

            } catch (IOException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            }

            return (seqNo + 1) % 2;

        } else {
            try {
                socket.send(r.getDatagramPacket(r.makeACKPacket(senderSeqNo), addr, portNumber));

            } catch (IOException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            }
            return seqNo;
        }
    }

    @Override
    public int action(String event, receiverSite r, int seqNo, DatagramSocket socket, ackPacket packet, InetAddress addr, int portNumber) {

        int senderSeqNo = packet.ackNo;

        if (seqNo == senderSeqNo && r.validateCKSum(packet)) {
            try {
                socket.send(r.getDatagramPacket(r.makeACKPacket(seqNo), addr, portNumber));

            } catch (IOException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            }

            return (seqNo + 1) % 2;

        } else {
            try {
                socket.send(r.getDatagramPacket(r.makeACKPacket(senderSeqNo), addr, portNumber));

            } catch (IOException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(wfbstate.class.getName()).log(Level.SEVERE, null, ex);
            }
            return seqNo;
        }

    }

}
