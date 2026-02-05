package com.bookingproject.booking.model;

public class Guest {
    //attributi privati per l'incapsulamento
    private String name;
    private String surname;
    private String documentID;
    private String role; //"ADMIN" o "GUEST"

    //costruttore vuoto
    public Guest(){}

    //COSTRUTTORE CON PARAMETRI PER TEST:
    public Guest(String name, String surname, String documentID){
        this.name = name;
        this.surname = surname;
        this.documentID = documentID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDocumentID() {
        return documentID;
    }
    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
