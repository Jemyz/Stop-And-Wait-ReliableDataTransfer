/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author mahmoud
 */
public class ackPacket extends packet{

    public int ackNo;
    public ackPacket( int ackNo) {
        this.ackNo = ackNo;
        calcCKSum();
        calcLength();
    }

    @Override
    public void calcCKSum() {
        this.cksum = 0;
        this.cksum += this.ackNo;
        this.cksum += this.length;
    }

    @Override
    public void calcLength() {
        length = 3 * Integer.SIZE / 8;
    }
    
    
    
}
