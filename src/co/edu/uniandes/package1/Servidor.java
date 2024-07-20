package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Servidor
{
    public static final int PUERTO = 3400;
    public static final String SERVIDOR = "localhost";
    private static final String dirArchivo = "data/datosServidor.txt";
    private static int count;
    private static HashMap<String, List<String>> hashMap;

    public static void main(String[] args) throws IOException
    {
        System.out.println("Iniciando Servidor...");

        Scanner in = new Scanner(System.in);

        boolean menu = true;
        while (menu) {
            hashMap = leerArchivo(dirArchivo);
            System.out.println("Ingrese si desea correr el Servidor en modo iterativo (1) o con delegados (2): ");
            int modo = in.nextInt();

            switch (modo) {
                case 1:
                    correrServidorIterativo();
                    menu = false;
                    break;
                case 2:
                    System.out.println("Ingrese la cantidad de Servidores Delegados que quiere ejecutar (4, 16 o 32). \n" + //
                                                "RECUERDE QUE ESTE NUMERO DEBE COINCIDIR CON EL NUMERO DE CLIENTES DELEGADOS: ");
                    int cantidadServidores = in.nextInt();
                    correrServidorDelegados(cantidadServidores);
                    menu = false;
                    break;

                default:
                    System.out.println("Opción invalida");
                    break;
            }
        }

        in.close();
    }

    private static HashMap<String, List<String>> leerArchivo(String csvFile)
    {
        String line;
        String csvSplitBy = ",";

        HashMap<String, List<String>> hashMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile)))
        {
            while ((line = br.readLine()) != null)
            {
                String[] elements = line.split(csvSplitBy);
                String id = elements[0];

                List<String> values = new ArrayList<>();
                for (int i = 1; i < elements.length; i++)
                {
                    values.add(elements[i]);
                }

                hashMap.put(id, values);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return hashMap;
    }


    private static void correrServidorDelegados(int cantidadServidores) throws IOException
    {
        ServerSocket ss = null;

        try
        {
            ss = new ServerSocket(PUERTO);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        count = 1;
        while (cantidadServidores > 0)
        {
            Socket socket = ss.accept();
            ThreadServidor thread = new ThreadServidor (socket, count, hashMap);
            cantidadServidores--;
            count++;

            thread.start();
        }
        ss.close();
    }


    private static void correrServidorIterativo() throws IOException
    {
        ServerSocket ss = null;
        BufferedReader pIn = null;
		PrintWriter pOut = null;

        System.out.println("Inicia Servidor iterativo");
        try
        {
            ss = new ServerSocket(PUERTO);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

      
        Socket socket = ss.accept();

        try
        {
            pIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pOut = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }

        HashMap<String, List<String>> hashMap = leerArchivo(dirArchivo);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        for (int i = 0; i < 32; i++)
        {
            ProtocoloServidor.procesar(stdIn, pIn, pOut, hashMap, i);
        }

        try
        {
            stdIn.close();
            pIn.close();
            pOut.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        
    }
}
