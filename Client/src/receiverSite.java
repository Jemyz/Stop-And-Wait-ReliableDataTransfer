/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author mahmoud
 */
public interface receiverSite {

    public ackPacket makeACKPacket(int ackNo);

    public DatagramPacket getDatagramPacket(ackPacket packet, InetAddress addr, int portNumber) throws IOException, ClassNotFoundException;

    public boolean validateCKSum(dataPacket packet);

    public boolean validateCKSum(ackPacket packet);

    public ackPacket getACKPacket(DatagramPacket packet) throws IOException, ClassNotFoundException;
}
