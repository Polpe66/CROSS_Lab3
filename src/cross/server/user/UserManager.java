/*
 * Classe per gestire gli user e le loro operazioni
 */
package cross.server.user;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final ConcurrentHashMap<String, User> users; // Mappa di tutti gli utenti associati al proprio username 
    private final ConcurrentHashMap<String, Boolean> onlineUsers; // Mappa di tutti gli utenti online associati al proprio username
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Gson per la serializzazione/deserializzazione degli oggetti
    private final String path = "files/users.json"; // Percorso del file JSON contenente gli utenti

    public UserManager() {
        users = new ConcurrentHashMap<>();
        this.onlineUsers = new ConcurrentHashMap<>();
        caricaUsers();
    }

    // Carica gli utenti dal file JSON
    public void caricaUsers() {
        File FILE = new File(path);
        if (!FILE.exists()) {
            return;
        }
        try (Reader reader = new FileReader(FILE)) {
            JsonElement jsonElement = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("users");
            jsonArray.forEach(jsonElement1 -> {
                User user = gson.fromJson(jsonElement1, User.class);
                users.put(user.getUsername(), user);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Salva gli utenti nel file JSON
    public void saveUsers() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        users.values().forEach(user -> {
            jsonArray.add(gson.toJsonTree(user));
        });
        jsonObject.add("users", jsonArray);        
        
        try (Writer writer = new FileWriter(path)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Registra un nuovo utente
    public int register(String username, String password) {
        if (password == null || password.length() < 8) {
            return 101; // Password troppo corta
        }
        if (users.containsKey(username)) {
            return 102; // Username già esistente
        }

        users.put(username, new User(username, password));
        return 100; // Registrazione avvenuta con successo
    }


    // Effettua il login
    public int login(String username, String password) {
        User user = users.get(username);
        if (user == null || !user.checkPassword(password)) {
            return 101;
        }
        return onlineUsers.putIfAbsent(username, true) == null ? 100 : 102;
    }
    
    // Effettua il logout
    public int logout(String username) {
        return onlineUsers.remove(username) != null ? 100 : 101;
    }
    
    // Aggiorna le credenziali di un utente
    public int updateCredentials(String username, String oldPassword, String newPassword) {
        try {
            // 1. Controllo esistenza utente
            User user = users.get(username);
            if (user == null) {
                return 102; // Utente non esistente
            }

            // 2. Verifica password vecchia
            if (!user.checkPassword(oldPassword)) {
                return 102; // Password errata
            }

            // 3. Verifica se utente è loggato
            if (onlineUsers.containsKey(username)) {
                return 104; // Utente loggato
            }

            // 4. Verifica nuova password valida
            if (newPassword.length() < 8) {
                return 101; // Password troppo corta
            }

            // 5. Verifica se la nuova password è diversa
            if (oldPassword.equals(newPassword)) {
                return 103; // Password uguale alla vecchia
            }

            // 6. Tutto ok: aggiorno
            user.setPassword(newPassword);
            return 100;

        } catch (Exception e) {
            e.printStackTrace();
            return 105; // Other error cases
        }
    }


    // funzione di chiusura
    public void close() {
        saveUsers();
        users.clear();  
    }
}
