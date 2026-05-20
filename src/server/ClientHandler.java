package server;

import logging.SystemLogger;
import model.Ticket;
import protocol.Request;
import protocol.RequestType;
import protocol.Response;
import protocol.ResponseType;
import protocol.Role;
import service.TicketManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * Denne klassen håndterer én client.
 *
 * Hver client kjører i sin egen thread.
 *
 * ClientHandler endrer ikke tickets direkte.
 * Alle endringer skjer gjennom TicketManager.
 */
public class ClientHandler implements Runnable {

    // vi lager forbinelsen mellom clint og server
    private final Socket socket;

    // Manager har ansvar for å håndtere tickets
    private final TicketManager manager;

    // Logger som skriver hendelser til system.log
    private final SystemLogger logger = SystemLogger.getInstance();

    /*
     * Constructor
     *
     * Lagrer socket og manager når ClientHandler opprettes.
     */
    public ClientHandler(Socket socket, TicketManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    /*
     * Kjøres når threaden starter.
     *
     * 
     * -  server motta request fra client
     * -  og validere request
     * -  til slutt sende response tilbake til client
     */


    @Override
    public void run() {
        try (
                // Brukes for å sende objekter til client
                ObjectOutputStream output =
                        new ObjectOutputStream(socket.getOutputStream());


                // Brukes for å lese objekter fra client
                ObjectInputStream input =
                        new ObjectInputStream(socket.getInputStream())
        ) {

            /*
             * server venter for client
             * .
             */
            while (true) {

                // Leser objekt sendt fra client
                Object receivedObject = input.readObject();

                /*
                 *  her Sjekker at objektet faktisk er en Request.
                 */
                if (!(receivedObject instanceof Request)) {

                    logger.error(
                            "REQUEST_VALIDATION_ERROR",
                            "Object is not a Request"
                    );

                    output.writeObject(
                            new Response(
                                    ResponseType.ERROR,
                                    "Invalid request object",
                                    null
                            )
                    );

                    output.flush();
                    continue;
                }

                /*
                 * Siden vi vet at objektet er en Request,
                 * kan vi caste det trygt.
                 */
                Request request = (Request) receivedObject;

                // Behandler request og lager response
                Response response = handleRequest(request);

                // Sender response tilbake til client
                output.writeObject(response);

                // Sørger for at response sendes med en gang
                output.flush();
            }

        } catch (Exception e) {

            /*
             * Hvis client kobler fra eller en nettverksfeil skjer,
             * logges også årsaken til feilen.
             */
            logger.error(
                    "DISCONNECT",
                    "Client disconnected from "
                            + socket.getRemoteSocketAddress()
                            + " reason=" + e.getMessage()
            );
        }
    }

    /*
     * Validerer og utfører request.
     *
     * Basert på request-type bestemmer metoden om den skal:
     * - opprette ticket
     * - kansellere ticket
     * - hente neste ticket
     * - fullføre ticket
     */
    private Response handleRequest(Request request) {

        /*
         * her sjekker at request ikke er null.
         */
        if (request == null) {

            logger.error(
                    "REQUEST_VALIDATION_ERROR",
                    "request is null"
            );

            return new Response(
                    ResponseType.ERROR,
                    "Invalid request",
                    null
            );
        }

        // Henter request-typen
        RequestType type = request.getType();

        /*
         * Hvis type er null,
         * kan ikke switch fungere trygt.
         */
        if (type == null) {

            logger.error(
                    "REQUEST_VALIDATION_ERROR",
                    "request type is null"
            );

            return new Response(
                    ResponseType.ERROR,
                    "Invalid request type",
                    null
            );
        }

        // Henter rollen til client
        // siden registrar har ansvar for create og cancel
        //  og agent har ansvar for fetch og complete

        Role role = request.getRole();

        /*
         * Hvis role mangler,
         * kan ikke serveren sjekke tilgang.
         */
        if (role == null) {

            logger.error(
                    "ROLE_VALIDATION_ERROR",
                    "role is null for request type=" + type
            );

            return new Response(
                    ResponseType.ERROR,
                    "Missing role",
                    null
            );
        }

        /*
         * Bestemmer hvilken operasjon som skal utføres
         * basert på request type.
         */
        switch (type) {

            /*
             * Oppretter en ny ticket.
             *
             * Kun REGISTRAR har lov til dette.
             */
            case CREATE_TICKET:

                if (role != Role.REGISTRAR) {

                    logger.error(
                            "ROLE_VALIDATION_ERROR",
                            "CREATE_TICKET denied for role=" + role
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "Only REGISTRAR can create tickets",
                            null
                    );
                }

                Ticket created = manager.createTicket(
                        request.getDescription()
                );

                if (created == null) {

                    logger.error(
                            "CREATE",
                            "Failed to create ticket"
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "Unable to create ticket",
                            null
                    );
                }

                logger.info(
                        "CREATE",
                        "Ticket created by actor="
                                + request.getActorName()
                );

                return new Response(
                        ResponseType.SUCCESS,
                        "Ticket created",
                        created
                );

            /*
             * Kansellerer en ticket.
             *
             * Kun REGISTRAR har lov til dette.
             */
            case CANCEL_TICKET:

                if (role != Role.REGISTRAR) {

                    logger.error(
                            "ROLE_VALIDATION_ERROR",
                            "CANCEL_TICKET denied for role=" + role
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "Only REGISTRAR can cancel tickets",
                            null
                    );
                }

                boolean cancelled = manager.cancelTicket(
                        request.getTicketId()
                );

                if (cancelled) {

                    logger.info(
                            "CANCEL",
                            "Ticket cancelled id="
                                    + request.getTicketId()
                                    + " actor="
                                    + request.getActorName()
                    );

                    return new Response(
                            ResponseType.SUCCESS,
                            "Ticket cancelled",
                            null
                    );
                }

                logger.error(
                        "CANCEL",
                        "Unable to cancel ticket id="
                                + request.getTicketId()
                                + " actor="
                                + request.getActorName()
                );

                return new Response(
                        ResponseType.ERROR,
                        "Unable to cancel ticket",
                        null
                );

            /*
             * Henter neste tilgjengelige ticket.
             *
             * Kun AGENT har lov til dette.
             */
            case FETCH_NEXT_TICKET:

                if (role != Role.AGENT) {

                    logger.error(
                            "ROLE_VALIDATION_ERROR",
                            "FETCH_NEXT_TICKET denied for role=" + role
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "Only AGENT can fetch tickets",
                            null
                    );
                }

                Ticket assigned = manager.assignNextTicket(
                        request.getActorName()
                );

                if (assigned == null) {

                    logger.error(
                            "ASSIGN",
                            "No available tickets for actor="
                                    + request.getActorName()
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "No available tickets",
                            null
                    );
                }

                logger.info(
                        "ASSIGN",
                        "Ticket assigned to actor="
                                + request.getActorName()
                );

                return new Response(
                        ResponseType.SUCCESS,
                        "Ticket assigned",
                        assigned
                );

            /*
             * Fullfører en ticket.
             *
             * Kun AGENT har lov til dette.
             */
            case COMPLETE_TICKET:

                if (role != Role.AGENT) {

                    logger.error(
                            "ROLE_VALIDATION_ERROR",
                            "COMPLETE_TICKET denied for role=" + role
                    );

                    return new Response(
                            ResponseType.ERROR,
                            "Only AGENT can complete tickets",
                            null
                    );
                }

                boolean completed = manager.completeTicket(
                        request.getTicketId(),
                        request.getActorName()
                );

                if (completed) {

                    logger.info(
                            "COMPLETE",
                            "Ticket completed id="
                                    + request.getTicketId()
                                    + " actor="
                                    + request.getActorName()
                    );

                    return new Response(
                            ResponseType.SUCCESS,
                            "Ticket completed",
                            null
                    );
                }

                logger.error(
                        "COMPLETE",
                        "Unable to complete ticket id="
                                + request.getTicketId()
                                + " actor="
                                + request.getActorName()
                );

                return new Response(
                        ResponseType.ERROR,
                        "Unable to complete ticket",
                        null
                );

            /*
             * Hvis request type er ukjent.
             */
            default:

                logger.error(
                        "REQUEST_VALIDATION_ERROR",
                        "Unknown request type=" + type
                );

                return new Response(
                        ResponseType.ERROR,
                        "Unknown request type",
                        null
                );
        }
    }
}