package util;

// Genererer unike ID-er for tickets i systemet
public class IdGenerator {

    // Holder styr på neste tilgjengelige ID
    //
    // Starter på 1 for enklere lesbarhet i logger
    // og konsollutskrifter
    private static int currentId = 1;

    // synchronized sørger for at flere tråder
    // ikke kan generere samme ID samtidig
    public static synchronized int nextId(){

        // Returnerer nåværende ID og øker telleren
        return currentId++;
    }
}
