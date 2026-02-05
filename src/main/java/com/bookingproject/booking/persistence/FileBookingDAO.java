package com.bookingproject.booking.persistence;

import com.bookingproject.booking.model.Guest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Repository
public class FileBookingDAO implements IBookingDAO{

    //Imposto il Logger con SLF4J
    private static final Logger logger = LoggerFactory.getLogger(FileBookingDAO.class);

    private final String CHECKIN_FILE = "checkins.txt"; //file in cui registro gli utenti
    private final String INVOICE_PATH = "invoices/"; //percorso per il salvataggio del file invoice

    @Override
    public void saveCheckIn(Guest g){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHECKIN_FILE, true))){
            writer.write(g.getName() + "," + g.getSurname() + "," + g.getDocumentID());
            writer.newLine();
            logger.info("Check-in salvato correttamente per l'ospite {}", g.getDocumentID());
        }catch (IOException e){
            logger.error("Errore durante il salvataggio del check-in: ", e);
        }
    }

    @Override
    public void saveInvoice(String data){
        File directory = new File(INVOICE_PATH);
        if (!directory.exists()){
            boolean created = directory.mkdir();
            if(created) logger.info("Cartella invoices creata con successo!");
        }

        String fileName = INVOICE_PATH + "Fattura_" + System.currentTimeMillis() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
            writer.write(data);
            logger.info("Fattura generata correttamente: ", fileName);
        } catch (IOException e) {
            logger.error("Fallimento generazione fattura I/O: ", e);
            throw new RuntimeException("Errore persistenza: ", e);
        }
    }

    @Override
    public List<Guest> findAllCheckins(){
        //Metodo per recuperare la lista dal file
        return new ArrayList<>();
    }
}
