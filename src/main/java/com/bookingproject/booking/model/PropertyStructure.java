package com.bookingproject.booking.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PropertyStructure implements BookingComponent, Iterable<BookingComponent>{
    //Lista di componenti che possono essere strutture o stanze o servizi extra
    private List<BookingComponent> components = new ArrayList<>();

    public void addComponent(BookingComponent component){
        components.add(component);
    }

    public List<BookingComponent> getComponents(){
        return this.components;
    }

    @Override
    public double getPrice() {
        return components.stream()
                .mapToDouble(BookingComponent::getPrice)
                .sum();
    }

    @Override
    public Iterator<BookingComponent> iterator(){
        return components.iterator();
    }
}
