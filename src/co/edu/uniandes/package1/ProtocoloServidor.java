package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ProtocoloServidor
{
    private static final String ALGORITMO_ASIMETRICO = "RSA";
    private static final String ALGORITMO_SIMETRICO = "AES/ECB/PKCS5Padding";
    public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, HashMap<String, List<String>> hashMap, int id) throws IOException
    {
        try
        {
            FileInputStream archivoPrivada = new FileInputStream("data/privada");
            ObjectInputStream ois = new ObjectInputStream(archivoPrivada);
            PrivateKey llavePrivada = (PrivateKey) ois.readObject();
            ois.close();

            pIn.readLine();

            pOut.println("ACK");

            String reto = pIn.readLine();

            long timepoInicialAsimetrico = System.nanoTime();
            String cifradoAutenticacion = ManejadorSeguridad.cifrar(llavePrivada, ALGORITMO_ASIMETRICO, reto);
            long tiempoFinalAsimetrico = System.nanoTime();

            pOut.println(cifradoAutenticacion);

            String llaveS = ManejadorSeguridad.descifrar(llavePrivada, ALGORITMO_ASIMETRICO, pIn.readLine());

            byte[] decodedKey = Base64.getDecoder().decode(llaveS);

            SecretKey llaveSimetrica = new SecretKeySpec(decodedKey, "AES");

            long timepoInicialSimetrico = System.nanoTime();
            ManejadorSeguridad.cifrar(llaveSimetrica, ALGORITMO_SIMETRICO, reto);
            long tiempoFinalSimetrico = System.nanoTime();

            pOut.println("ACK");

            String infoClienteString = ManejadorSeguridad.descifrar(llavePrivada, ALGORITMO_ASIMETRICO, pIn.readLine());
            String[] info = infoClienteString.split(";");
            String idDoc = info[0];
            String infoDoc = info[1];
            List<String> list = hashMap.get(idDoc);
            if (list != null)
            {
                if(list.get(0).equals(infoDoc))
                {
                    pOut.println("ACK");
                }
                else
                {
                    pOut.println("ERROR");
                    System.out.println("llega");
                    System.exit(-1);
                }
            }
            else
            {
                pOut.println("ERROR");
                System.exit(-1);
            }

            String idProd = pIn.readLine();
            String idProdString = ManejadorSeguridad.descifrar(llaveSimetrica, ALGORITMO_SIMETRICO, idProd);

            boolean found = false;

            String estado = null;

            for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
                List<String> values = entry.getValue();
                if (values.contains(idProdString))
                {
                    estado = values.get(2);
                    pOut.println(ManejadorSeguridad.cifrar(llaveSimetrica, ALGORITMO_SIMETRICO, estado));
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("Producto no encontrado en ninguna lista.");
                System.exit(-1);
            }

            pIn.readLine();

            byte[] kBytes = estado.getBytes(StandardCharsets.UTF_8);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(llaveSimetrica);

            String HMACString = Base64.getEncoder().encodeToString(kBytes);
            pOut.println(HMACString);

            pIn.readLine();

            System.out.println("Consulta " + id + "\nTiempo asimetrico: " + (tiempoFinalAsimetrico - timepoInicialAsimetrico) + "\nTiempo simetrico: " + (tiempoFinalSimetrico - timepoInicialSimetrico) + "\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
