package com.bookingproject.booking.model;

public class ExtraService implements BookingComponent{
    private String serviceName;
    private double price;

    public ExtraService(String serviceName, double price){
        this.serviceName = serviceName;
        this.price = price;
    }

    @Override
    public double getPrice(){
        return this.price;
    }

    private String getServiceName(){
        return serviceName;
    }
}
