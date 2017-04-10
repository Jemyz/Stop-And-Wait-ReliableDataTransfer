/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Timestamp;
import java.util.Date;

/**
 *
 * @author mahmoud
 */
public class client implements senderSite, receiverSite {

    public int mainport;
    public int noOfport;
    byte[] ipAddr;
    InetAddress addr;
    int counter;
    public int serverPort;
    InetAddress serverAddr;
    double requiredProb;

    public static void main(String[] args) throws UnknownHostException, IOException, SocketException, ClassNotFoundException {

        int mainPort = 4000;
        int noOfport = 1;
        byte[] ipAddr = new byte[]{127, 0, 0, 1};
        InetAddress addr = InetAddress.getByAddress(ipAddr);
        int serverPort = 2000;
        InetAddress serverAddr = InetAddress.getByAddress(ipAddr);
        double requiredProb = 0.85;

        client c = new client(mainPort, noOfport, ipAddr, addr, serverPort, serverAddr, requiredProb);
        System.out.println("Client request file 1");
        System.out.println("");
        System.out.println("");
        System.out.println("");

        c.start(c);
        
        System.out.println("Client request file 2");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        
        c.start(c);
        
        System.out.println("Client request file 3");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        
        c.start(c);
        
        System.out.println("Client request file 4");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        
        c.start(c);

    }

    public client(int mainPort, int noOfport, byte[] ipAddr, InetAddress addr, int serverPort, InetAddress serverAddr, double requiredProb) throws UnknownHostException {

        this.mainport = mainPort;
        this.noOfport = noOfport;
        this.ipAddr = ipAddr;
        this.addr = addr;
        this.counter = 0;
        this.serverPort = serverPort;
        this.serverAddr = serverAddr;
        this.requiredProb = requiredProb;
    }

    public void start(client c) throws SocketException, IOException, ClassNotFoundException {

        System.out.println("Client Is Running");
        double sampleRTT = 8000;
        double EstimatedRTT = 0;
        double devRTT = 0;
        double RTTinterval;
        double[] times;
        int seqNo = 0;
        int newSeqNo = 0;
        String event;
        String fname;
        senderFSM sfsm;
        dataPacket datapacket;
        DatagramSocket socket = new DatagramSocket(c.mainport);
        fname = c.getFilename();
        byte[] data = fname.getBytes();

        datapacket = new dataPacket(data, seqNo);
        DatagramPacket datagramPacket = getDatagramPacket(datapacket, c.serverAddr, c.serverPort);
        sfsm = new senderFSM();
        event = "packetSend";

        System.out.println("Client Send File: " + fname);
        Date datetime1 = new Date();
        System.out.println(datetime1.toString());
        Long time1 = (long) (((((datetime1.getHours() * 60) + datetime1.getMinutes()) * 60) + datetime1.getSeconds()) * 1000);

        times = calcRTT(sampleRTT, EstimatedRTT, devRTT);
        RTTinterval = times[0];
        EstimatedRTT = times[1];
        devRTT = times[2];

        System.out.println("RTT: " + RTTinterval + " ms");

        ackPacket ackpacket;
        byte[] inBuf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(inBuf, inBuf.length);
        boolean correctpacket = false;

        while (!correctpacket) {

            sfsm.eventaction(event, c, seqNo, socket, datagramPacket, c.requiredProb, RTTinterval);
            System.out.println("Packet " + seqNo + " Is Sent");

            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Timeout Resending Packet");
                continue;
            }

            int source_port = packet.getPort();
            InetAddress source_IP = packet.getAddress();

            ackpacket = c.getACKPacket(packet);
            ackpacket.calcCKSum(); //added
            if (ackpacket.ackNo == seqNo && c.validateCKSum(ackpacket)) {
                event = "packetReceived1";
                newSeqNo = (seqNo + 1) % 2;
            } else {
                event = "packetReceived2";
            }
            sfsm.eventaction(event, c, seqNo, socket, packet, requiredProb, RTTinterval);

            System.out.println("Server Address: " + source_IP.toString() + " Server Port: " + source_port);
            System.out.println("ACK Packet " + ackpacket.ackNo + " Received");

            event = "packetReceived";

            if (newSeqNo != seqNo) {
                seqNo = (seqNo + 1) % 2;
                correctpacket = true;

            } else {
                System.out.println(" Error Resending ");
            }

        }

        Date datetime2 = new Date();
        System.out.println(datetime2.toString());
        Long time2 = (long) (((((datetime2.getHours() * 60) + datetime2.getMinutes()) * 60) + datetime2.getSeconds()) * 1000);

        sampleRTT = (double) (time2 - time1);
        System.out.println("sampleRTT " + sampleRTT);

        correctpacket = false;
        seqNo = 0;
        newSeqNo = 0;
        int source_port = 0;
        InetAddress source_IP = null;
        dataPacket dataPacket = null;
        ReceiverFSM rfsm = new ReceiverFSM();

        StringBuilder builder = new StringBuilder();
        socket.setSoTimeout(0);
        while (true) {

            correctpacket = false;
            while (!correctpacket) {

                socket.receive(packet);

                source_port = packet.getPort();
                source_IP = packet.getAddress();
                System.out.println(" ");
                System.out.println("Server Address: " + source_IP.toString() + " Server Port: " + source_port);
                dataPacket = this.getOriginalPacket(packet);
                event = "packetReceived";
                System.out.println("Packet " + dataPacket.seqNo + " Received");
                dataPacket.calcCKSum();//added
                newSeqNo = rfsm.eventaction(event, this, seqNo, socket, dataPacket, source_IP, source_port);
                System.out.println("ACK Packet " + seqNo + " Is Sent");
                if (newSeqNo == seqNo) {
                    System.out.println();
                    System.out.println("Duplicate OR Corrupted Packet");
                    continue;
                } else {
                    seqNo = newSeqNo;
                    correctpacket = true;
                }
            }

            if (new String(dataPacket.data).equals("Finished")) {
                break;
            }
            builder.append(new String(dataPacket.data));

        }

        if (builder.toString().trim().equals("File Not Exist")) {
            return;
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fname), "utf-8"))) {
            writer.write(builder.toString());
        }
        socket.close();

    }

    public String getFilename() {
        switch (this.counter) {

            case 0: {
                this.counter = this.counter + 1 % 4;
                return "gamal.txt";
            }

            case 1: {
                this.counter = this.counter + 1 % 4;
                return "hassan.txt";
            }

            case 2: {
                this.counter = this.counter + 1 % 4;
                return "oraby.txt";
            }

            default: {
                this.counter = this.counter + 1 % 4;
                return "false.txt";

            }
        }

    }

    public DatagramPacket getDatagramPacket(dataPacket packet, InetAddress addr, int portNumber) throws IOException, ClassNotFoundException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(packet.length);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        oos.flush();
        oos.close();

        byte[] data = baos.toByteArray();

        DatagramPacket dPacket = new DatagramPacket(data, data.length, addr, portNumber);
        return dPacket;

    }

    @Override
    public double[] calcRTT(double sampleRTT, double EstimatedRTT, double devRTT) {

        double alpha = 0.125;
        double beta = 0.25;
        double[] times = new double[3];

        EstimatedRTT = alpha * sampleRTT + (1 - alpha) * EstimatedRTT;
        devRTT = (1 - beta) * devRTT + beta * Math.abs(sampleRTT - EstimatedRTT);
        times[0] = EstimatedRTT + 4 * devRTT;
        times[1] = EstimatedRTT;
        times[2] = devRTT;
        return times;

    }

    @Override
    public boolean calcLostProb(double requiredProb) {

        if (Math.random() > requiredProb) {
            return false;
        }
        return true;

    }

    @Override
    public ackPacket makeACKPacket(int ackNo) {

        ackPacket packet = new ackPacket(ackNo);
        return packet;
    }

    @Override
    public boolean validateCKSum(dataPacket packet) {
        int dataLength = packet.data.length;
        int totalSum = 0;
        for (int i = 0; i < dataLength / 4; i += 4) {
            totalSum = totalSum + packet.data[i] + packet.data[i + 1] * 10 + packet.data[i + 2] * 100 + packet.data[i + 3] * 1000;
        }
        totalSum += packet.length + packet.seqNo;

        System.out.println("Check Sum of Data Packet: " + packet.cksum);
        System.out.println("Expected Check Sum of Data Packet: " + totalSum);

        if (packet.cksum == totalSum) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param packet
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public dataPacket getOriginalPacket(DatagramPacket packet) throws IOException, ClassNotFoundException {

        final ByteArrayInputStream baos = new ByteArrayInputStream((packet.getData()));
        final ObjectInputStream oos = new ObjectInputStream(baos);

        dataPacket datapacket = (dataPacket) oos.readObject();
        return datapacket;

    }

    @Override
    public DatagramPacket getDatagramPacket(ackPacket packet, InetAddress addr, int portNumber) throws IOException, ClassNotFoundException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(packet.length);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        oos.flush();
        oos.close();

        byte[] data = baos.toByteArray();

        DatagramPacket dPacket = new DatagramPacket(data, data.length, addr, portNumber);
        return dPacket;
    }

    @Override
    public ackPacket getACKPacket(DatagramPacket packet) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream baos = new ByteArrayInputStream((packet.getData()));
        final ObjectInputStream oos = new ObjectInputStream(baos);

        ackPacket ackpacket = (ackPacket) oos.readObject();
        return ackpacket;
    }

    @Override
    public boolean validateCKSum(ackPacket packet) {

        int totalSum = 0;
        totalSum += packet.ackNo;
        totalSum += packet.length;

        System.out.println("Check Sum of ACK Packet: " + packet.cksum);
        System.out.println("Expected Check Sum of ACK Packet: " + totalSum);

        if (packet.cksum == totalSum) {
            return true;
        }
        return false;
    }

}
