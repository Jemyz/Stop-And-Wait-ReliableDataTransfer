/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;
import java.net.*;

/**
 *
 * @author mahmoud
 */
public abstract class packet implements Serializable{
    
    public int cksum;
    public int length;
    
    public packet(){
        
       
    }
    
   
    public abstract void calcCKSum();
    public abstract void calcLength();

    
    
}
