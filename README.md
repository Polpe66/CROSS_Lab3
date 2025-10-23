# CROSS_Lab3

CROSS è un servizio di Order Book Client-Server per lo scambio BTC/USD. Gestisce Limit, Market e Stop Order, implementa l'algoritmo di matching Price/Time e utilizza TCP (persistente), UDP (notifiche) e RMI/Multicast UDP. Sviluppato in Java, assicura la persistenza dei dati e la notifica asincrona degli scambi.

## Descrizione Progetto

CROSS implementa un Order Book Client-Server per la gestione degli scambi BTC/USD su un exchange centralizzato, con focus su:

- Architettura di rete e comunicazione
- Gestione multithreaded del Server e del Client
- Algoritmo di matching Price/Time
- Persistenza dei dati e notifiche asincrone

## Scelte Implementative

- Stop Order scartati generano notifiche con quantità e prezzo impostati a -1.
- In caso di timeout o terminazione del Server, il Client riceve codice 999 e si chiude.
- Il Client comunica la propria porta UDP al Server per notifiche efficienti.



### Principali Classi Server

- `ServerMain.java`: avvio Server e gestione connessioni  
- `handler.ConnectionHandler.java`: gestisce richieste Client in thread dedicati  
- `handler.StoricoOrdiniHandler.java`: storico ordini, persistenza su file JSON  
- `order.Order` + `LimitOrder`, `MarketOrder`, `StopOrder`: gestione ordini  
- `orderbook.Orderbook.java`: struttura dati principale, matching ordini, notifiche  
- `orderbook.BuyStopOrder` / `SellStopOrder`: gestione ordini stop in thread separati  
- `user.User` / `UserManager`: registrazione e autenticazione utenti  
- `utilities.NotificationSender.java`, `Trade.java`: invio notifiche e gestione trade

### Principali Classi Client

- `ClientMain.java`: avvio Client  
- `utilities.ConsoleManager.java`: sincronizzazione accesso console  
- `utilities.ServerListener.java`: gestione messaggi TCP/UDP dal Server  
- `utilities.SocketTCP.java`: logica comunicazione TCP, login, logout, ordini

## Threading

### Server

- **Thread per connessioni Client**: un thread dedicato per ogni Client  
- **Thread per salvataggio dati**: salva periodicamente utenti e ordini su file JSON  
- **Thread per StopOrder**: monitorano condizioni di attivazione degli ordini  

### Client

- **Thread di gestione connessione**: invio/ricezione richieste  
- **Thread di ascolto notifiche UDP**: ricezione notifiche asincrone

### Thread Pool

- `FixedThreadPool`: gestione StopOrder e Client  
- `CachedThreadPool`: gestione connessioni Server  
- `ScheduledThreadPool`: operazioni periodiche su storico ordini e utenti  

## Strutture Dati e Persistenza

- `ConcurrentHashMap` per utenti e stato online (thread-safe)  
- `ConcurrentLinkedQueue` e `ConcurrentSkipListMap` per Orderbook (priorità prezzo/tempo)  
- `ConcurrentLinkedDeque` per storico trade  
- File JSON per persistenza:
  - `users.json`, `Orderbook.json`, `buyStopOrder.json`, `sellStopOrder.json`, `storicoOrdini.json`

## Architettura Tecnica e Comunicazione

| Fase            | Protocollo      | Dettagli |
|-----------------|----------------|----------|
| Registrazione   | TCP             | Connessione iniziale per registrarsi |
| Transazioni     | TCP Persistente | Tutte le operazioni CRUD sugli ordini |
| Notifiche       | UDP             | Notifiche asincrone sui trade completati |

## Istruzioni Compilazione ed Esecuzione


### Configurazione
Parametri letti da file separati:

- `config/server_config.txt`  
- `config/client_config.txt`
- 
# Server
java -jar Server.jar

# Client
java -jar Client.jar

# Tipica interazione Client
> Connessione Avviata con il server 127.0.0.1 sulla porta 8080  
> Inserisci operazione (help per vedere le operazioni disponibili):  
help  
> Operazioni disponibili: login, register, updateCredentials, getCurrentPrice, exit  
> login  
> Inserisci username: fra  
> Inserisci password: ********  
> insertLimitOrder <ask/bid> <size> <price>  

# Comandi CLI Principali
register <username> <password>  
login <username> <password>  
insertLimitOrder <ask/bid> <size> <price>  
cancelOrder <orderID>  
getPriceHistory <mese>  
updateCredentials <nuova_password>  
exit  

# Autore
Francesco Polperio, Matricola 635406
Corso: Laboratorio 3
Anno Accademico: 2024/25


