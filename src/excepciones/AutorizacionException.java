/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package excepciones;

/**
 *
 * @author Isabella
 */
public class AutorizacionException extends Exception{
    
    public AutorizacionException(String mensaje){
        super(mensaje);
    }
    
    public AutorizacionException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
    
}
