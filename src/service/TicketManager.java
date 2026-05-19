package service;

import model.Ticket;
import model.TicketStatus;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class TicketManager {
    private final List<Ticket> tickets = new ArrayList<>();

    public synchronized Ticket createTicket(String description){
        int id = IdGenerator.nextId();

        Ticket ticket = new Ticket(id, description);

        tickets.add(ticket);

        return ticket;
    }

    public synchronized boolean cancelTicket(int ticketId){
        for (Ticket ticket : tickets){
            if(ticket.getId() == ticketId && ticket.getStatus() == TicketStatus.NEW){
                ticket.cancel();

                return true;
            }
        }

        return false;
    }

    public synchronized Ticket assignNextTicket(String agentName){
        for(Ticket ticket : tickets){
            if(ticket.getStatus() == TicketStatus.NEW){
                ticket.assignTo(agentName);

                return ticket;
            }
        }
        return null;
    }

    public synchronized boolean completeTicket(int ticketId, String agentName){
        for(Ticket ticket : tickets) {
            if(ticket.getId() == ticketId && 
                    ticket.getStatus() == TicketStatus.ASSIGNED && 
                    agentName.equals(ticket.getAssignedAgent())){
                
                ticket.complete();

                return true;
            }
        }

        return false;
    }

    public synchronized List<Ticket> getAllTickets(){
        return new ArrayList<>(tickets);
    }
}
