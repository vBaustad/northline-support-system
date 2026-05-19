# NorthLine Support System

Distributed client-server support ticket system implemented in Java using TCP sockets and object-based communication.

---

# Project Goal

The purpose of this project is to implement a centralized concurrent support system where:

- Registrars can create and cancel tickets
- Agents can fetch and complete tickets
- The server coordinates all shared state safely
- Multiple clients can interact concurrently
- System invariants are preserved during simultaneous operations

---

# Core Architecture

The system follows a centralized client-server architecture.

```text
Clients
    ↓
ClientHandler
    ↓
TicketManager
    ↓
Shared Ticket State
