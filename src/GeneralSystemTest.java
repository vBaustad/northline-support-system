import model.Ticket;
import model.TicketStatus;
import service.TicketManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GeneralSystemTest {

    public static void main(String[] args) throws Exception {
        TicketManager manager = new TicketManager();

        printHeader("1. Create tickets");

        Ticket ticket1 = manager.createTicket("Cannot access internal system");
        Ticket ticket2 = manager.createTicket("VPN not working");
        Ticket ticket3 = manager.createTicket("Email login failed");
        Ticket invalidTicket = manager.createTicket("");

        System.out.println("Created: " + ticket1);
        System.out.println("Created: " + ticket2);
        System.out.println("Created: " + ticket3);
        System.out.println("Create empty description should fail: " + invalidTicket);

        printHeader("2. Cancel NEW ticket");

        boolean cancelled = manager.cancelTicket(ticket3.getId());
        System.out.println("Cancel ticket #" + ticket3.getId() + ": " + cancelled);

        boolean cancelSameAgain = manager.cancelTicket(ticket3.getId());
        System.out.println("Cancel same ticket again should fail: " + cancelSameAgain);

        printHeader("3. Concurrent assignment and completion");

        Thread agentA = createAgentThread(manager, "AgentA");
        Thread agentB = createAgentThread(manager, "AgentB");
        Thread agentC = createAgentThread(manager, "AgentC");

        agentA.start();
        agentB.start();
        agentC.start();

        agentA.join();
        agentB.join();
        agentC.join();

        printHeader("4. Wrong agent test");

        Ticket ticket4 = manager.createTicket("Printer does not work");
        System.out.println("Created: " + ticket4);

        Ticket assignedToCorrectAgent = manager.assignNextTicket("CorrectAgent");
        System.out.println("CorrectAgent got: " + assignedToCorrectAgent);

        boolean wrongAgentComplete = manager.completeTicket(
                assignedToCorrectAgent.getId(),
                "WrongAgent"
        );

        System.out.println("Wrong agent completing assigned ticket should fail: " + wrongAgentComplete);

        boolean correctAgentComplete = manager.completeTicket(
                assignedToCorrectAgent.getId(),
                "CorrectAgent"
        );

        System.out.println("Correct agent completing assigned ticket should succeed: " + correctAgentComplete);

        printHeader("5. Invalid operations");

        boolean cancelCompleted = manager.cancelTicket(ticket1.getId());
        System.out.println("Cancel completed ticket should fail: " + cancelCompleted);

        boolean completeAlreadyCompleted = manager.completeTicket(ticket2.getId(), "AgentB");
        System.out.println("Completing already completed ticket should fail: " + completeAlreadyCompleted);

        boolean completeUnknownTicket = manager.completeTicket(999, "AgentA");
        System.out.println("Complete unknown ticket should fail: " + completeUnknownTicket);

        boolean cancelUnknownTicket = manager.cancelTicket(999);
        System.out.println("Cancel unknown ticket should fail: " + cancelUnknownTicket);

        printHeader("6. Ticket counts");

        System.out.println("Total tickets: " + manager.getTicketCount());
        System.out.println("NEW: " + manager.getTicketCountByStatus(TicketStatus.NEW));
        System.out.println("ASSIGNED: " + manager.getTicketCountByStatus(TicketStatus.ASSIGNED));
        System.out.println("COMPLETED: " + manager.getTicketCountByStatus(TicketStatus.COMPLETED));
        System.out.println("CANCELLED: " + manager.getTicketCountByStatus(TicketStatus.CANCELLED));

        printHeader("7. All tickets");

        for (Ticket ticket : manager.getAllTickets()) {
            System.out.println(ticket);
        }

        printHeader("8. Log output");

        printLastLogLines("system.log", 30);
    }

    private static Thread createAgentThread(TicketManager manager, String agentName) {
        return new Thread(() -> {
            Ticket assigned = manager.assignNextTicket(agentName);
            System.out.println(agentName + " got: " + assigned);

            if (assigned != null) {
                boolean completed = manager.completeTicket(assigned.getId(), agentName);
                System.out.println(agentName + " completed ticket #" + assigned.getId() + ": " + completed);
            }
        });
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }

    private static void printLastLogLines(String fileName, int numberOfLines) throws Exception {
        Path logPath = Path.of(fileName);

        if (!Files.exists(logPath)) {
            System.out.println("No " + fileName + " file found.");
            return;
        }

        List<String> logLines = Files.readAllLines(logPath);
        int start = Math.max(0, logLines.size() - numberOfLines);

        for (int i = start; i < logLines.size(); i++) {
            System.out.println(logLines.get(i));
        }
    }
}