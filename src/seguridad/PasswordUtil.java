/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Isabella
 */
public class PasswordUtil {
    
    private static final String ALGORITMO ="PBKDF2WithHmacSHA256"; 
    private static final int ITERACIONES = 600_000; //cuantas veces se repite el cálculo, recomendado según la guía OWASP
    private static final int LONGITUD_SAL = 16; //bytes
    private static final int LONGITUD_HASH = 256; //bits
    private static final SecureRandom RANDOM = new SecureRandom(); // generador de números aleatorios para criptografía.
            
    public static String hashear(char[] clave){
        byte[] sal = new byte[LONGITUD_SAL];
        RANDOM.nextBytes(sal); //crea un arreglo con valores aleatorios (nueva sal)
        byte[] hash = derivar(clave, sal, ITERACIONES); // calcula el hash de la clave con la sal creada
        return "pbkdf2$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash); // Base64 convierte los bytes del hash y la sal en letras, numeros y símbolos. Texto de 4 partes separas por $
    }
    
   public static boolean verificar(char[] clave, String hashGuardado){
       if (clave == null || hashGuardado == null){
           return false; //si falta algo, no se compara y falla.
       }
       String[] partes = hashGuardado.split("\\$");
       if (partes.length != 4 || !"pbkdf2".equals(partes[0])){
           return false; // separa el texto guardado en sus 4 partes, si no, delvuelve false.
       }
       try {
           int iteraciones = Integer.parseInt(partes[1]);
           byte[] sal = Base64.getDecoder().decode(partes[2]);
           byte[] esperado = Base64.getDecoder().decode(partes[3]); // recupera los datos pero ya convertidos de nuevo a bytes
           byte[] calculado = derivar(clave, sal, iteraciones); 
           return MessageDigest.isEqual(esperado, calculado); // la clave e´tá aquí, vuelve a calcular y compara. Clave correcta = salen identicos.
       } catch (IllegalArgumentException e){
           return false; // de lo contrario se lanza una excepcion
       }
   }
   
   private static byte[] derivar(char[] clave, byte[] sal, int iteraciones){
       PBEKeySpec spec = new PBEKeySpec(clave, sal, iteraciones, LONGITUD_HASH); // la receta para PBKDF2
       try {
           return SecretKeyFactory.getInstance(ALGORITMO)
                   .generateSecret(spec).getEncoded(); // aquí se entrega la receta y lo devuelve en bytes (este es el hash)
       } catch (NoSuchAlgorithmException | InvalidKeySpecException e){
           throw new IllegalStateException("No se pudo calcular el hash de la constraseña.", e); // no obliga a llamar a manejarla y hashear y verficar queda más simple
       } finally {
           spec.clearPassword(); //siempre se ejecuta, borra la memoria de la copia de la contraseña
       }
   }
}
