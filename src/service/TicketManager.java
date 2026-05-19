package service;

import logging.SystemLogger;
import model.Ticket;
import model.TicketStatus;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TicketManager {
    /* LinkedHashMap brukes fordi:
        1. Vi ønsker rask lookup via ticket-ID
        2. Vi ønsker å bevarde innsettingsrekkefølge, slik at eldste NEW-ticket tildeles først
    */  
    
    private final Map<Integer, Ticket> tickets = new LinkedHashMap<>();

    // Delt singleton-logger brukt av alle tråder
    private final SystemLogger logger = SystemLogger.getInstance();

    // synchronized sørger for at flere klienttråder
    // ikke kan opprette tickets samtidig på en usikker måte
    public synchronized Ticket createTicket(String description){

        // Validerer input for å unngå ugyldige tickets
        if(description == null || description.isBlank()){

            logger.error(
                "CREATE",
                "Failed to create ticket because description was invalid"
            );

            return null;
        }  

        // Genererer unik ticket-ID  på thread-safe måte
        int id = IdGenerator.nextId();

        // Oppretter ticket med NEW-status
        Ticket ticket = new Ticket(id, description);

        // Legger ticket inn i delt datastruktur
        tickets.put(id, ticket);

        logger.info(
            "CREATE",
            "ticketId=" + id +
            " description=\"" + description + "\""
        );

        // Vekker eventuelle tråder som venter på nye tickets
        notifyAll();

        return ticket;
    }

    // En ticket kan kun kanselleres dersom den
    // fortsatt har status NEW
    public synchronized boolean cancelTicket(int ticketId) {
        Ticket ticket = tickets.get(ticketId);

        // Ticket eksisterer ikke
        if (ticket == null){

            logger.error(
                "CANCEL",
                "Failed to cancel because ticketId=" + ticketId + " does not exist"
            );

            return false;
        }

        // Kun NEW tickets kan kanselleres
        if (ticket.getStatus() != TicketStatus.NEW){
            
            logger.error(
                "CANCEL",
                "Failed to cancel ticketId=" + ticketId +
                " because status was " + ticket.getStatus()
            );

            return false;
        }

        ticket.cancel();

        logger.info(
            "CANCEL",
            "ticketId=" + ticketId
        );

        return true;
    }

    // Tildeler neste tilgjengelige NEW-ticket til agent
    public synchronized Ticket assignNextTicket(String agentName) {
        // Hindrer ugyldig agentnavn
        if (agentName == null || agentName.isBlank()){
            
            logger.error(
                "ASSIGN",
                "Failed to assign ticket because agent name was invalid"
            );

            return null;
        }

        // Finner første ticket som fortsatt er NEW
        Ticket ticket = findFirstNewTicket();

        // Ingen tilgjengelige tickets
        if (ticket == null){
            
            logger.info(
                "ASSIGN",
                "No available NEW tickets for agent=" + agentName
            );

            return null;
        }

        // Ticket blir nå eksklusivt tildelt agent
        ticket.assignTo(agentName);

        logger.info(
            "ASSIGN",
            "ticketId=" + ticket.getId() +
            " agent=" + agentName
        );

        return ticket;
    }

     /* 
        Fullfører ticket dersom:
        1. Ticket eksisterer
        2. Ticket er ASSIGNED
        3. Riktig agent forsøker å fullføre den
    */
    public synchronized boolean completeTicket(int ticketId, String agentName) {

        if (agentName == null || agentName.isBlank()){
            
            logger.error(
                "COMPLETE",
                "Failed to complete ticket because agent name was invalid"
            );

            return false;
        }

        Ticket ticket = tickets.get(ticketId);

        if (ticket == null){
            
            logger.error(
                "COMPLETE",
                "Failed to complete because ticketId=" + ticketId + " does not exist"
            );

            return false;
        }

        // Kun ASSIGNED tickets kan fullføres
        if (ticket.getStatus() != TicketStatus.ASSIGNED){
            
            logger.error(
                "COMPLETE",
                "Failed to complete ticketId=" + ticketId +
                " because status was " + ticket.getStatus()
            );

            return false;
        }

        // Kun agenten som eier ticketen kan fullføre den
        if (!agentName.equals(ticket.getAssignedAgent())){
            
            logger.error(
                "COMPLETE",
                "Failed to complete ticketId=" + ticketId +
                " because wrong agent attempted completion"
            );

            return false;
        }

        ticket.complete();

        logger.info(
            "COMPLETE",
            "ticketId=" + ticketId +
            " agent=" + agentName
        );

        return true;
    }

    // Returnerer kopi av alle tickets
    // for å beskytte intern datastruktur
    public synchronized List<Ticket> getAllTickets(){
        return new ArrayList<>(tickets.values());
    }

    // Filtrerer tickets basert på status
    // Kan brukes om vi ønsker filtrering
    public synchronized List<Ticket> getTicketByStatus(TicketStatus status){
        List<Ticket> result = new ArrayList<>();

        for (Ticket ticket : tickets.values()) {
            if(ticket.getStatus() == status){
                result.add(ticket);
            }
        }

        return result;
    }

    // Returnerer totalt antall tickets
    // Mest brukt for evt debug
    public synchronized int getTicketCount(){
        return tickets.size();
    }

    // Teller antall tickets med gitt status
    // Mest brukt for evt debug
    public synchronized int getTicketCountByStatus(TicketStatus status){
        int count = 0;

        for (Ticket ticket : tickets.values()) {
            if(ticket.getStatus() == status){
                count++;
            }
        }

        return count;
    }

    // Hjelpemetode som finner første ticket
    // med status NEW i innsettingsrekkefølge
    private Ticket findFirstNewTicket() {
        for (Ticket ticket : tickets.values()) {
            if(ticket.getStatus() == TicketStatus.NEW){
                return ticket;
            }
        }

        return null;
    }

}
