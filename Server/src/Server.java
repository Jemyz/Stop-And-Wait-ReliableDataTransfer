/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mahmoud
 */
public class Server extends Thread implements receiverSite, senderSite {

    public int mainport;
    public int noOfport;
    byte[] ipAddr;
    byte[] fileData;
    InetAddress addr;
    String fname;
    InetAddress source_IP;
    int source_port;

    public static void main(String[] args) throws IOException, SocketException, ClassNotFoundException {

        int mainPort = 2000;
        int noOfport = 1;
        byte[] ipAddr = new byte[]{127, 0, 0, 1};
        InetAddress addr = InetAddress.getByAddress(ipAddr);

        Server s = new Server(mainPort, noOfport, ipAddr, addr);
        s.start(s);
    }
    private Object builder;

    public Server(int mainPort, int noOfport, byte[] ipAddr, InetAddress addr) throws UnknownHostException {

        this.mainport = mainPort;
        this.noOfport = noOfport;
        this.ipAddr = ipAddr;
        this.addr = addr;
    }

    public Server(int mainPort, int noOfport, byte[] ipAddr, InetAddress addr, byte[] fileData, InetAddress source_IP, int source_port) throws UnknownHostException {

        this.mainport = mainPort;
        this.noOfport = noOfport;
        this.ipAddr = ipAddr;
        this.addr = addr;
        this.fileData = fileData;
        this.source_IP = source_IP;
        this.source_port = source_port;

    }

    public void start(Server s) throws SocketException, IOException, ClassNotFoundException {
        while (true) {
            System.out.println("Server Is Running");
            int seqNo = 0;
            int newSeqNo = 0;
            String event;
            String fname = "";
            ReceiverFSM rfsm;
            dataPacket dataPacket;
            DatagramSocket socket = new DatagramSocket(s.mainport);
            byte[] inBuf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(inBuf, inBuf.length);
            System.out.println("Server Is Waiting For Filename");
            boolean correctpacket = false;
            int source_port = 0;
            InetAddress source_IP = null;

            while (!correctpacket) {

                System.out.println("Packet Sequence Number " + seqNo + " Expected");

                socket.receive(packet);

                source_port = packet.getPort();
                source_IP = packet.getAddress();
                System.out.println("Client Address: " + source_IP.toString() + " Client Port: " + source_port);
                dataPacket = s.getOriginalPacket(packet);
                System.out.println("Packet Sequence Number " + dataPacket.seqNo + " Received");

                rfsm = new ReceiverFSM();
                event = "packetReceived";
                dataPacket.calcCKSum();
                newSeqNo = rfsm.eventaction(event, s, seqNo, socket, dataPacket, source_IP, source_port);

                if (newSeqNo == seqNo) {
                    System.out.println("Duplicate OR Corrupted Packet");
                    continue;
                } else {
                    seqNo = newSeqNo;
                    fname = new String(dataPacket.data);
                    System.out.println("File Name: " + fname);
                    correctpacket = true;
                }

            }

            socket.close();
            System.out.println("");
            System.out.println("");
            System.out.println("");

            byte[] fileData;
            
            try {
                File file = new File(fname);
            RandomAccessFile f = new RandomAccessFile(file, "r");
            fileData = new byte[(int) f.length()];
            f.readFully(fileData);  
            } catch (FileNotFoundException e) {
                fileData = "File Not Exist".getBytes();
                System.out.println("");
                System.out.println("File Not Exist");
                System.out.println("");
            }
            
            
            Thread t1 = new Server(s.mainport, s.noOfport, s.ipAddr, s.addr, fileData, source_IP, source_port);
            t1.start();
 
        }
    }

    public byte[] getFileBytes(String fname) throws FileNotFoundException, IOException {
        File file = new File(fname);
        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] b = new byte[(int) f.length()];
        f.readFully(b);
        return b;
    }

    public dataPacket getOriginalPacket(DatagramPacket packet) throws IOException, ClassNotFoundException {

        final ByteArrayInputStream baos = new ByteArrayInputStream((packet.getData()));
        final ObjectInputStream oos = new ObjectInputStream(baos);

        dataPacket datapacket = (dataPacket) oos.readObject();
        return datapacket;

    }

    @Override
    public void run() {

        System.out.println("Current Thread Is " + this.currentThread().getId());

        double requiredprob = 0.98;
        int length = 500;
        int n = (int) Math.ceil(this.fileData.length / 500.0);
        System.out.println(n + " Packets to be Sent");

        double[] sampleRTTanalysis = new double[n + 1];
        double[] EstimatedRTTanalysis = new double[n + 1];
        double[] devRTTanalysis = new double[n + 1];
        double[] RTTintervalanalysis = new double[n + 1];

        byte[][] dataArray = new byte[n + 1][length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < length; j++) {

                if (i * length + j == this.fileData.length) {
                    break;
                }
                dataArray[i][j] = this.fileData[i * length + j];
            }
            // System.out.println(new String(dataArray[i]));
            // System.out.println("");

        }
        dataArray[n] = "Finished".getBytes();

        double sampleRTT = 5;
        double EstimatedRTT = 0;
        double devRTT = 0;
        double RTTinterval;
        double[] times;
        int seqNo = 0;
        int newSeqNo = 0;
        String event;
        senderFSM sfsm;
        dataPacket datapacket;

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < n + 1; i++) {

            datapacket = new dataPacket(dataArray[i], seqNo);
            DatagramPacket datagramPacket = null;
            try {
                datagramPacket = getDatagramPacket(datapacket, this.source_IP, this.source_port);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            sfsm = new senderFSM();
            event = "packetSend";

            Date datetime1 = new Date();
            System.out.println(datetime1.toString());
            Long time1 = (long) (((((datetime1.getHours() * 60) + datetime1.getMinutes()) * 60) + datetime1.getSeconds()) * 1000);

            datetime1 = new Date();
            time1 = (long) (((((datetime1.getHours() * 60) + datetime1.getMinutes()) * 60) + datetime1.getSeconds()) * 1000);

            times = calcRTT(sampleRTT, EstimatedRTT, devRTT);
            RTTinterval = times[0];
            EstimatedRTT = times[1];
            devRTT = times[2];

            sampleRTTanalysis[i] = sampleRTT;
            EstimatedRTTanalysis[i] = EstimatedRTT;
            devRTTanalysis[i] = devRTT;
            RTTintervalanalysis[i] = RTTinterval;

            System.out.println("RTT: " + RTTinterval + " ms");

            ackPacket ackpacket = null;
            byte[] inBuf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(inBuf, inBuf.length);
            boolean correctpacket = false;

            datetime1 = new Date();
            time1 = (long) (((((datetime1.getHours() * 60) + datetime1.getMinutes()) * 60) + datetime1.getSeconds()) * 1000);

            while (!correctpacket) {
                event = "packetSend";
                sfsm.eventaction(event, (senderSite) this, seqNo, socket, datagramPacket, requiredprob, RTTinterval);
                System.out.println("Packet " + seqNo + " Is Sent");

                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout Resending Packet");
                    continue;
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

                int source_port = packet.getPort();
                InetAddress source_IP = packet.getAddress();

                try {
                    ackpacket = this.getACKPacket(packet);
                    ackpacket.calcCKSum();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (ackpacket.ackNo == seqNo && this.validateCKSum(ackpacket)) {
                    event = "packetReceived1";
                    newSeqNo = (seqNo + 1) % 2;
                } else {
                    event = "packetReceived2";
                }
                sfsm.eventaction(event, this, seqNo, socket, packet, requiredprob, RTTinterval);

                System.out.println("Server Address: " + source_IP.toString() + " Server Port: " + source_port);
                System.out.println("ACK Packet " + ackpacket.ackNo + " Received");

                if (newSeqNo != seqNo) {
                    seqNo = (seqNo + 1) % 2;
                    correctpacket = true;

                } else {
                    System.out.println(" Error Resending ");
                }

            }
            Date datetime2 = new Date();
            long time2 = (long) (((((datetime2.getHours() * 60) + datetime2.getMinutes()) * 60) + datetime2.getSeconds()) * 1000);

            sampleRTT = (double) (time2 - time1);
            if (sampleRTT < 1) {
                sampleRTT = 2;
            }

        }
        double totalSampleRTTanalysis = 0;
        double totalEstimatedRTTanalysis = 0;
        double totaldevRTTanalysis = 0;
        double totalRTTintervalanalysis = 0;

        for (int z = 0; z < n; z++) {
            totalSampleRTTanalysis += sampleRTTanalysis[z];
            totalEstimatedRTTanalysis += EstimatedRTTanalysis[z];
            totaldevRTTanalysis += devRTTanalysis[z];
            totalRTTintervalanalysis += RTTintervalanalysis[z];
        }

        System.out.println("\nAverage of sampleRTT is " + totalSampleRTTanalysis / n);
        System.out.println("Average of estimatedRTT is " + totalEstimatedRTTanalysis / n);
        System.out.println("Average of devRTT is " + totaldevRTTanalysis / n);
        System.out.println("Average of RTTinterval is " + totalRTTintervalanalysis / n);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("sampleRTTanalysis.txt"), "utf-8"))) {
            writer.write("sampleRTTanalysis");
            writer.newLine();
            writer.write(Arrays.toString(sampleRTTanalysis));
            writer.newLine();
            writer.newLine();
            writer.write("Average sampleRTTanalysis is " + totalSampleRTTanalysis / n);
            writer.newLine();
            writer.write("File size is " + this.fileData.length + " bytes");
            writer.newLine();
            writer.write("Loss probability is " + (1 - requiredprob) * 100 + "%");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("EstimatedRTTanalysis.txt"), "utf-8"))) {

            writer.write("EstimatedRTTanalysis");
            writer.newLine();
            writer.write(Arrays.toString(EstimatedRTTanalysis));
            writer.newLine();
            writer.newLine();
            writer.write("Average EstimatedRTTanalysis is " + totalEstimatedRTTanalysis / n);
            writer.newLine();
            writer.write("File size is " + this.fileData.length + " bytes");
            writer.newLine();
            writer.write("Loss probability is " + (1 - requiredprob) * 100 + "%");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("devRTTanalysis.txt"), "utf-8"))) {

            writer.write("devRTTanalysis");
            writer.newLine();
            writer.write(Arrays.toString(devRTTanalysis));
            writer.newLine();
            writer.newLine();
            writer.write("Average devRTTanalysis is " + totaldevRTTanalysis / n);
            writer.newLine();
            writer.write("File size is " + this.fileData.length + " bytes");
            writer.newLine();
            writer.write("Loss probability is " + (1 - requiredprob) * 100 + "%");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("RTTintervalanalysis.txt"), "utf-8"))) {

            writer.write("RTTintervalanalysis");
            writer.newLine();
            writer.write(Arrays.toString(RTTintervalanalysis));
            writer.newLine();
            writer.newLine();
            writer.write("Average RTTintervalanalysis is " + totalRTTintervalanalysis / n);
            writer.newLine();
            writer.write("File size is " + this.fileData.length + " bytes");
            writer.newLine();
            writer.write("Loss probability is " + (1 - requiredprob) * 100 + "%");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public ackPacket makeACKPacket(int ackNo) {

        ackPacket packet = new ackPacket(ackNo);
        return packet;
    }

    @Override
    public DatagramPacket getDatagramPacket(ackPacket packet, InetAddress addr, int portNumber) throws IOException, ClassNotFoundException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(packet.length);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        final byte[] data = baos.toByteArray();
        DatagramPacket dPacket = new DatagramPacket(data, data.length, addr, portNumber);
        return dPacket;

    }

    @Override
    public boolean validateCKSum(dataPacket packet) {
        int dataLength = packet.data.length;
        int totalSum = 0;
        for (int i = 0; i < dataLength / 4; i += 4) {
            totalSum = totalSum + packet.data[i] + packet.data[i + 1] * 10 + packet.data[i + 2] * 100 + packet.data[i + 3] * 1000;
        }
        totalSum += packet.length + packet.seqNo;
        if (packet.cksum == totalSum) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateCKSum(ackPacket packet) {
        int totalSum = 0;
        totalSum += packet.ackNo;
        totalSum += packet.length;

        if (packet.cksum == totalSum) {
            return true;
        }
        return false;
    }

    @Override
    public ackPacket getACKPacket(DatagramPacket packet) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream baos = new ByteArrayInputStream((packet.getData()));
        final ObjectInputStream oos = new ObjectInputStream(baos);

        ackPacket ackpacket = (ackPacket) oos.readObject();
        return ackpacket;
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

    public DatagramPacket getDatagramPacket(dataPacket packet, InetAddress source_IP, int source_port) throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(packet.length);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        oos.flush();
        oos.close();

        byte[] data = baos.toByteArray();

        DatagramPacket dPacket = new DatagramPacket(data, data.length, source_IP, source_port);
        return dPacket;

    }

}
