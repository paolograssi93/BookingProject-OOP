package com.bookingproject.booking.service;

public class StandardVATStrategy implements TaxStrategy{
    @Override
    public double applyTax(double amount){
        return amount * 1.10;
    }
}
