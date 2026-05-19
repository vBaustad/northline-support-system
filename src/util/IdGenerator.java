package util;

public class IdGenerator {
    private static int currentId = 1;

    public static synchronized int nextId(){
        return currentId++;
    }
}
