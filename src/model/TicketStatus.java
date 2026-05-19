package model;

// Definerer alle mulige tilstander en ticket
// kan ha gjennom livssyklusen sin
public enum TicketStatus {
    
    // Ticket er opprettet, men ikke tildelt agent ennå
    NEW,

    // Ticket er tildelt en supportagent
    ASSIGNED,

    // Ticket er ferdig behandlet av agent
    COMPLETED,

    // Ticket ble kansellert før den ble tildelt
    CANCELLED
}