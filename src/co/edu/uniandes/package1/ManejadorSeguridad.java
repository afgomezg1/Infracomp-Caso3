package co.edu.uniandes.package1;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;


public class ManejadorSeguridad {
    public static String cifrar(Key llave, String algoritmo, String texto) {
        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            byte[] textoClaro = texto.getBytes();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            byte[] textoCifrado = cifrador.doFinal(textoClaro);

            return Base64.getEncoder().encodeToString(textoCifrado);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String descifrar(Key llave, String algoritmo, String textoCifrado) {
        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            cifrador.init(Cipher.DECRYPT_MODE, llave);
            byte[] textoCifradoBytes = Base64.getDecoder().decode(textoCifrado);
            byte[] textoClaro = cifrador.doFinal(textoCifradoBytes);

            return new String(textoClaro);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getDigest(byte[] datos) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(datos);
            return digest.digest();
        } catch (Exception e) {
            return null;
        }
    }
}

