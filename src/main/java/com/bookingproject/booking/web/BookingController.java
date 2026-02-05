package com.bookingproject.booking.web;

import com.bookingproject.booking.exception.InvalidInputException;
import com.bookingproject.booking.model.*;
import com.bookingproject.booking.persistence.FileBookingDAO;
import com.bookingproject.booking.persistence.IBookingDAO;
import com.bookingproject.booking.service.BookingService;
import com.bookingproject.booking.service.ExemptTaxStrategy;
import com.bookingproject.booking.service.StandardVATStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/bookings") //percorso per le chiamate
public class BookingController {

    private final BookingService bookingService;
    private final IBookingDAO fileDao;

    private PropertyStructure villa;

    public BookingController(BookingService bookingService, FileBookingDAO fileDao){
        this.bookingService = bookingService;
        this.fileDao = fileDao;
        initData();
    }

    private void initData(){
        this.villa = (PropertyStructure) BookingFactory.createComponent("VILLA", 0);

        //aggiungo le stanze di test
        //villa.addComponent(BookingFactory.createComponent("ROOM", 120.0));
        //villa.addComponent(BookingFactory.createComponent("ROOM", 180.0));
        //villa.addComponent(BookingFactory.createComponent("ROOM", 200.0));

        //aggiungo servizi extra di test
        //villa.addComponent((BookingFactory.createComponent("EXTRA", 25.0)));
    }

    @PostMapping("/add-component")
    public ResponseEntity<String> addComponent(
            @RequestParam String type,
            @RequestParam double price,
            @RequestHeader("Role") String role){

        //Effettuo un controllo se il type inserito non è quello accettato (ROOM, VILLA, EXTRA)
        if(!"ROOM".equalsIgnoreCase(type) && !"EXTRA".equalsIgnoreCase(type) && !"VILLA".equalsIgnoreCase(type)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipologia " + type + " non accettata");
        }

        //verifico se il post viene da un admin
        if(!bookingService.isAuthorized(role, "Admin")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato. Utente non Admin");
        }

        //Utilizzo il factory per creare il componente inserito in type
        BookingComponent newComponent = BookingFactory.createComponent(type, price);
        villa.addComponent(newComponent); //aggiungo il componente alla villa
        return ResponseEntity.ok(type.toUpperCase() + " aggiunto con successo alla proprietà");
    }

    //AREA CHECKIN:
    //Viene richiesto un body con i dati del cliente
    // {
    //  "name": "Paolo",
    //  "surname": "Rossi",
    //  "documentID": "AB12345"
    //}
    //e un header con il numero della stanza a cui si deve effettuare il checkin
    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestBody Guest guest, @RequestParam int roomIndex){
        try {
            if (this.villa.getComponents().isEmpty()) {
                return ResponseEntity.ok("Nessun elemento configurato. Utilizza add-component");
            }

            //per rendere reale l'app, l'indice 0 lo considero come stanza 1. La reception inserirà stanza 1
            roomIndex -= 1;

            //Prelevo i componenti della villa
            List<BookingComponent> components = villa.getComponents();
            if(roomIndex < 0 || roomIndex >= components.size()){
                //se l'indice inserito non rientra nella dimensione del components restituisco un errore
                return ResponseEntity.badRequest().body("Indice stanza non valido");
            }

            BookingComponent selected = villa.getComponents().get(roomIndex); //assegno a selected l'elemento presente nell'indice inserito

            bookingService.validate(guest); //valido i dati inseriti per guest
            bookingService.occupyComponent(selected); //occupo la stanza selezionata
            fileDao.saveCheckIn(guest); //salvo i dati del cliente nel file checkins.txt

            return ResponseEntity.ok("Check-in effettuato e registrato su file checkins.txt!");
        }catch (InvalidInputException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore: " + e.getMessage());
        }
    }

    //AREA CHECKOUT
    //effettuo il checkout del guest: genero prima la fattura della stanza, poi rilascio imposto la camera come DISPONIBILE
    @PostMapping("/checkout")
    public ResponseEntity<String> checkOut(@RequestParam int roomIndex, @RequestParam boolean isExempt) {
        try {
            //verifico se ci sono elementi configurati nella struttura
            if (this.villa.getComponents().isEmpty()) {
                return ResponseEntity.ok("Nessun elemento configurato. Utilizza add-component");
            }

            roomIndex = roomIndex - 1;

            List<BookingComponent> components = villa.getComponents();

            // 1. Controllo validità indice
            if (roomIndex < 0 || roomIndex >= components.size()) {
                return ResponseEntity.badRequest().body("Indice stanza non valido.");
            }

            BookingComponent selected = villa.getComponents().get(roomIndex);

            //prima di liberare la stanza genero la fattura del cliente
            String invoice = calculateDetails("ROOM", roomIndex, isExempt);

            if(invoice.contains("Avviso:")){ //se la fattura contiene la stringa avviso, si è generato un errore
                return ResponseEntity.badRequest().body("Impossibile effettuare il checkout: " + invoice);
            }

            bookingService.releaseComponent(selected); //rilascio la stamza e la imposto su DISPONIBILE

            return ResponseEntity.ok("Check-out completato. La stanza numero " + (roomIndex + 1) + " è di nuovo disponibile.\n" +
                                        "FATTURA SALVATA NELLA CARTELLA INVOICES");
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (InvalidInputException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore imprevisto: " + e.getMessage());
        }
    }

    //STATO DELLE CAMERE
    //Sia il Guest che Admin possono vedere lo stato delle camere e prezzo (iva esclusa)
    @GetMapping("/rooms-status")
    public ResponseEntity<String> getRoomsStatus() {
        //verifico se ci sono componenti all'interno della villa
        if (this.villa.getComponents().isEmpty()) {
            return ResponseEntity.ok("Nessun elemento configurato. Utilizza add-component");
        }

        //Costruisco la stringa da ritornare successivamente
        StringBuilder statusReport = new StringBuilder("--- STATO ATTUALE DELLA VILLA ---\n");
        int[] counter = {1}; //counter utilizzato per il numero delle stanze
        printStatusRecursive(this.villa, statusReport, counter); //richiamo il metodo per creare il template di stampa

        return ResponseEntity.ok(statusReport.toString());
    }

    //METODO PER CREARE ALBERATURA DELLE CAMERE
    private void printStatusRecursive(BookingComponent component, StringBuilder statusReport, int[] counter){
        if(component instanceof Room){ //verifico se il componente è una camera
            Room r = (Room) component;
            String state = r.isAvailable() ? "DISPONIBILE" : "OCCUPATA"; //verifico lo stato delle camere e la assegno in modo condizionale

            statusReport.append("|- Stanza ").append(counter[0]).append(" ") //aggiungo la stanza rilevata
                    .append(state)
                    .append(" - Prezzo: ").append(r.getPrice()).append("€\n");
            counter[0]++;
        }else if(component instanceof PropertyStructure){ //verifico se il componente è una Villa
            PropertyStructure ps = (PropertyStructure) component;

            if(ps != this.villa){
                statusReport.append("+DEPANDANCE (Prezzo totale: ")
                        .append(ps.getPrice()).append("€)\n").append("  ");
            }

            for(BookingComponent child : ps.getComponents()){
                printStatusRecursive(child, statusReport, counter);
            }
        } else if(component instanceof ExtraService){ //verifico se il componente è un servizio extra
            ExtraService extra = (ExtraService) component;
            statusReport.append("\n  L +SERVIZI EXTRA: (")
                        .append(extra.getPrice()).append("€)\n\n");
            }
    }

    //CALCOLO DELLA FATTURAZIONE
    //Permette di fatturare le stanze effettivamente occupate ed eventuali servizi extra
    //Posso impostare fattura massiva per tutte le stanze occupate o fattura per singola stanza
    @GetMapping("/calculate-details")
    public String calculateDetails(
            @RequestParam String target, //"VILLA" per calcolare il PropertStructure o "ROOM" per il figlio
            @RequestParam(defaultValue = "0") int index, //richiesto nel caso in cui il target è room
            @RequestParam boolean isExempt){

        if (this.villa.getComponents().isEmpty()) {
            return "Nessun elemento configurato. Utilizza add-component";
        }

        StringBuilder details = new StringBuilder("--- DETTAGLIO FATTURA TOTALE - Solo stanze occupate ad oggi ---\n");

        BookingComponent targetComponent = null;

        try {
            if ("VILLA".equalsIgnoreCase(target)) { //target VILLA: fatturo massivamente le stanze occupate e servizi extra
                targetComponent = this.villa;

                boolean hasOccupiedRooms = false; //controllo se ci sono stanze occupate
                int roomIndex = 1;
                double totalBase = 0; //definisco il valore iniziale del prezzo

                //uso l'iteratore java in quanto mi permette di utilizzare la funzione remove - utilizzata in caso di servizio extra
                //in fase di fatturazione eseguo il checkout e rimuovo i servizi extra già fatturati (per no fare doppia fattura)
                java.util.Iterator<BookingComponent> it = villa.getComponents().iterator();
                while(it.hasNext()) { //continuo fino all'ultimo elemento
                    BookingComponent component = it.next();
                    if (component instanceof Room) {
                        Room r = (Room) component;
                        if (!r.isAvailable()) {
                            details.append(String.format("- Stanza numero %d: %.2f €\n", roomIndex, r.getPrice()));
                            totalBase += r.getPrice(); //calcolo solo se la stanza è occupata
                            r.setAvailable(true); //Libero la stanza fatturata
                            hasOccupiedRooms = true;
                        }
                        roomIndex++;
                    } else if (component instanceof ExtraService) {
                        details.append(String.format("- Servizio Extra: %.2f €\n", component.getPrice()));
                        totalBase += component.getPrice();
                        hasOccupiedRooms = true;
                        it.remove(); //Rimuovo il servizio extra fatturato
                    }
                }

                //se non risultano esserci stanze occupate o servizi inseriti
                if(!hasOccupiedRooms){
                    return "AVVISO: Impossibile emettere la fattura. Non risultano esserci stanze occupate.";
                }

                // --- CALCOLO FINALE ---
                // Uso la strategia sulla somma filtrata
                bookingService.setTaxStrategy(isExempt ? new ExemptTaxStrategy() : new StandardVATStrategy());

                // Creo un componente temporaneo o passo il totale alla strategia
                double finalPrice = isExempt ? totalBase : totalBase * 1.10;

                details.append("-----------------\n");
                details.append(String.format("Totale Base (Solo occupate): %.2f €\n", totalBase));
                details.append(String.format("Totale FINALE (%s): %.2f €\n", isExempt ? "Esente IVA" : "IVA Inclusa 10%", finalPrice));
                details.append(String.format("\nFATTURA SALVATA NELLA CARTELLA INVOICES"));
            } else if("ROOM".equalsIgnoreCase(target)) { //se il target è ROOM richiedo la fatturazione della singola stanza index

                List<BookingComponent> components = villa.getComponents();

                int realIndex = index - 1;

                if (realIndex < 0 || realIndex >= components.size()) {
                    throw new IllegalArgumentException("Indice stanza non valido. Ci sono " + components.size() + " stanze configurate");
                }

                targetComponent = villa.getComponents().get(realIndex);

                //verifico se la stanza è occupata prima di emettere la fattura
                if (targetComponent instanceof Room && ((Room) targetComponent).isAvailable()) {
                    return "AVVISO: la stanza " + index + " è attualemte libera. Effettuare prima il checkin";
                }

                //emetto la fattura per la stanza indicata
                details.append(String.format("- Fattura specifica per Stanza Numero %d: %.2f €\n", index, targetComponent.getPrice()));
            }
                fileDao.saveInvoice(details.toString()); //Salvo la fattura tramite DAO
                return details.toString();
        }catch (Exception e){
                return "Errore durante il calcolo: " + e.getMessage();
            }
    }
}