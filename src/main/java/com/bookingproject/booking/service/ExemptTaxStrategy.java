package com.bookingproject.booking.service;

public class ExemptTaxStrategy implements TaxStrategy{
    @Override
    public double applyTax(double amount){
        return amount;
    }
}
