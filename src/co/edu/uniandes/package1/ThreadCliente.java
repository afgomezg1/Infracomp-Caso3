package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadCliente extends Thread {
    private final String NUMERO_DOCUMENTO;
    private final String TIPO_DOCUMENTO;
    private final String ID_PROD;
    private final int ID;

    public ThreadCliente(String numeroDocumento, String tipoDocumento, String idProd, int id) {
        this.NUMERO_DOCUMENTO = numeroDocumento;
        this.TIPO_DOCUMENTO = tipoDocumento;
        this.ID_PROD = idProd;
        this.ID = id;
    }

    @Override
    public void run() {

		Socket socket = null;
        BufferedReader pIn = null;
		PrintWriter pOut = null;

		System.out.println("Inica cliente delegado " + ID);

		try {
			socket = new Socket(Cliente.SERVIDOR, Cliente.PUERTO);
            pIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pOut = new PrintWriter(socket.getOutputStream(), true);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        try {
            ProtocoloCliente.procesar(stdIn, pIn, pOut, NUMERO_DOCUMENTO, TIPO_DOCUMENTO, ID_PROD, ID);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            stdIn.close();
            pIn.close();
            pOut.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
