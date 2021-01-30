package it.chat.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static Socket clientSocket;
    private static class Listener implements Runnable {
        private static final int PORT = 5000;
        private BufferedReader in;

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String read;
                //creazione ciclo infinito
                for(;;) {
                    read = in.readLine();
                    if (read != null && !(read.isEmpty()))
                        System.out.println(read);
                }
            } catch (IOException e) {
                return;
            }
        }
        private static class Writer implements Runnable {
            private PrintWriter out;
            @Override
            public void run() {
                Scanner sc = new Scanner(System.in);
                try {
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    for(;;) {
                        if (sc.hasNext()) out.println(sc.nextLine());
                    }
                } catch (IOException e) {
                    sc.close();
                    return;
                }
            }
        }

        public static void main(String[] args) {
            String ipAddress = "localhost";
            try {
                // creazione connessione socket se il socket del server esiste
                clientSocket = new Socket(ipAddress, PORT);
            } catch (Exception e) {
                // genera un errore se non è presente il socket del server
                e.printStackTrace();
            }
            new Thread(new Writer()).start();
            new Thread(new Listener()).start();
        }

    }
}
