package co.edu.uniandes.package1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static final int PUERTO = 3400;
    public static final String SERVIDOR = "localhost";
    private static final String dirArchivo = "data/datosCliente.txt";
    public static void main(String[] args) throws IOException{
        System.out.println("Iniciando cliente...");

        Scanner in = new Scanner(System.in);

        boolean menu = true;
        while (menu) {
            System.out.println("Ingrese si desea correr el cliente en modo iterativo (1) o con delegados (2): ");
            int modo = in.nextInt();

            switch (modo) {
                case 1:
                    correrClienteIterativo();
                    menu = false;
                    break;
                case 2:
                    System.out.println("Ingrese la cantidad de clientes delegados que quiere ejecutar: ");
                    int cantidadClientes = in.nextInt();
                    correrClienteDelegados(cantidadClientes);
                    menu = false;
                    break;

                default:
                    System.out.println("Opción invalida");
                    break;
            }
        }

        in.close();
    }

    private static void correrClienteIterativo() {
		Socket socket = null;
        BufferedReader pIn = null;
		PrintWriter pOut = null;

		System.out.println("Inicia cliente iterativo");

		try {
			socket = new Socket(SERVIDOR, PUERTO);
            pIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pOut = new PrintWriter(socket.getOutputStream(), true);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try (BufferedReader br = new BufferedReader(new FileReader(dirArchivo))) {
            String[] datos = null;
            for (int i = 0; i < 32; i++) {
                datos = br.readLine().split(",");
                ProtocoloCliente.procesar(stdIn, pIn, pOut, datos[0], datos[1], datos[2], 0);
            }
            br.close();
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

    private static void correrClienteDelegados(int cantidadClientes) {
        ThreadCliente[] clientesDelegados = new ThreadCliente[cantidadClientes];
        try (BufferedReader br = new BufferedReader(new FileReader(dirArchivo))) {
            String line;
            int lineaActual = 0;
            String[] datos = null;
            while ((line = br.readLine()) != null && lineaActual < cantidadClientes) {
                datos = line.split(",");
                clientesDelegados[lineaActual] = new ThreadCliente(datos[0], datos[1], datos[2], lineaActual+1);
                lineaActual++;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (int i = 0; i < cantidadClientes; i++) {
            clientesDelegados[i].start();
        }
    }
}
