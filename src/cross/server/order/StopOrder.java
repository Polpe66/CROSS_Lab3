/*
 * StopOrder class una classe che rappresenta un ordine di tipo Stop ed è sottoclasse di order
 * @param priceStopOrder: prezzo di stop dell'ordine
 */
package cross.server.order;

import java.net.InetAddress;

public class StopOrder extends Order {
    private int priceStopOrder;

    public StopOrder(int id, String type, int size, int priceStopOrder, String username, InetAddress IP, int port) {
        super(id, type, "StopOrder", size, username, IP, port);
        this.priceStopOrder = priceStopOrder;
    }
    
    public int getPriceStopOrder() {

        return priceStopOrder;
    }
}
