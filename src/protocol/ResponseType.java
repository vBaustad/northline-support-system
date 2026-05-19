package protocol;

// Definerer mulige responstyper serveren
// kan sende tilbake til klienten
public enum ResponseType {

    // Operasjonen ble gjennomført vellykket
    SUCCESS,
    
    // Operasjonen feilet eller var ugyldig
    ERROR
}
