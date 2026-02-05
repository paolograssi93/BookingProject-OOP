# 🏨 Booking Management System - Progetto OOP

Sistema gestionale per strutture ricettive sviluppato in **Java** con **Spring Boot**. Il progetto implementa un'architettura flessibile per la gestione di proprietà complesse, check-in/out e fatturazione automatica basata su logiche reali di business.

---

## 📂 Documentazione Allegata
Nella cartella `/documentation` sono disponibili i seguenti file:
- **UseCases.pdf**: Analisi dei requisiti e casi d'uso del sistema.
- **Progetto_Diagrammi.drawio**: Sorgente editabile dei diagrammi (Composite, Strategy, Use Case).

---

## 🏗️ Design Pattern Implementati

Il software è stato progettato seguendo i principi **S.O.L.I.D.** e applicando i seguenti design pattern per garantire scalabilità e manutenibilità:

### 1. Composite Pattern
Gestisce la gerarchia della proprietà in modo ad albero.
- Permette di trattare una singola `Room` o l'intera `PropertyStructure` (Villa) tramite la stessa interfaccia `BookingComponent`.
- **Calcolo Ricorsivo:** Il prezzo totale della struttura viene calcolato attraversando l'albero dei componenti in modo trasparente.

### 2. Strategy Pattern
Centralizza la logica di tassazione variabile.
- Le classi `StandardVATStrategy` (IVA 10%) e `ExemptTaxStrategy` permettono di cambiare il calcolo fiscale a runtime.
- **Vantaggio:** Il `BookingService` non conosce i dettagli del calcolo, ma delega alla strategia selezionata.

### 3. Factory Pattern
Utilizzato nella classe `BookingFactory` per disaccoppiare la creazione degli oggetti dal Controller.
- Astrae l'istanziazione di `ROOM`, `EXTRA` e `VILLA`, rendendo il sistema facilmente estendibile con nuovi tipi di componenti.

### 4. Iterator Pattern
Essenziale per la gestione dinamica della Villa.
- Utilizzato per scorrere i componenti durante la fatturazione e **rimuovere in modo sicuro** gli `ExtraService` già consumati, evitando errori.

### 5. DAO Pattern (Data Access Object)
Gestisce la persistenza dei dati.
- L'interfaccia `IBookingDAO` e la classe `FileBookingDAO` si occupano di salvare check-in e fatture su file di testo (`.txt`), separando la logica di business dallo storage.

---

## 🛠️ Funzionalità Avanzate

### ✅ Gestione dello Stato (Occupato/Libero)
Ogni componente gestisce il proprio stato di disponibilità. Il sistema è "state-aware": impedisce check-in su stanze occupate o fatturazioni su stanze già libere.

### ✅ Fatturazione Dinamica e Pulizia automatica
L'endpoint `/calculate-details` implementa una logica di business raffinata:
- **Filtro Occupazione:** Nella fattura totale della Villa vengono inclusi **solo** i componenti effettivamente occupati o i servizi usufruiti.
- **Auto-Cleanup:** Una volta emessa la fattura, il sistema libera automaticamente le stanze e rimuove definitivamente i servizi extra dalla proprietà.

### ✅ Checkout Integrato
L'operazione di `/checkout` esegue una transazione completa: genera la fattura, la archivia tramite il DAO e resetta lo stato della stanza per renderla nuovamente disponibile.

---

## 💻 Guida all'Utilizzo (Endpoint API)

### 🔹 Configurazione (Admin)
`POST /api/bookings/add-component?type=ROOM&price=150`
- **Header richiesto:** `Role: Admin`

### 🔹 Monitoraggio e Operazioni
- **Status Struttura:** `GET /api/bookings/rooms-status` (Mostra l'albero della villa e lo stato di ogni stanza).
- **Check-in:** `POST /api/bookings/checkin?roomIndex=1` (Richiede oggetto Guest nel Body).
- **Check-out:** `POST /api/bookings/checkout?roomIndex=1&isExempt=false` (Libera la stanza e genera fattura).

### 🔹 Fatturazione Totale
- **Villa:** `GET /api/bookings/calculate-details?target=VILLA&isExempt=false`

---

## 🛡️ Robustezza e Sicurezza
- **Gestione Eccezioni:** Implementati blocchi `try-catch` e validazioni per prevenire indici errati, input non validi o strutture non inizializzate.
- **Autorizzazione:** Controllo granulare dei ruoli (Admin/User) tramite header nelle chiamate critiche.

---

## 🧪 Testing e Mocking (JUnit 5 & Mockito)

Il progetto include una suite di test unitari per garantire l'affidabilità della logica di business, utilizzando **Mockito** per isolare le dipendenze.

### Strategia di Testing:
- **Mocking del DAO:** Durante i test di fatturazione, il `FileBookingDAO` viene mockato per evitare la scrittura reale su disco, verificando però che il metodo `saveInvoice()` venga effettivamente chiamato con i dati corretti.
- **Isolamento del Service:** Testiamo il `BookingController` simulando le risposte del `BookingService`, permettendo di verificare la gestione degli errori (es. Check-in fallito) in modo deterministico.
- **Esempio di Test:**
    - Verifica che il calcolo del prezzo totale con `StandardVATStrategy` applichi correttamente il +10%.
    - Verifica che la rimozione dell' `ExtraService` avvenga solo dopo la generazione della fattura.

**Vantaggio:** Questo approccio garantisce che i test siano veloci, indipendenti dall'ambiente esterno e focalizzati esclusivamente sulla logica dell'algoritmo.

---

**Sviluppatore:** Paolo Grassi  
**Esame:** Object Oriented Programming (OOP)