package client;

import protocol.Request;
import protocol.RequestType;
import protocol.Response;
import protocol.ResponseType;
import protocol.Role;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import model.Ticket;

public class AgentClient {

    // Serveradresse og port klienten kobler til
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Få agentnavn fra brukeren
        System.out.print("Enter agent name: ");
        String agentName = scanner.nextLine().trim();

        // Åpne socket-tilkobling til serveren og opprett objektstrømmer
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            printHelp();

            boolean running = true;
            while (running) {
                System.out.print("agent> ");
                String command = scanner.nextLine().trim().toLowerCase();

                // Les kommando og velg riktig handling
                switch (command) {
                    case "fetch":
                        handleFetch(output, input, agentName);
                        break;
                    case "complete":
                        handleComplete(scanner, output, input, agentName);
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "quit":
                    case "exit":
                        running = false;
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            }

        } catch (Exception e) {
            // Feilhåndtering ved tilkoblings- eller kommunikasjonsproblemer
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
        System.out.println("Agent client stopped.");
    }

    // Sender FETCH_NEXT_TICKET-forespørsel og leser responsen
    private static void handleFetch(ObjectOutputStream output,
                                    ObjectInputStream input,
                                    String agentName) throws Exception {

        Request request = new Request(
                RequestType.FETCH_NEXT_TICKET,
                Role.AGENT,
                -1,
                agentName,
                null
        );

        output.writeObject(request); // send request til server
        output.flush();

        Response response = (Response) input.readObject(); // les svar fra server
        printResponse(response);

        if (response.getType() == ResponseType.SUCCESS && response.getTicket() != null) {
            Ticket assigned = response.getTicket();

            System.out.println(
                "You have been assigned ticket #" +
                assigned.getId() +
                ": " +
                assigned.getDescription()
            );
        }
    }

    // Sender COMPLETE_TICKET-forespørsel for en spesifikk ticket-id
    private static void handleComplete(Scanner scanner,
                                       ObjectOutputStream output,
                                       ObjectInputStream input,
                                       String agentName) throws Exception {

        System.out.print("Ticket ID to complete: ");
        String line = scanner.nextLine().trim();

        try {
            int ticketId = Integer.parseInt(line);
            Request request = new Request(
                    RequestType.COMPLETE_TICKET,
                    Role.AGENT,
                    ticketId,
                    agentName,
                    null
            );

            output.writeObject(request);
            output.flush();

            Response response = (Response) input.readObject();
            printResponse(response);

        } catch (NumberFormatException e) {
            // Håndterer ugyldig input for ticket-id
            System.out.println("Invalid ticket ID. Please enter a numeric value.");
        }
    }

    // Viser tilgjengelige kommandoer for agentbrukeren
    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  fetch    - Fetch and assign next available ticket");
        System.out.println("  complete - Complete an assigned ticket by ID");
        System.out.println("  help     - Show this help message");
        System.out.println("  quit     - Exit the agent client");
    }

    // Skriver ut serverens svar på en konsistent måte
    private static void printResponse(Response response) {
        if (response == null) {
            System.out.println("No response from server.");
            return;
        }

        System.out.println("[" + response.getType() + "] " + response.getMessage());
    }
}
