/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author mahmoud
 */
import java.io.IOException;
import java.net.*;
public interface senderSite {


    public double[] calcRTT(double sampleRTT, double EstimatedRTT, double devRTT);

    public boolean calcLostProb(double requiredProb);
    
    public dataPacket getOriginalPacket(DatagramPacket packet)throws IOException, ClassNotFoundException ;
    

}
