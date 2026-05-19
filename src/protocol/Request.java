package protocol;

import java.io.Serializable;

/*
Request field usage:
- CREATE_TICKET: requires role=REGISTRAR, actorName, description
- CANCEL_TICKET: requires role=REGISTRAR, actorName, ticketId
- FETCH_NEXT_TICKET: requires role=AGENT, actorName
- COMPLETE_TICKET: requires role=AGENT, actorName, ticketId
*/
public class Request implements Serializable{
    private final RequestType type;
    private final Role role;
    private final int ticketId;
    private final String actorName;
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
