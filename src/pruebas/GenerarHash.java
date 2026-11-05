/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import java.util.Scanner;
import seguridad.PasswordUtil;

/**
 *
 * @author Isabella
 */
public class GenerarHash {
    public static void main(String[] args) {
        /*
        System.out.print("Escriba la clave del administrador: ");
        char[] clave = new Scanner(System.in).nextLine().toCharArray();

        String hash = PasswordUtil.hashear(clave);
        System.out.println("Hash: " + hash);
        System.out.println("¿Verifica la clave correcta? " + PasswordUtil.verificar(clave, hash));
        System.out.println("¿Verifica una clave incorrecta? "
                + PasswordUtil.verificar("otraClave".toCharArray(), hash));
 
    
        System.out.print("Escriba la clave del rrhh: ");
        char[] clave = new Scanner(System.in).nextLine().toCharArray();

        String hash = PasswordUtil.hashear(clave);
        System.out.println("Hash: " + hash);
        System.out.println("¿Verifica la clave correcta? " + PasswordUtil.verificar(clave, hash));
        System.out.println("¿Verifica una clave incorrecta? "
                + PasswordUtil.verificar("otraClave".toCharArray(), hash));
        
        */
        System.out.print("Escriba la clave del consultor: ");
        char[] clave = new Scanner(System.in).nextLine().toCharArray();

        String hash = PasswordUtil.hashear(clave);
        System.out.println("Hash: " + hash);
        System.out.println("¿Verifica la clave correcta? " + PasswordUtil.verificar(clave, hash));
        System.out.println("¿Verifica una clave incorrecta? "
                + PasswordUtil.verificar("otraClave".toCharArray(), hash));
    }
}
