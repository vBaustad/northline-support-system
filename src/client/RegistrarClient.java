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

public class RegistrarClient {

    // Serveradresse og port klienten kobler til
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Få registratornavn fra brukeren
        System.out.print("Enter registrar name: ");
        String registrarName = scanner.nextLine().trim();

        // Opprett socket og objektstrømmer mot serveren
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            printHelp();

            boolean running = true;
            while (running) {
                System.out.print("registrar> ");
                String command = scanner.nextLine().trim().toLowerCase();

                // Håndter forskjellige kommandoer fra brukeren
                switch (command) {
                    case "create":
                        handleCreate(scanner, output, input, registrarName);
                        break;
                    case "cancel":
                        handleCancel(scanner, output, input, registrarName);
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
            // Håndterer alle I/O-feil for tilkoblingen
            System.out.println("Error: " + e.getMessage());
        }

        scanner.close();
        System.out.println("Registrar client stopped.");
    }

    // Sender CREATE_TICKET-forespørsel med beskrivelse til serveren
    private static void handleCreate(Scanner scanner,
                                     ObjectOutputStream output,
                                     ObjectInputStream input,
                                     String registrarName) throws Exception {

        System.out.print("Ticket description: ");
        String description = scanner.nextLine().trim();

        if (description.isEmpty()) {
            System.out.println("Description must not be empty.");
            return;
        }

        Request request = new Request(
                RequestType.CREATE_TICKET,
                Role.REGISTRAR,
                -1,
                registrarName,
                description
        );

        output.writeObject(request); // send request til server
        output.flush();

        Response response = (Response) input.readObject(); // vent på serverens svar
        printResponse(response);

        if (response.getType() == ResponseType.SUCCESS && response.getTicket() != null) {
            System.out.println("Created: " + response.getTicket());
        }
    }

    // Sender CANCEL_TICKET-forespørsel for en spesifikk ticket-id
    private static void handleCancel(Scanner scanner,
                                     ObjectOutputStream output,
                                     ObjectInputStream input,
                                     String registrarName) throws Exception {

        System.out.print("Ticket ID to cancel: ");
        String line = scanner.nextLine().trim();

        try {
            int ticketId = Integer.parseInt(line);
            Request request = new Request(
                    RequestType.CANCEL_TICKET,
                    Role.REGISTRAR,
                    ticketId,
                    registrarName,
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

    // Viser tilgjengelige kommandoer for registratorbrukeren
    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  create  - Create a new ticket");
        System.out.println("  cancel  - Cancel a ticket with status NEW");
        System.out.println("  help    - Show this help message");
        System.out.println("  quit    - Exit the registrar client");
    }

    // Skriver ut serverens svar på konsollen
    private static void printResponse(Response response) {
        if (response == null) {
            System.out.println("No response from server.");
            return;
        }

        System.out.println("[" + response.getType() + "] " + response.getMessage());
    }
}
