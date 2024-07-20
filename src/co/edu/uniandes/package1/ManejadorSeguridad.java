package co.edu.uniandes.package1;

import java.security.Key;
import java.security.MessageDigest;
import javax.crypto.Cipher;


public class ManejadorSeguridad {
    public static String cifrar(Key llave, String algoritmo, String texto) {
        byte[] textoCifrado;

        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            byte[] textoClaro = texto.getBytes();

            cifrador.init(Cipher.ENCRYPT_MODE, llave);
            textoCifrado = cifrador.doFinal(textoClaro);

            return new String(textoCifrado);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String descifrar(Key llave, String algoritmo, byte[] textoCifrado) {
        byte[] textoClaro;

        try {
            Cipher cifrador = Cipher.getInstance(algoritmo);
            cifrador.init(Cipher.DECRYPT_MODE, llave);
            textoClaro = cifrador.doFinal(textoCifrado);

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

