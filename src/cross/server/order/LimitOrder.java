/*
 * LimitOrder class una classe che rappresenta un ordine di tipo Limit ed è sottoclasse di order
 * @param priceLimitOrder: prezzo limite dell'ordine
 */
package cross.server.order;
import java.net.InetAddress;

public class LimitOrder extends Order{
    private int priceLimitOrder;

    public LimitOrder(int id, String type, int size, int priceLimitOrder, String username, InetAddress IP, int port) {
        super(id, type, "LimitOrder", size, username, IP, port);
        this.priceLimitOrder = priceLimitOrder;
    }
    
    public int getPriceLimitOrder() {

        return priceLimitOrder;
    }
    
}
