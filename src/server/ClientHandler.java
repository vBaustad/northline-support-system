package server;

import model.Ticket;
import protocol.Request;
import protocol.RequestType;
import protocol.Response;
import protocol.ResponseType;
import service.TicketManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * ClientHandler skal ikke endre tickets direkte.
 * Det er kun TicketManager som eier shared state.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TicketManager manager;

    public ClientHandler(Socket socket, TicketManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream())
        ) {

            while (true) {

                // Leser objekt fra klient
                Object receivedObject = input.readObject();

                // Beskytter protokollen mot ugyldige objekter
                if (!(receivedObject instanceof Request)) {

                    System.out.println("Protocol error: Object is not a Request");

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

                Request request = (Request) receivedObject;

                Response response = handleRequest(request);

                output.writeObject(response);
                output.flush();
            }

        } catch (Exception e) {

            // Logger tilkoblingsfeil / disconnect
            System.out.println("Client disconnected.");
        }
    }

    private Response handleRequest(Request request) {

        // Validerer request før behandling
        if (request == null) {

            System.out.println("Protocol error: request is null");

            return new Response(
                    ResponseType.ERROR,
                    "Invalid request",
                    null
            );
        }

        RequestType type = request.getType();

        // Hindrer NullPointerException i switch(type)
        if (type == null) {

            System.out.println("Protocol error: request type is null");

            return new Response(
                    ResponseType.ERROR,
                    "Invalid request type",
                    null
            );
        }

        switch (type) {

            case CREATE_TICKET:

                // Oppretter ticket via TicketManager
                Ticket created = manager.createTicket(
                        request.getDescription()
                );

                // Håndterer mulig null-retur
                if (created == null) {

                    System.out.println("Failed to create ticket");

                    return new Response(
                            ResponseType.ERROR,
                            "Unable to create ticket",
                            null
                    );
                }

                return new Response(
                        ResponseType.SUCCESS,
                        "Ticket created",
                        created
                );

            case CANCEL_TICKET:

                boolean cancelled = manager.cancelTicket(
                        request.getTicketId()
                );

                if (cancelled) {

                    return new Response(
                            ResponseType.SUCCESS,
                            "Ticket cancelled",
                            null
                    );
                }

                return new Response(
                        ResponseType.ERROR,
                        "Unable to cancel ticket",
                        null
                );

            case FETCH_NEXT_TICKET:

                Ticket assigned = manager.assignNextTicket(
                        request.getActorName()
                );

                if (assigned == null) {

                    return new Response(
                            ResponseType.ERROR,
                            "No available tickets",
                            null
                    );
                }

                return new Response(
                        ResponseType.SUCCESS,
                        "Ticket assigned",
                        assigned
                );

            case COMPLETE_TICKET:

                boolean completed = manager.completeTicket(
                        request.getTicketId(),
                        request.getActorName()
                );

                if (completed) {

                    return new Response(
                            ResponseType.SUCCESS,
                            "Ticket completed",
                            null
                    );
                }

                return new Response(
                        ResponseType.ERROR,
                        "Unable to complete ticket",
                        null
                );

            default:

                // Logger ukjent request-type
                System.out.println("Protocol error: unknown request type");

                return new Response(
                        ResponseType.ERROR,
                        "Unknown request type",
                        null
                );
        }
    }
}