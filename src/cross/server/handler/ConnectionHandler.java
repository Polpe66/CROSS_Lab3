
/*
 * ConnectionHandler class è una classe che gestisce la connessione con un client
 */
package cross.server.handler;
import cross.server.order.*;
import cross.server.orderbook.*;
import cross.server.user.UserManager;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionHandler implements Runnable{
    private Socket socket; // Socket per la connessione con il client
    private BufferedReader input; // BufferedReader per leggere i messaggi dal client
    private PrintWriter output; // PrintWriter per inviare messaggi al client
    private String username; // Username dell'utente connesso
    private Gson gson = new Gson(); // Gson per la conversione di oggetti input JSON
    private AtomicBoolean running; // AtomicBoolean per controllare se il server è input esecuzione
    private AtomicInteger orderId; // AtomicInt per generare un id univoco per gli ordini
    private InetAddress IP; // Indirizzo IP del client
    private static int UDP_PORT; // Porta UDP per l'invio delle notifiche

    private final Orderbook orderbook; // orderbook per gestire gli ordini
    private final UserManager userManager; // UserManager per gestire gli utenti
    private final BuyStopOrder buyStopOrder; // BuyStopOrder per gestire gli ordini di stop di acquisto
    private final SellStopOrder sellStopOrder; // SellStopOrder per gestire gli ordini di stop di vendita

    private final StoricoOrdiniHandler storicoOrdiniHandler; // StoricoOrdiniHandler per gestire la cronologia degli ordini

    public ConnectionHandler(Socket socket, AtomicBoolean running, UserManager userManager, Orderbook orderbook, BuyStopOrder buyStopOrder, SellStopOrder sellStopOrder, AtomicInteger orderId, StoricoOrdiniHandler storicoOrdiniHandler) {
        this.socket = socket;
        this.running = running;
        this.userManager = userManager;
        this.orderbook = orderbook;
        this.buyStopOrder = buyStopOrder;
        this.sellStopOrder = sellStopOrder;
        this.orderId = orderId;
        this.storicoOrdiniHandler = storicoOrdiniHandler;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.IP = socket.getInetAddress();
    }

    // Metodo per gestire le richieste TCP del client
    public void handleRequest(String req) {
        JsonObject request = JsonParser.parseString(req).getAsJsonObject();;
        JsonObject values = request.get("values").getAsJsonObject();
        switch(request.get("operation").getAsString()){
            case("login"):
                handleLoginRequest(values);
                break;
            case("register"):
                handleRegisterRequest(values);
                break;
            case("logout"):
                handleLogoutRequest();
                break;
            case("updateCredentials"):
                handleUpdateCredentialsRequest(values);
                break;
            case("insertLimitOrder"):
                handleInsertLimitOrderRequest(values);
                break;
            case("insertMarketOrder"):
                handleInsertMarketOrderRequest(values);
                break;
            case("insertStopOrder"):
                handleInsertStopOrderRequest(values);
                break;
            case("cancelOrder"):
                handleCancelOrderRequest(values);
                break;
            case("getPriceHistory"):
                handleGetPriceHistoryRequest(values);
                break;
            case("getCurrentPrice"):
                handleGetCurrentPriceRequest();
                break;
            default:
                handleError();
                break;
        };
    }

    // Metodo per inviare una risposta TCP al client
    public void sendResponse(JsonObject response) {
        String res = gson.toJson(response);
        try {
            output.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodi per gestire le richieste del client di Login
    private void handleLoginRequest(JsonObject values) {
        String username = values.get("username").getAsString();
        String password = values.get("password").getAsString();
        int code = 103;
        try {
            code = userManager.login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("response", code);

        switch (code) {
            case 100:
                this.username = username;
                response.addProperty("errorMessage", "OK");
                break;
            case 101:
                response.addProperty("errorMessage", "username/password mismatch or non existent username.");
                break;
            case 102:
                response.addProperty("errorMessage", "user already logged input.");
                break;
            case 103:
                response.addProperty("errorMessage", "other error cases.");
                break;
            default:
                response.addProperty("errorMessage", "unknown error.");
        }
        sendResponse(response);
    }



    // Metodi per gestire le richieste del client di registrazione
    private void handleRegisterRequest(JsonObject values) {
        String username = values.get("username").getAsString();
        String password = values.get("password").getAsString();
        int code = 103;
        try {
            code = userManager.register(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("response", code);

        switch (code) {
            case 100:
                response.addProperty("errorMessage", "OK");
                break;
            case 101:
                response.addProperty("errorMessage", "invalid password.");
                break;
            case 102:
                response.addProperty("errorMessage", "Username not available.");
                break;
            case 103:
                response.addProperty("errorMessage", "Other error cases.");
                break;
            default:
                response.addProperty("errorMessage", "unknown error.");
        }
        sendResponse(response);
    }

    // Metodi per gestire le richieste del client di Logout
    private void handleLogoutRequest() {
        int code = 101;
        try {
            code = userManager.logout(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("response", code);

        switch (code) {
            case 100:
                response.addProperty("errorMessage", "OK");
                break;
            case 101:
                response.addProperty("errorMessage", "user not logged input or other error cases.");
                break;
            default:
                response.addProperty("errorMessage", "unknown error.");
        }
        sendResponse(response);
    }


    // Metodi per gestire le richieste del client di aggiornamento delle credenziali
    private void handleUpdateCredentialsRequest(JsonObject values) {
        String username = values.get("username").getAsString();
        String oldPassword = values.get("oldPassword").getAsString();
        String newPassword = values.get("newPassword").getAsString();
        int code = 105;

        try {
            code = userManager.updateCredentials(username, oldPassword, newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject response = new JsonObject();
        response.addProperty("response", code);

        switch (code) {
            case 100:
                response.addProperty("errorMessage", "OK");
                break;
            case 101:
                response.addProperty("errorMessage", "Invalid new password.");
                break;
            case 102:
                response.addProperty("errorMessage", "Username/Password mismatch or non existent username.");
                break;
            case 103:
                response.addProperty("errorMessage", "New password equal to old one.");
                break;
            case 104:
                response.addProperty("errorMessage", "user currently logged input.");
                break;
            case 105:
                response.addProperty("errorMessage", "Other error cases.");
                break;
            default:
                response.addProperty("errorMessage", "Unknown error.");
        }

        sendResponse(response);
    }

    // Metodi per gestire le richieste del client di inserimento di un LimitOrder
    private void handleInsertLimitOrderRequest(JsonObject values) {
        String type = values.get("type").getAsString();
        int size = values.get("size").getAsInt();
        int price = values.get("price").getAsInt();
        LimitOrder order = new LimitOrder(orderId.incrementAndGet(), type, size, price, username, IP, UDP_PORT);
        int orderid = -1;
        try {
            orderid = orderbook.addLimitOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("orderId", orderid);
        sendResponse(response);
    }


    // Metodi per gestire le richieste del client di inserimento di un MarketOrder
    private void handleInsertMarketOrderRequest(JsonObject values) {
        String type = values.get("type").getAsString();
        int size = values.get("size").getAsInt();
        MarketOrder order = new MarketOrder(orderId.incrementAndGet(), type, size, username, IP, UDP_PORT);
        int orderid = orderbook.processMarketOrder(order);
        JsonObject response = new JsonObject();
        response.addProperty("orderId", orderid);
        sendResponse(response);
    }

    // Metodi per gestire le richieste del client di inserimento di un StopOrder
    private void handleInsertStopOrderRequest(JsonObject values) {
        String type = values.get("type").getAsString();
        int size = values.get("size").getAsInt();
        int price = values.get("price").getAsInt();
        StopOrder order = new StopOrder(orderId.incrementAndGet(), type, size, price, username, IP, UDP_PORT);
        int orderid = -1;
        try {
            if (type.equals("bid")) {
                orderid = buyStopOrder.addBuyStopOrder(order);
            } else {
                orderid = sellStopOrder.addSellStopOrder(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObject response = new JsonObject();
        response.addProperty("orderId", orderid);
        sendResponse(response);
    }


    // Metodi per gestire le richieste del client di cancellazione di un ordine
    private void handleCancelOrderRequest(JsonObject values) {
        int orderid = values.get("orderid").getAsInt();
        boolean result = orderbook.removeLimitOrder(orderid, username) || buyStopOrder.removeBuyStopOrder(orderid, username) || sellStopOrder.removeSellStopOrder(orderid, username);
        JsonObject response = new JsonObject();
        response.addProperty("response", result ? 100 : 101);
        response.addProperty("errorMessage", result ? "OK" : "order does not exist or belongs to different user or has already been finalized or other error cases.");
        sendResponse(response);
    }

    // Metodi per gestire le richieste del client di ottenere il prezzo corrente
    private void handleGetCurrentPriceRequest() {
        JsonObject response = new JsonObject();
        response.addProperty("price", orderbook.getCurrentPrice());
        sendResponse(response);
    }

    // Metodi per gestire le richieste del client di ottenere la cronologia dei prezzi
    private void handleGetPriceHistoryRequest(JsonObject values) {
        String MMYYYY = values.get("month").getAsString();
        int month = Integer.parseInt(MMYYYY.substring(0, 2));
        int year = Integer.parseInt(MMYYYY.substring(2, 6));
        JsonObject response = storicoOrdiniHandler.getPriceHistory(month, year);
        sendResponse(response);
        
    }
   

    // Metodo per gestire errori
    private void handleError() {
        JsonObject response = new JsonObject();
        response.addProperty("response", 400);
        response.addProperty("errorMessage", "Operazione non riconosciuta.");
        sendResponse(response);
    }


    // Metodo per chiudere la connessione con il client
    private void closeConnection() {
        try {
            if (output != null) {
                JsonObject response = new JsonObject();
                response.addProperty("response", 999);
                response.addProperty("errorMessage", "Connessione chiusa dal server");
                output.println(response.toString()); // Invia un messaggio JSON al client
                if(username != null) userManager.logout(username);
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connessione chiusa con " + IP.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    // Metodo per eseguire il thread
    @Override
    public void run() {
        // il client invia la sua porta UDP su cui è input ascolto
        try{
            UDP_PORT= Integer.parseInt(input.readLine());
        }catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(socket.getSoTimeout());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("Connessione stabilita con " + IP.getHostAddress() + ":" + socket.getPort() + " sulla porta UDP " + UDP_PORT);
        while (running.get() && !socket.isClosed()) {
            try {
                String req = input.readLine();
                if (req == null) {
                    System.out.println("Connessione chiusa da " + IP.getHostAddress());
                    break;
                }
                handleRequest(req);
            } catch (SocketTimeoutException e){
                System.out.println("Timeout scaduto per la connessione con " + IP.getHostAddress());
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }        
        closeConnection();
    }   
}
