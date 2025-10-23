/*
 * MarketOrder class una classe che rappresenta un ordine di tipo Market ed è sottoclasse di order
 */
package cross.server.order;
import java.net.InetAddress;

public class MarketOrder extends Order {

    public MarketOrder(int id, String type, int size, String username, InetAddress IP, int port) {
        super(id, type, "MarketOrder", size, username, IP, port);
    }
}
