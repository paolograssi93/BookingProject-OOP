package com.bookingproject.booking.persistence;

import com.bookingproject.booking.model.Guest;

import java.util.List;

public interface IBookingDAO {
    void saveCheckIn(Guest g); //per salvare l'utente nel file checins.txt
    void saveInvoice(String data); //per salvare la fattura
    List<Guest> findAllCheckins(); //per visualizzare i log
}
