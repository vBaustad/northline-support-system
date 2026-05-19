package protocol;

// Definerer hvilke roller klientene kan ha i systemet
//
// Rollene brukes av serveren for å validere
// hvilke operasjoner klienten har tilgang til
public enum Role {

    // Registrator kan opprette og kansellere tickets
    REGISTRAR,

    // Agent kan hente og fullføre tickets
    AGENT
}
