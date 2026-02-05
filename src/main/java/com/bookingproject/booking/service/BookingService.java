package com.bookingproject.booking.service;

import com.bookingproject.booking.exception.InvalidInputException;
import com.bookingproject.booking.model.*;
import com.bookingproject.booking.persistence.IBookingDAO;
import org.springframework.stereotype.Service;


@Service
public class BookingService {

    //Default: IVA 10%
    private TaxStrategy taxStrategy = new StandardVATStrategy();

    //Requisito Strategy: permetto di cambiare la logica a runtime
    public void setTaxStrategy(TaxStrategy taxStrategy){

        this.taxStrategy = taxStrategy;
    }

    //Setto la stanza come occupata dopo il checkin
    public void occupyComponent(BookingComponent component){
        if(component instanceof Room) {
            Room room = (Room) component;
            if (!room.isAvailable()) {
                throw new InvalidInputException("La stanza è già occupata. Checkin già effettuato.");
            }
            room.setAvailable(false);
        } else if (component instanceof  PropertyStructure) {
            PropertyStructure structure = (PropertyStructure) component;
            //Occupo tutti i figli della struttura per rendere tutto occupato
            for (BookingComponent child : structure.getComponents()) {
                occupyComponent(child);
            }
        }
        }

    //Processo di Check-out
    public void releaseComponent(BookingComponent component){
        if(component instanceof Room){
            Room room = (Room) component;
            if(room.isAvailable()){
                throw new InvalidInputException("Nessun checkin effettuato per la stanza selezionata.");
            }
            room.setAvailable(true);
        } else if (component instanceof PropertyStructure) {
            PropertyStructure structure = (PropertyStructure) component;
            for (BookingComponent child : structure.getComponents()) {
                releaseComponent(child);
            }
        }
    }

    //UC-004: Processo di check-in con validazione I-001
    public void processCheckIn(Guest g, IBookingDAO dao, BookingComponent component){
        validate(g); //Exception Shielding
        occupyComponent(component);
        dao.saveCheckIn(g);
        System.out.println("Check-in completato con successo per: " + g.getName());
    }


    //Verifica del documento (Regex semplificata)
    public void validate(Guest g){
        if(g.getDocumentID() == null || !g.getDocumentID().matches("^[A-Z0-9]{5,10}$")){
            throw new InvalidInputException("Formato documento non valido o mancante!");
        }
    }

    //UC-005: Calcolo totale basato sul Composite + IVA 10%
    public double calculateFinalPrice(BookingComponent component){
        double basePrice = component.getPrice();
        return taxStrategy.applyTax(basePrice); //Delego il calcolo alla strategia corrente
    }

    //UC-000 Controllo Ruoli
    public boolean isAuthorized(String userRole, String requiredRole){
        return userRole != null && userRole.equalsIgnoreCase(requiredRole);
    }
}
