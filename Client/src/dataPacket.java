/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author mahmoud
 */
public class dataPacket extends packet{
    
    public byte data[];
    public  int seqNo;


    public dataPacket( byte[] data , int seqNo ) {
        this.data = data;
        this.seqNo = seqNo;
        calcCKSum();
        calcLength();
        
    }
    @Override
    public void calcCKSum() {
        this.cksum = 0;
        for(int i = 0; i < data.length/4; i+=4) {
            this.cksum = this.cksum + this.data[i] + this.data[i+1]*10 + this.data[i+2]*100 + this.data[i+3]*1000;
        }
        this.cksum += this.length + this.seqNo;
        
    }

    @Override
    public void calcLength() {
        length = 3 * Integer.SIZE / 8 + this.data.length;
    }
    
}
