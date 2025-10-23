# CROSS_Lab3

CROSS è un servizio di Order Book Client-Server per lo scambio BTC/USD. Gestisce Limit, Market e Stop Order, implementa l'algoritmo di matching Price/Time e utilizza TCP (persistente), UDP (notifiche) e RMI/Multicast UDP (V.O.). Sviluppato in Java, assicura la persistenza dei dati e la notifica asincrona degli scambi.

## Descrizione Progetto

**CROSS** (exChange oRder bOokS Service) è un progetto del Nuovo Ordinamento (N.O.), sviluppato per il corso di Reti e Laboratorio (A.A. 2024/25). Il progetto implementa un Order Book Client-Server per la gestione degli scambi BTC/USD su un exchange centralizzato, con focus su:

- Architettura di rete e comunicazione
- Gestione multithreaded del Server
- Algoritmo di matching Price/Time
- Persistenza dei dati e notifiche asincrone

## Funzionalità di Base

### 1. Gestione Utenti (Client)
- `register <username> <password>`: Registrazione di un nuovo utente  
- `login <username> <password>`: Accesso al servizio  
- `logout`: Disconnessione dal servizio  
- `updateCredentials <nuova_password>`: Aggiornamento della password  

### 2. Gestione Ordini e Order Book (Server)
- **Limit Order**: Ordini inseriti nell'Order Book, eseguiti al prezzo limite o migliore  
- **Market Order**: Ordini eseguiti immediatamente al miglior prezzo disponibile  
- **Stop Order**: Ordini che diventano Market Order al raggiungimento della Stop Price  
- `cancelOrder <orderID>`: Annullamento di ordini non ancora evasi  

### 3. Algoritmo di Matching
Il Server usa la **Price/Time Priority**:
- Miglior prezzo → priorità massima  
- In caso di parità di prezzo → FIFO (ordine di inserimento)  

### 4. Dati Storici e Persistenza
- `getPriceHistory <mese>`: Richiesta dei dati storici aggregati (apertura, chiusura, massimo, minimo)  
- Il Server salva periodicamente utenti e ordini su file JSON  

## Architettura Tecnica e Comunicazione

### Stack Tecnologico
- Linguaggio: Java  
- Server: Multithreaded con Thread Pool  
- Dati: JSON (richieste, risposte, notifiche)  

### Protocolli
| Fase            | Protocollo      | Dettagli |
|-----------------|----------------|----------|
| Registrazione   | TCP             | Connessione iniziale per registrarsi |
| Transazioni     | TCP Persistente | Tutte le operazioni CRUD sugli ordini |
| Notifiche       | UDP             | Notifiche asincrone sui trade completati |

## Istruzioni per Compilazione ed Esecuzione

### Prerequisiti
- Java Development Kit (JDK) 17+  

### Configurazione
Parametri letti da file di configurazione separati:
- `config/server_config.txt`  
- `config/client_config.txt`  

### Compilazione
```bash
# Compilazione dei sorgenti Java e creazione dei JAR
javac -d bin src/**/*.java
jar cf Server.jar -C bin/ .
jar cf Client.jar -C bin/ .
