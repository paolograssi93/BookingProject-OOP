package com.bookingproject.booking.model;

public class BookingFactory {
    public static BookingComponent createComponent(String type, double price){
        switch (type.toUpperCase()){
            case "ROOM":
                return new Room(price);

            case "VILLA":
                PropertyStructure subVilla = new PropertyStructure();
                if(price > 0) {
                    subVilla.addComponent(new Room(price));
                }
                return subVilla;

            case "EXTRA":
                return new ExtraService("Servizio Extra", price);

            default:
                throw new IllegalArgumentException("Tipo non supportato");
        }
    }
}
