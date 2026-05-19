import model.Ticket;
import model.TicketStatus;
import service.TicketManager;

public class TestTicketManager {
    public static void main(String[] args) throws InterruptedException {
        TicketManager manager = new TicketManager();

        System.out.println("=== Create tickets ===");

        Ticket ticket1 = manager.createTicket("Cannot access internal system");
        Ticket ticket2 = manager.createTicket("VPN not working");
        Ticket ticket3 = manager.createTicket("Email login failed");

        System.out.println("Created: " + ticket1);
        System.out.println("Created: " + ticket2);
        System.out.println("Created: " + ticket3);

        System.out.println("\n=== Cancel NEW ticket ===");

        boolean cancelled = manager.cancelTicket(ticket3.getId());
        System.out.println("Cancelled ticket #" + ticket3.getId() + ": " + cancelled);

        System.out.println("\n=== Concurrent assignment ===");

        Thread agentA = new Thread(() -> {
            Ticket assigned = manager.assignNextTicket("AgentA");
            System.out.println("AgentA got: " + assigned);

            if (assigned != null) {
                boolean completed = manager.completeTicket(assigned.getId(), "AgentA");
                System.out.println("AgentA completed ticket #" + assigned.getId() + ": " + completed);
            }
        });

        Thread agentB = new Thread(() -> {
            Ticket assigned = manager.assignNextTicket("AgentB");
            System.out.println("AgentB got: " + assigned);

            if (assigned != null) {
                boolean completed = manager.completeTicket(assigned.getId(), "AgentB");
                System.out.println("AgentB completed ticket #" + assigned.getId() + ": " + completed);
            }
        });

        agentA.start();
        agentB.start();

        agentA.join();
        agentB.join();

        System.out.println("\n=== Invalid operations ===");

        boolean cancelAssignedOrCompleted = manager.cancelTicket(ticket1.getId());
        System.out.println("Trying to cancel ticket #" + ticket1.getId() + " after assignment/completion: " + cancelAssignedOrCompleted);

        boolean wrongAgentComplete = manager.completeTicket(ticket2.getId(), "WrongAgent");
        System.out.println("Wrong agent trying to complete ticket #" + ticket2.getId() + ": " + wrongAgentComplete);

        System.out.println("\n=== Counts ===");

        System.out.println("Total tickets: " + manager.getTicketCount());
        System.out.println("NEW: " + manager.getTicketCountByStatus(TicketStatus.NEW));
        System.out.println("ASSIGNED: " + manager.getTicketCountByStatus(TicketStatus.ASSIGNED));
        System.out.println("COMPLETED: " + manager.getTicketCountByStatus(TicketStatus.COMPLETED));
        System.out.println("CANCELLED: " + manager.getTicketCountByStatus(TicketStatus.CANCELLED));

        System.out.println("\n=== All tickets ===");

        for (Ticket t : manager.getAllTickets()) {
            System.out.println(t);
        }
    }
}