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
```

Important rule:

- Only `TicketManager` is allowed to modify ticket state.
- Clients and `ClientHandler` must only send requests and handle responses.

---

# Ticket Lifecycle

```text
NEW -> ASSIGNED
NEW -> CANCELLED
ASSIGNED -> COMPLETED
```

Final states:

- COMPLETED
- CANCELLED

---

# Main Invariant

A ticket can never be assigned to more than one agent at the same time.

---

# Package Structure

```text
src/
├── server/
├── client/
├── model/
├── protocol/
├── service/
├── logging/
└── util/
```

---

# Shared Foundation

The following files are considered shared foundation/contracts and should be changed carefully:

## model/
- Ticket.java
- TicketStatus.java

## protocol/
- Request.java
- Response.java
- RequestType.java
- ResponseType.java
- Role.java

## service/
- TicketManager.java

## util/
- IdGenerator.java

If changes are needed in these files:
- notify the group first
- avoid breaking shared contracts

---

# Synchronization Strategy

The system uses a simple synchronized shared-state model.

- Shared ticket state is stored in `TicketManager`
- State-changing methods are synchronized
- Ticket assignment must be atomic
- Lifecycle validation happens inside `TicketManager`

---

# Current Work Distribution

## Vebjørn
Responsible for:
- TicketManager logic
- synchronization
- lifecycle validation
- atomic operations
- invariant protection

Main files:
- service/TicketManager.java

---

## Person 2
Responsible for:
- TCP server
- socket handling
- client connection handling
- request routing

Main files:
- server/ServerMain.java
- server/ClientHandler.java

---

## Person 3
Responsible for:
- console clients
- logging
- testing/demo support

Main files:
- client/RegistrarClient.java
- client/AgentClient.java
- logging/SystemLogger.java

---

# Git Workflow

Please avoid modifying files outside your main area unless discussed first.

Recommended workflow:

```bash
git pull
git checkout -b feature/your-feature-name
```

Commit often with small commits.

Before pushing:
- pull latest changes
- resolve conflicts locally

---

# Build / Run

(To be updated later)

---

# Team Notes

- Keep the architecture simple
- Focus on correctness and synchronization
- Do not bypass TicketManager when changing ticket state
- Avoid overengineering
- Prioritize clean ownership boundaries
