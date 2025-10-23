
/*
 * ServerListener class per la gestione della ricezione dei messaggi dal server
 */
package cross.client.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class ServerListener implements Runnable {
    private DatagramSocket socketUDP; // riferimento alla Socket UDP
    private byte[] buffer = new byte[4096]; // buffer per la lettura dei messaggi UDP
    private SocketTCP socketTCP; // riferimento alla Socket TCP
    private BufferedReader input; // buffer per la lettura dei messaggi TCP dal server
    private static final Gson gson = new Gson(); // Gson per la serializzazione/deserializzazione degli oggetti
    private static volatile boolean running = true; // flag per terminare il thread

    public ServerListener(SocketTCP socketTCP, DatagramSocket socketUDP) {
        this.socketUDP = socketUDP;
        this.socketTCP = socketTCP;
        try {
            this.input = new BufferedReader(new InputStreamReader(socketTCP.getSocket().getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per gestire le risposte dal server
    private synchronized void HandleResponse(JsonObject jsonResponse) { // Gestione delle risposte utente del server
        if (jsonResponse.has("errorMessage")){
             ConsoleManager.print("> [TCP] " + jsonResponse.get("errorMessage").getAsString());
            return;
        }
        if (jsonResponse.has("orderId")) { // Ho ricevuto l'ID dell'ordine
            int orderId = jsonResponse.get("orderId").getAsInt();
            if(orderId != -1) ConsoleManager.print("> [TCP] Ordine inserito con successo. ID: " + orderId);
            else ConsoleManager.print("> [TCP] Errore nell'inserimento dell'ordine.");
            return;
        }
        if (jsonResponse.has("price")){  // Ho ricevuto il prezzo attuale
            ConsoleManager.print("> [TCP] Prezzo attuale: " + jsonResponse.get("price").getAsInt());
            return;
        }
        if (jsonResponse.has("dailyData")) { // Ho ricevuto dati giornalieri
            JsonArray dailyData = jsonResponse.get("dailyData").getAsJsonArray();
            JsonElement first = dailyData.get(0);
            String data = first.getAsJsonObject().get("date").getAsString();
        
            if (data.equals("N/A")) {
                ConsoleManager.print("> Il mese scelto non è presente nello storico.");
            } else {
                ConsoleManager.print("> [TCP] Dati giornalieri ricevuti:");
                for (JsonElement element : dailyData) {
                    JsonObject day = element.getAsJsonObject();
                    ConsoleManager.print("  - Data: " + day.get("date").getAsString() +
                        " | Min: " + day.get("minPrice").getAsInt() +
                        " | Max: " + day.get("maxPrice").getAsInt() +
                        " | Open: " + day.get("openPrice").getAsInt() +
                        " | Close: " + day.get("closePrice").getAsInt());
                }
            }
            return;
        }
        if (jsonResponse.has("notification")) { // Ho ricevuto una notifica
            JsonArray closedTrades = jsonResponse.get("trades").getAsJsonArray();
            if (closedTrades.size() == 0) {
                ConsoleManager.print("> Nessun trade concluso.");
            } else {
                ConsoleManager.print("> [UDP] Trade effettuato con:");
                for (JsonElement element : closedTrades) {
                    JsonObject trade = element.getAsJsonObject();
                    ConsoleManager.print("  - ID: " + trade.get("orderId").getAsInt() +
                        " | Tipo: " + trade.get("type").getAsString() +
                        " | Tipo ordine: " + trade.get("orderType").getAsString() +
                        " | Prezzo: " + trade.get("price").getAsInt() +
                        " | Quantità: " + trade.get("size").getAsInt() +
                        " | Timestamp: " + trade.get("timestamp").getAsLong());
                }
            }
            return;
        }
        
    }

    // Metodo per terminare il thread
    public void shutdown() {
        running = false;
        socketTCP.shutdown();
        if (socketUDP != null && !socketUDP.isClosed()) {
            socketUDP.close();
            ConsoleManager.print("> Connessione UDP chiusa.");
        }
    }


    @Override
    public void run() {
        new Thread(this::listenTCP).start();
        new Thread(this::listenUDP).start();
    }


    // Thread per la ricezione dei messaggi TCP
    private void listenTCP() {
        try {
            while (running) {
                String response = input.readLine();
                if (response == null) {
                    ConsoleManager.print("> Connessione TCP chiusa dal server.");
                    shutdown();
                    System.exit(0);
                    break;
                }
    
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
    
                if (jsonResponse.has("response")) {
                    int responseCode = jsonResponse.get("response").getAsInt();
    
                    if (responseCode == 999) {
                        ConsoleManager.print("> Il server ha chiuso la connessione. Chiusura del client...");
                        shutdown();
                        System.exit(0);
                        break;
                    } 
                   if(socketTCP.isPendingLogin()){
                        if(responseCode == 100){
                            socketTCP.setLogged(true);
                            socketTCP.clearPending();
                        } else {
                            socketTCP.clearPending();
                        }
                     }  
                    if(socketTCP.isPendingLogout()){
                        if(responseCode == 100){
                            socketTCP.setLogged(false);
                            socketTCP.clearPending();
                        } else {
                            socketTCP.clearPending();
                        }
                   }
                }
                
                HandleResponse(jsonResponse);
            }
        } catch (IOException e) {
            ConsoleManager.print("> Errore di connessione TCP.");
        }
    }
    
    // Thread per la ricezione dei messaggi UDP
    private void listenUDP() {
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketUDP.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                JsonObject jsonResponse = gson.fromJson(message, JsonObject.class);
                HandleResponse(jsonResponse);
            }
        } catch (IOException e) {
            if (running) {
                ConsoleManager.print("> Errore di connessione UDP.");
                e.printStackTrace();
            }
        }
    }
}
