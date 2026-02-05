package com.bookingproject.booking.model;

public class Room implements BookingComponent {
    private double price;
    private boolean available = true; //Stato della camera, inizialmente libera

    public Room(double price){

        this.price = price;
    }

    @Override
    public double getPrice(){

        return this.price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
