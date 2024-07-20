package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class ProtocoloCliente {
    private static final String ALGORITMO_ASIMETRICO = "RSA";
    private static final String ALGORITMO_SIMETRICO = "AES/ECB/PKCS5Padding";

    public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, String numeroDocumento, String tipoDocumento, String idProd, int id) throws IOException{
        try {
            FileInputStream archivoPublica = new FileInputStream("data/publica");
            ObjectInputStream ois = new ObjectInputStream(archivoPublica);
            PublicKey llavePublica = (PublicKey) ois.readObject();
            ois.close();

            pOut.println("INICIO");

            pIn.readLine();

            Random random = new Random();

            int longitudReto = random.nextBoolean() ? 24 : 32;//TODO: Meter a generarReto

            String reto = generarReto(longitudReto);

            pOut.println(reto);

            String respuestaReto = pIn.readLine();

            if (!ManejadorSeguridad.descifrar(llavePublica, ALGORITMO_ASIMETRICO, respuestaReto).equals(reto)) {
                System.out.println("Error en la transmision, intente de nuevo");
                System.exit(-1);
            }

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey llaveSimetrica = keyGen.generateKey();
            pOut.println(ManejadorSeguridad.cifrar(llavePublica, ALGORITMO_ASIMETRICO, Base64.getEncoder().encodeToString(llaveSimetrica.getEncoded())));

            pIn.readLine();

            pOut.println(ManejadorSeguridad.cifrar(llavePublica, ALGORITMO_ASIMETRICO, numeroDocumento+";"+tipoDocumento));

            if (pIn.readLine().equals("ERROR")) {
                System.out.println("Error en la consulta");
                System.exit(-1);
            }

            pOut.println(ManejadorSeguridad.cifrar(llaveSimetrica, ALGORITMO_SIMETRICO, idProd));

            String respuestaEstado = ManejadorSeguridad.descifrar(llaveSimetrica, ALGORITMO_SIMETRICO, pIn.readLine());

            pOut.println("ACK");


            byte[] kBytes = respuestaEstado.getBytes(StandardCharsets.UTF_8);
            byte[] digest = ManejadorSeguridad.getDigest(kBytes);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(llaveSimetrica);

            byte[] hmacBytes = mac.doFinal(digest);

            String HMACString = Base64.getEncoder().encodeToString(hmacBytes);

            if (pIn.readLine().equals(HMACString)) {
                switch (id) {
                    case 0:
                        System.out.println("========== Respuesta al Cliente" + " ==========");
                        System.out.println("Estado del producto: " + respuestaEstado);
                        System.out.println();
                        break;

                    default:
                        System.out.println("========== Respuesta al Cliente " + id + " ==========");
                        System.out.println("Estado del producto: " + respuestaEstado);
                        System.out.println();
                        break;
                }
                pOut.println("TERMINAR");
            } else {
                System.out.println("Error en la transmision, intente de nuevo");
                System.exit(-1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static String generarReto(int longitudReto) {
        Random random = new Random();
        StringBuilder reto = new StringBuilder();

        for (int i = 0; i < longitudReto; i++) {
            int digito = random.nextInt(10);
            reto.append(digito);
        }

        return reto.toString();
    }

}
