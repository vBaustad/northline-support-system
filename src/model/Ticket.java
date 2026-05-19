package model;

import java.io.Serializable;

// Disse metodene skal KUN kalles av TicketManager.
public class Ticket implements Serializable{
    private final int id;
    private final String description;
    private TicketStatus status;
    private String assignedAgent;

    public Ticket(int id, String description){
        this.id = id;
        this.description = description;
        this.status = TicketStatus.NEW;
        this.assignedAgent = null;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus(){
        return status;
    }

    public String getAssignedAgent() {
        return assignedAgent;
    }

    public void assignTo(String agentName) {
        this.status = TicketStatus.ASSIGNED;
        this.assignedAgent = agentName;
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    public void complete() {
        this.status = TicketStatus.COMPLETED;
    }

    @Override
    public String toString(){
        return "Ticket #" + id + " [" + status + "] " + description + 
            (assignedAgent != null ? " | Agent: " + assignedAgent : "");
    }
}
