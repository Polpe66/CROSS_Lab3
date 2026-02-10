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

# Comandi CLI 
register <username> <password>  
login <username> <password>  
logout  
updateCredentials <nuova_password>  
insertLimitOrder <ask/bid> <size> <price>  
insertMarketOrder <ask/bid> <size>  
insertStopOrder <ask/bid> <size> <stopPrice>  
cancelOrder <orderID>  
getCurrentPrice  
getPriceHistory <mese>  
help  
exit   

# Autore
Francesco Polperio, Matricola 635406
Corso: Laboratorio 3
Anno Accademico: 2024/25

---

EN Version
# CROSS_Lab3

**CROSS** is a Client–Server Order Book service for **BTC/USD** trading. It supports **Limit**, **Market**, and **Stop** orders, implements **Price/Time** matching, and uses **TCP** (persistent), **UDP** (notifications), and **RMI/UDP Multicast**. Developed in **Java**, it ensures **data persistence** and **asynchronous trade notifications**.

## Project Description

CROSS implements a Client–Server Order Book to manage BTC/USD trades on a centralized exchange, focusing on:

- Network architecture and communication
- Multithreaded Server and Client management
- Price/Time matching algorithm
- Data persistence and asynchronous notifications

## Implementation Choices

- Rejected **Stop Orders** generate notifications with **quantity** and **price** set to **-1**.
- In case of **timeout** or **Server termination**, the Client receives code **999** and shuts down.
- The Client communicates its **UDP port** to the Server to enable efficient notifications.

## Main Server Classes

- **ServerMain.java**: starts the Server and manages connections  
- **handler.ConnectionHandler.java**: handles Client requests in dedicated threads  
- **handler.StoricoOrdiniHandler.java**: order history and JSON file persistence  
- **order.Order** + **LimitOrder**, **MarketOrder**, **StopOrder**: order management  
- **orderbook.Orderbook.java**: main data structure, order matching, notifications  
- **orderbook.BuyStopOrder / SellStopOrder**: manages stop orders in separate threads  
- **user.User / UserManager**: user registration and authentication  
- **utilities.NotificationSender.java**, **Trade.java**: notification sending and trade handling  

## Main Client Classes

- **ClientMain.java**: starts the Client  
- **utilities.ConsoleManager.java**: synchronizes console access  
- **utilities.ServerListener.java**: handles TCP/UDP messages from the Server  
- **utilities.SocketTCP.java**: TCP communication logic (login, logout, orders)  

## Threading

### Server

- **Client connection threads**: one dedicated thread per Client
- **Data saving thread**: periodically saves users and orders to JSON files
- **StopOrder threads**: monitor activation conditions for stop orders

### Client

- **Connection management thread**: sends/receives requests
- **UDP notification listener thread**: receives asynchronous notifications

## Thread Pools

- **FixedThreadPool**: StopOrder and Client management  
- **CachedThreadPool**: Server connection handling  
- **ScheduledThreadPool**: periodic tasks for order history and users  

## Data Structures and Persistence

- **ConcurrentHashMap** for users and online status (thread-safe)
- **ConcurrentLinkedQueue** and **ConcurrentSkipListMap** for the Order Book (price/time priority)
- **ConcurrentLinkedDeque** for trade history

### JSON persistence files

- `users.json`
- `Orderbook.json`
- `buyStopOrder.json`
- `sellStopOrder.json`
- `storicoOrdini.json`

## Technical Architecture and Communication

| Phase          | Protocol          | Details |
|----------------|-------------------|---------|
| Registration   | TCP               | Initial connection to register |
| Transactions   | Persistent TCP     | All CRUD operations on orders |
| Notifications  | UDP               | Asynchronous notifications for completed trades |

## Build and Run Instructions

### Configuration

Parameters are read from separate files:

- `config/server_config.txt`
- `config/client_config.txt`

### Server
java -jar Server.jar


Client
java -jar Client.jar

Typical Client Interaction
Connection started with server 127.0.0.1 on port 8080
Enter operation (help to see the available operations):
help
Available operations: login, register, updateCredentials, getCurrentPrice, exit
login
Enter username: fra
Enter password: ********
insertLimitOrder <ask/bid>

CLI Commands

register

login

logout

updateCredentials <new_password>

insertLimitOrder <ask/bid>

insertMarketOrder <ask/bid>

insertStopOrder <ask/bid>

cancelOrder

getCurrentPrice

getPriceHistory

help

exit

Author

Francesco Polperio
Student ID: 635406
Course: Laboratory 3
Academic Year: 2024/25

