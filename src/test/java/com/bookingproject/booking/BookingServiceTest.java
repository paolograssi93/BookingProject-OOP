package com.bookingproject.booking;


import com.bookingproject.booking.model.BookingFactory;
import com.bookingproject.booking.model.Guest;
import com.bookingproject.booking.model.PropertyStructure;
import com.bookingproject.booking.model.Room;
import com.bookingproject.booking.persistence.IBookingDAO;
import com.bookingproject.booking.service.BookingService;
import com.bookingproject.booking.service.ExemptTaxStrategy;
import com.bookingproject.booking.service.StandardVATStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    // --- TEST SENZA MOCKITO SUL CALCOLO ---
    @Test
    void testCalculateFinalPrice_Correctness(){
        PropertyStructure villa = new PropertyStructure();
        villa.addComponent(BookingFactory.createComponent("ROOM", 100.0));
        villa.addComponent(BookingFactory.createComponent("ROOM", 200.0));

        //Imposto la strategia con IVA al +10%
        bookingService.setTaxStrategy(new StandardVATStrategy());

        double result = bookingService.calculateFinalPrice(villa);

        //Verifico che il totale sia 330.0 euro
        assertEquals(330.0, result, "Il calcolo del prezzo con IVA 10% non è corretto");
    }

    //--- TEST CON MOCKITO ---
    @Mock
    private IBookingDAO mokDao; //creo un finto DAO

    @InjectMocks
    private BookingService bookingService; //Inietto il finto DAO nel servizio

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this); //Inizializzo il Mock
    }

    @Test
    void testProcessCheckIn_Success(){
        //GIVEN: genero un ospite valido che ha occupato una Room
        Guest validGuest = new Guest("Paolo", "Grassi", "A12345GD");
        Room room = new Room(100.0);

        //WHEN: processo il checkin
        bookingService.processCheckIn(validGuest, mokDao, room);

        //THEN: Verifico che il DAO sia stato richiamato solo una volta
        verify(mokDao, times(1)).saveCheckIn(validGuest);
    }

    @Test
    void testProcessCheckIn_InvalidDocument(){
        //GIVEN: Genero un Guest con Documento non valido (troppo corto) che ha occupato una Room
        Guest invalidGuest = new Guest("Paolo", "Grassi", "123");
        Room room = new Room(100.0);

        //WHEN & THEN: Verifico che venga lanciata un'eccezione
        assertThrows(RuntimeException.class, ()->{
            bookingService.processCheckIn(invalidGuest, mokDao, room);
        });

        //Verifico che il DAO non sia mai stato chiamato per un ospite non valido
        verify(mokDao, never()).saveCheckIn(any());
    }
}
