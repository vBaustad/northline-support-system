package protocol;

// Definerer alle støttede operasjoner som klientene
// kan sende til serveren gjennom Request-objekter
public enum RequestType {

    // Registrator oppretter en ny ticket
    CREATE_TICKET,

    // Registrator kansellerer en ticket
    // som fortsatt har status NEW
    CANCEL_TICKET,

    // Agent ber om neste tilgjengelige ticket
    FETCH_NEXT_TICKET,

    // Agent markerer ticket som fullført
    COMPLETE_TICKET
}
