package protocol;

import model.Ticket;
import java.io.Serializable;
import java.util.List;


// Response-objekter sendes fra server tilbake til klient
// etter at en Request er behandlet.
//
// Serializable gjør at objektene kan sendes
// gjennom ObjectOutputStream over TCP.
public class Response implements Serializable{

    // Resultatet av operasjonen
    // F.eks SUCCESS eller ERROR
    private final ResponseType type;

    // Menneskelesbar melding som forklarer resultatet
    // Kan brukes til feilmeldinger eller statusmeldinger
    private final String message;

    // En enkelt ticket returnert fra serveren
    // Brukes for eksempel ved FETCH_NEXT_TICKET
    private final Ticket ticket;

    // Liste med tickets
    // Kan brukes ved fremtidige utvidelser som
    // "GET_ALL_TICKETS"
    private final List<Ticket> tickets;

    public Response(ResponseType type, String message, Ticket ticket, List<Ticket> tickets){
        this.type = type;
        this.message = message;
        this.ticket = ticket;
        this.tickets = tickets;
    }

    public ResponseType getType(){
        return type;
    }

    public String getMessage(){
        return message;
    }

    public Ticket getTicket(){
        return ticket;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
}
