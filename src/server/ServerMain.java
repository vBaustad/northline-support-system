package server;

import logging.SystemLogger;
import service.TicketManager;

import java.net.ServerSocket;
import java.net.Socket;

/*
 * Main-klassen for serveren.
 *
 * Denne klassen:
 * - starter serveren
 * - venter på klienter
 * - oppretter ClientHandler for hver klient
 * - starter hver klient i egen thread
 */
public class ServerMain {

    // Porten serveren kjører på
    private static final int PORT = 5000;

    // Global logger
    private static final SystemLogger logger =
            SystemLogger.getInstance();

    public static void main(String[] args) {

        /*
         * Shared state owner
         *
         * Kun TicketManager får lov
         * til å endre ticket-state.
         *
         * Alle klienter deler samme manager.
         */
        TicketManager manager =
                new TicketManager();

        try (
                // Oppretter server socket på valgt port
                ServerSocket serverSocket =
                        new ServerSocket(PORT)
        ) {

            // Logger at serveren har startet
            logger.info(
                    "SERVER_START",
                    "Server started on port " + PORT
            );

            /*
             * Serveren kjører hele tiden
             * og aksepterer nye klienter.
             */
            while (true) {

                /*
                 * Venter til en client kobler til.
                 */
                Socket clientSocket =
                        serverSocket.accept();

                // Logger ny tilkobling
                logger.info(
                        "CLIENT_CONNECT",
                        "Client connected from "
                                + clientSocket.getInetAddress()
                );

                /*
                 * Oppretter én ClientHandler
                 * for hver klient.
                 */
                ClientHandler handler =
                        new ClientHandler(
                                clientSocket,
                                manager
                        );

                /*
                 * Hver klient kjører
                 * i sin egen thread.
                 *
                 * Dette gjør at flere klienter
                 * kan bruke serveren samtidig.
                 */
                Thread clientThread =
                        new Thread(handler);

                // Starter klient-thread
                clientThread.start();
            }

        } catch (Exception e) {

            /*
             * Logger serverfeil
             * med feilmelding.
             */
            logger.error(
                    "SERVER_ERROR",
                    "Server error: " + e.getMessage()
            );
        }
    }
}