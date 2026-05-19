package model;

import java.io.Serializable;

/*    
    Serializable gjør at Ticket-objekter kan sendes
    mellom klient og server gjennom ObjectStreams   
    Disse metodene skal KUN kalles av TicketManager.
    Dette hjelper med å bevare systeminvariantene og
    sørger for at statusendringer skjer kontrollert.
*/
public class Ticket implements Serializable{

    // Unik ID for ticketen
    private final int id;

    // Beskrivelse av problemet registratoren oppretter
    private final String description;

    // Nåværende status i ticket-livssyklusen
    private TicketStatus status;

    // Navnet på agenten som er tildelt ticketen
    // Er null frem til ticketen blir assignet
    private String assignedAgent;

    public Ticket(int id, String description){
        this.id = id;
        this.description = description;

        // Alle nye tickets starter med status NEW
        this.status = TicketStatus.NEW;

        // Ingen agent er tildelt ved opprettelse
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


    // Tildeler ticketen til en agent
    // og oppdaterer status til ASSIGNED
    public void assignTo(String agentName) {
        this.status = TicketStatus.ASSIGNED;
        this.assignedAgent = agentName;
    }

    // Kansellerer ticketen
    // Kan kun gjøres mens ticketen fortsatt er NEW
    // (valideres i TicketManager)
    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    // Marker ticket som fullført
    // Valideres av TicketManager før kall
    public void complete() {
        this.status = TicketStatus.COMPLETED;
    }

    @Override
    public String toString(){

        // Gir lesbar representasjon av ticketen
        // Nyttig for logging og debugging
        return "Ticket #" + id + " [" + status + "] " + description + 
            (assignedAgent != null ? " | Agent: " + assignedAgent : "");
    }
}
