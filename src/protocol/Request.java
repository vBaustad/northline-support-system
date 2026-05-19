package protocol;

import java.io.Serializable;

/*
Request field usage:

- CREATE_TICKET:
  requires role=REGISTRAR, actorName, description

- CANCEL_TICKET:
  requires role=REGISTRAR, actorName, ticketId

- FETCH_NEXT_TICKET:
  requires role=AGENT, actorName

- COMPLETE_TICKET:
  requires role=AGENT, actorName, ticketId
*/

// Serializable gjør at Request-objekter kan sendes
// mellom klient og server over TCP/ObjectStreams
public class Request implements Serializable{

    // Hvilken operasjon klienten ønsker å utføre
    private final RequestType type;

    // Rollen til klienten som sender forespørselen
    // Brukes for å validere tillatte operasjoner
    private final Role role;

    // ID til ticketen operasjonen gjelder
    // Brukes for eksempel ved cancel eller complete
    private final int ticketId;

    // Navn på agent eller registrator som utfører handlingen
    private final String actorName;

    // Beskrivelse av problemet
    // Brukes kun ved CREATE_TICKET
    private final String description;
    
    public Request(RequestType type, Role role, int ticketId, String actorName, String description){
        this.type = type;
        this.role = role;
        this.ticketId = ticketId;
        this.actorName = actorName;
        this.description = description;
    }

    public RequestType getType(){
        return type;
    }

    public Role getRole(){
        return role;
    }

    public int getTicketId(){
        return ticketId;
    }

    public String getActorName(){
        return actorName;
    }

    public String getDescription(){
        return description;
    }
}
