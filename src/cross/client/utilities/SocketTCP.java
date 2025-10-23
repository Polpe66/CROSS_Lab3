package cross.client.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;


/*
 * SocketTCP class per la gestione della connessione TCP e invio delle richieste al server
 */
public class SocketTCP implements Runnable {
    private Socket socket; // Socket TCP
    private static int UDP_PORT; // Porta UDP
    private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in)); // Input stream per leggere da tastiera
    private PrintWriter output; // Output stream per inviare al server
    private static Gson gson = new Gson(); // Gson per la conversione da e verso JSON
    private static volatile boolean running = true; // Flag per terminare il thread
    private static boolean isLogged = false; // Flag per vedere se l'utente è loggato
    private boolean pendingLogin = false; // Flag per vedere se il login è input attesa
    private boolean pendingLogout = false; // Flag per vedere se il logout è input attesa

    public SocketTCP(Socket socket, int PORT) {
        this.socket = socket;
        UDP_PORT = PORT;
        try {
            this.output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per inviare una richiesta al server
    public void sendRequest(JsonObject request) {
        if (request == null) return;
        if(request.get("operation").getAsString().equals("login")){
            pendingLogin=true;
        }
        if(request.get("operation").getAsString().equals("logout")){
            pendingLogout=true;
        }
        ConsoleManager.print("> Invio richiesta: " + request.toString());
        output.println(gson.toJson(request));
    }

    // Metodo per ottenere la socket
    public Socket getSocket() {
        return socket;
    }

    // Metodo per vedere se l'utente è loggato
    public boolean isLogged() {
        return isLogged;
    }

    // Metodo per settare se l'utente è loggato
    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    // Metodo per vedere se il login è input attesa
    public boolean isPendingLogin() {
        return pendingLogin;
    }   

    // Metodo per vedere se il logout è input attesa
    public boolean isPendingLogout() {
        return pendingLogout;
    }

    // Metodo per pulire i flag di login e logout
    public void clearPending() {
        pendingLogin = false;
        pendingLogout = false;
    }

    private static JsonObject printError() {
        ConsoleManager.print("> Errore: Operazione non valida.");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operation", "error");
        return jsonObject;
    }

    // Metodo per la gestione input caso l'utente non sia loggato
    private static JsonObject notLoggedError() {
        ConsoleManager.print("> Errore: Devi essere loggato per eseguire questa operazione.");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operation", "error");
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di registrazione
    private static JsonObject getRegisterRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci username: ");
        String username = input.readLine();
        ConsoleManager.print("> Inserisci password: ");
        String password = input.readLine();
        jsonObject.addProperty("operation", "register");
        JsonObject values = new JsonObject();
        values.addProperty("username", username);
        values.addProperty("password", password);
        jsonObject.add("values", values);
        return jsonObject;
    }


    // Metodo per la gestione delle richieste di login
    private static JsonObject getLoginRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci username: ");
        String username = input.readLine();
        ConsoleManager.print("> Inserisci password: ");
        String password = input.readLine();
        jsonObject.addProperty("operation", "login");
        JsonObject values = new JsonObject();
        values.addProperty("username", username);
        values.addProperty("password", password);
        jsonObject.add("values", values);
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di aggiornamento delle credenziali
    private static JsonObject getUpdateCredentialsRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci username: ");
        String username = input.readLine();

        ConsoleManager.print("> Inserisci vecchia password: ");
        String oldPassword = input.readLine();
        ConsoleManager.print("> Inserisci nuova password: ");
        String newPassword = input.readLine();

        jsonObject.addProperty("operation", "updateCredentials");
        JsonObject values = new JsonObject();
        values.addProperty("username", username);
        values.addProperty("old_password", oldPassword);
        values.addProperty("new_password", newPassword);
        jsonObject.add("values", values);
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di logout
    private static JsonObject getLogoutRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operation", "logout");
        jsonObject.add("values", new JsonObject());
        return jsonObject;
    }

    // Metodo per la validazione degli ordini
    private static boolean validateOrder(double size, double price, String type) {
        return size > 0 && size <= Integer.MAX_VALUE && price > 0 && price <= Integer.MAX_VALUE && (type.equals("ask") || type.equals("bid"));
    }

    // Metodo per la gestione delle richieste di inserimento di LimitOrder
    private static JsonObject getInsertLimitOrderRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci tipo (ask/bid): ");
        String type = input.readLine();
        ConsoleManager.print("> Inserisci quantità: ");
        double doubleSize = Double.parseDouble(input.readLine())*1000;
        ConsoleManager.print("> Inserisci prezzo: ");
        double doublePrice = Double.parseDouble(input.readLine())*1000;
        
        if (!validateOrder(doubleSize, doublePrice, type)) {
            return printError();
        }

        int size = (int) doubleSize;
        int price = (int) doublePrice;
        
        jsonObject.addProperty("operation", "insertLimitOrder");
        JsonObject values = new JsonObject();
        values.addProperty("type", type);
        values.addProperty("size", size);
        values.addProperty("price", price);
        jsonObject.add("values", values);
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di inserimento di StopOrder
    private static JsonObject getInsertStopOrderRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci tipo (ask/bid): ");
        String type = input.readLine();
        ConsoleManager.print("> Inserisci quantità: ");
        double doubleSize = Double.parseDouble(input.readLine())*1000;
        ConsoleManager.print("> Inserisci prezzo: ");
        double doublePrice = Double.parseDouble(input.readLine())*1000;

        if (!validateOrder(doubleSize, doublePrice, type)) {
            return printError();
        }

        int size = (int) doubleSize;
        int price = (int) doublePrice;

        jsonObject.addProperty("operation", "insertStopOrder");
        JsonObject values = new JsonObject();
        values.addProperty("type", type);
        values.addProperty("size", size);
        values.addProperty("price", price);
        jsonObject.add("values", values);
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di inserimento di MarketOrder
    private static JsonObject getInsertMarketOrderRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci tipo (ask/bid): ");
        String type = input.readLine();
        ConsoleManager.print("> Inserisci quantità: ");
        double doubleSize = Double.parseDouble(input.readLine())*1000;
        
        if (!validateOrder(doubleSize, 1, type)) {
            return printError();
        }

        int size = (int) doubleSize;
        
        jsonObject.addProperty("operation", "insertMarketOrder");
        JsonObject values = new JsonObject();
        values.addProperty("type", type);
        values.addProperty("size", size);
        jsonObject.add("values", values);
        return jsonObject;
    }


    // Metodo per la gestione delle richieste di cancellazione di ordini
    private static JsonObject getCancelOrderRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci ID ordine: ");
        int orderId = Integer.parseInt(input.readLine());
        
        if (orderId <= 0) {
            ConsoleManager.print("> Errore: ID ordine non valido.");
            return notLoggedError();
        }
        
        jsonObject.addProperty("operation", "cancelOrder");
        JsonObject values = new JsonObject();
        values.addProperty("orderId", orderId);
        jsonObject.add("values", values);
        return jsonObject;
    }


    // Metodo per la gestione delle richieste di ottenimento dello storico dei prezzi
    private static JsonObject getPriceHistoryRequest() throws IOException {
        JsonObject jsonObject = new JsonObject();
        ConsoleManager.print("> Inserisci mese e anno (MMYYYY): ");
        String MMYYYY = input.readLine();
        
        jsonObject.addProperty("operation", "getPriceHistory");
        JsonObject values = new JsonObject();
        values.addProperty("month", MMYYYY);
        jsonObject.add("values", values);
        return jsonObject;
    }

    // Metodo per la gestione delle richieste di ottenimento del prezzo corrente
    private static JsonObject getCurrentPriceRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("operation", "getCurrentPrice");
        jsonObject.add("values", new JsonObject());
        return jsonObject;
    }



    // Metodo per la stampa delle operazioni disponibili
    private static void printHelp(){
        if (isLogged) {
            ConsoleManager.print("> Operazioni disponibili: login, register, updateCredentials, getCurrentPrice, exit, logout, insertLimitOrder, insertMarketOrder, insertStopOrder, cancelOrder, getPriceHistory");
            return;
        }
        ConsoleManager.print("> Operazioni disponibili: login, register, updateCredentials, getCurrentPrice, exit");
    }

    // Metodo per la gestione delle richieste
    private static JsonObject getRequest() throws IOException {
        ConsoleManager.print("> Inserisci operazione (help per vedere le operazioni disponibili): ");
        String operation = input.readLine();

        switch (operation) {
            case "login":
                return getLoginRequest();
            case "register":
                return getRegisterRequest();
            case "logout":
                return isLogged ? getLogoutRequest() : notLoggedError();
            case "updateCredentials":
                return getUpdateCredentialsRequest();
            case "getCurrentPrice":
                return getCurrentPriceRequest();
            case "insertLimitOrder":
                return isLogged ? getInsertLimitOrderRequest() : notLoggedError();
            case "insertMarketOrder":
                return isLogged ? getInsertMarketOrderRequest() : notLoggedError();
            case "insertStopOrder":
                return isLogged ? getInsertStopOrderRequest() : notLoggedError();
            case "cancelOrder":
                return isLogged ? getCancelOrderRequest() : notLoggedError();
            case "getPriceHistory":
                return isLogged ? getPriceHistoryRequest() : notLoggedError();
            case "exit":
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("operation", "exit");
                return jsonObject;
            case "help":
                printHelp();
                return getRequest();
            default:
                jsonObject = new JsonObject();
                jsonObject.addProperty("operation", "error");
                return jsonObject;
        }
    }


    // Metodo per chiudere la connessione
    public void shutdown() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                ConsoleManager.print("> Connessione TCP chiusa.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void run() {
        output.println(UDP_PORT); //invio la porta UDP al server
        while (running && !socket.isClosed()) {
            try {
                JsonObject request = getRequest();
                if (request.get("operation").getAsString().equals("error") || request == null) {
                    if(request == null){
                        ConsoleManager.print("> Errore: Operazione non valida.");
                        continue;
                    }
                    continue;
                }
                if (request.get("operation").getAsString().equals("exit")) {
                    shutdown();
                    System.exit(0);
                    break;
                }
                sendRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
