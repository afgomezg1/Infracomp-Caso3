package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ThreadServidor extends Thread {
    private Socket sktCliente;
    private int id;
    private HashMap<String, List<String>> hashMap;

    public ThreadServidor(Socket sktCliente, int id, HashMap<String, List<String>> hashMap) 
    {
        this.sktCliente = sktCliente;
        this.id = id;
        this.hashMap = hashMap;
    }

    @Override
    public void run() 
    {
        System.out.println("Inicio de un nuevo thread: " + id);

        BufferedReader pIn = null;
		PrintWriter pOut = null;

        try 
        {
            pIn = new BufferedReader(new InputStreamReader(sktCliente.getInputStream()));
            pOut = new PrintWriter(sktCliente.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            ProtocoloServidor.procesar(stdIn, pIn, pOut, this.hashMap, this.id);
            pIn.close();
            pOut.close();
            stdIn.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
