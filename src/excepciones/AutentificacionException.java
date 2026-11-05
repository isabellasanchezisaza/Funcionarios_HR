/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package excepciones;

/**
 *
 * @author Isabella
 */
public class AutentificacionException extends Exception {
    
    public AutentificacionException(String mensaje){
        super(mensaje);
    }
    
    public AutentificacionException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
    
}
