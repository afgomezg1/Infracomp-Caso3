package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ProtocoloServidor 
{
    private static final String ALGORITMO_ASIMETRICO = "RSA";
    private static final String ALGORITMO_SIMETRICO = "AES/ECB/PKCS5Padding";
    private static Random randomNumbers = new Random();
    public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, HashMap<String, List<String>> hashMap, int id) throws IOException    
    {
        try 
        {
            FileInputStream archivoPrivada = new FileInputStream("data/privada");
            ObjectInputStream ois = new ObjectInputStream(archivoPrivada);
            SecretKey llavePrivada = (SecretKey) ois.readObject();
            ois.close();

            pIn.readLine();

            pOut.println("ACK");

            byte[] Reto = pIn.readLine().getBytes();
            String RetoString = new String(Reto);

            int numeroXD = randomNumbers.nextInt(0,10);
            if (numeroXD < 9) 
            {
                String cifradoAutenticacion = ManejadorSeguridad.cifrar(llavePrivada, ALGORITMO_ASIMETRICO, RetoString);     
                pOut.println(cifradoAutenticacion);
            }
            else
            {
                pOut.println("XD");
                System.exit(-1);
            }

            byte[] llaveSimetricaCifrada = pIn.readLine().getBytes();

            String llaveS = ManejadorSeguridad.descifrar(llavePrivada, ALGORITMO_ASIMETRICO, llaveSimetricaCifrada);

            byte[] keyBytes = llaveS.getBytes(StandardCharsets.UTF_8);

            SecretKey llaveSimetrica = new SecretKeySpec(keyBytes, ALGORITMO_SIMETRICO);

            pOut.println("ACK");

            byte[] infoCliente = pIn.readLine().getBytes();
            String infoClienteString = new String(infoCliente);

            String[] info = infoClienteString.split(";");
            String idDoc = info[0];
            String infoDoc = info[1];
            List<String> list = hashMap.get(idDoc);
            if (list != null)
            {
                if(list.get(0) == infoDoc)
                {
                    pOut.println("ACK");
                }
                else
                {
                    pOut.println("ERROR");
                    System.exit(-1);
                }
            }
            else
            {
                pOut.println("ERROR");
                System.exit(-1);
            }

            byte[] idProd = pIn.readLine().getBytes();
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
            byte[] Hmac = ManejadorSeguridad.getDigest(kBytes);
            String HMACString = new String(Hmac);

            pOut.println(ManejadorSeguridad.cifrar(llaveSimetrica, ALGORITMO_SIMETRICO, HMACString));
            
            pIn.readLine();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
}
