package server;

import service.TicketManager;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 5000;

    public static void main(String[] args) {

        /*
         * Shared state owner
         *
         * Kun TicketManager får lov
         * til å endre ticket-state.
         */
        TicketManager manager =
                new TicketManager();

        try (ServerSocket serverSocket =
                     new ServerSocket(PORT)) {

            System.out.println(
                    "Server started on port "
                    + PORT
            );

            /*
             * Serveren kjører bestandig 
             * og aksepterer nye klienter.
             */
            while (true) {

                /*
                 * Venter på klient til å koble
                 */
                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "Client connected: "
                        + clientSocket.getInetAddress()
                );

                /*
                 * Én ClientHandler per klient
                 */
                ClientHandler handler =
                        new ClientHandler(
                                clientSocket,
                                manager
                        );

                /*
                 * Hver klient kjører
                 * i egen tråd
                 */
                Thread clientThread =
                        new Thread(handler);

                clientThread.start();
            }

        } catch (Exception e) {

            System.out.println(
                    "Server error: "
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}