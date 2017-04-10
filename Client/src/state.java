/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author mahmoud
 */
public abstract class state{
    
    public String name;
    public state(String name)
    {
        this.name = name;
    }
    
    public abstract void action ();
    
    
}
