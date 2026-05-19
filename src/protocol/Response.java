package protocol;

import model.Ticket;
import java.io.Serializable;
import java.util.List;

public class Response implements Serializable{
    private final ResponseType type;
    private final String message;
    private final Ticket ticket;
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
