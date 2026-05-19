package service;

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

    // synchronized sørger for at flere klienttråder
    // ikke kan opprette tickets samtidig på en usikker måte
    public synchronized Ticket createTicket(String description){

        // Validerer input for å unngå ugyldige tickets
        if(description == null || description.isBlank()){
            return null;
        }  

        // Genererer unik ticket-ID  på thread-safe måte
        int id = IdGenerator.nextId();

        // Oppretter ticket med NEW-status
        Ticket ticket = new Ticket(id, description);

        // Legger ticket inn i delt datastruktur
        tickets.put(id, ticket);

        // Vekker eventuelle tråder som venter på nye tickets
        notifyAll();

        return ticket;
    }

    // En ticket kan kun kanselleres dersom den
    // fortsatt har status NEW
    public synchronized boolean cancelTicket(int ticketId) {
        Ticket ticket = tickets.get(ticketId);

        // Ticket eksisterer ikke
        if (ticket == null) return false;

        // Kun NEW tickets kan kanselleres
        if (ticket.getStatus() != TicketStatus.NEW) return false;

        ticket.cancel();

        return true;
    }

    // Tildeler neste tilgjengelige NEW-ticket til agent
    public synchronized Ticket assignNextTicket(String agentName) {
        // Hindrer ugyldig agentnavn
        if (agentName == null || agentName.isBlank()) return null;

        // Finner første ticket som fortsatt er NEW
        Ticket ticket = findFirstNewTicket();

        // Ingen tilgjengelige tickets
        if (ticket == null) return null;

        // Ticket blir nå eksklusivt tildelt agent
        ticket.assignTo(agentName);

        return ticket;
    }

     /* 
        Fullfører ticket dersom:
        1. Ticket eksisterer
        2. Ticket er ASSIGNED
        3. Riktig agent forsøker å fullføre den
    */
    public synchronized boolean completeTicket(int ticketId, String agentName) {

        if (agentName == null || agentName.isBlank()) return false;

        Ticket ticket = tickets.get(ticketId);

        if (ticket == null) return false;

        // Kun ASSIGNED tickets kan fullføres
        if (ticket.getStatus() != TicketStatus.ASSIGNED) return false;

        // Kun agenten som eier ticketen kan fullføre den
        if (!agentName.equals(ticket.getAssignedAgent())) return false;

        ticket.complete();

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
