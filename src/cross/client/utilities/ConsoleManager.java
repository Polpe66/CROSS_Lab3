/*
 * ConsoleManager permette di sincronizzare la stampa su console, dato che anche essa risulta una risorsa condivisa
 */
package cross.client.utilities;
public class ConsoleManager {
    public static final Object lock = new Object();

    public static void print(String message) {
        synchronized (lock) {
            System.out.println(message);
        }
    }
}
