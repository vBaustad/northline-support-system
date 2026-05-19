package server;

import model.Ticket;
import protocol.Request;
import protocol.Response;
import protocol.ResponseType;
import protocol.RequestType;
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
                Request request = (Request) input.readObject();
                Response response = handleRequest(request);

                output.writeObject(response);
                output.flush();
            }
        } catch (Exception e) {
            System.out.println("Client disconnected.");
        }
    }

    private Response handleRequest(Request request) {
        if (request == null) {
            return new Response(
                    ResponseType.ERROR,
                    "Invalid request",
                    null
            );
        }

        RequestType type = request.getType();

        switch (type) {
            case CREATE_TICKET:
                Ticket created = manager.createTicket(request.getDescription());

                return new Response(
                        ResponseType.SUCCESS,
                        "Ticket created",
                        created
                );

            case CANCEL_TICKET:
                boolean cancelled = manager.cancelTicket(request.getTicketId());

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
                Ticket assigned = manager.assignNextTicket(request.getActorName());

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
                return new Response(
                        ResponseType.ERROR,
                        "Unknown request type",
                        null
                );
        }
    }
}