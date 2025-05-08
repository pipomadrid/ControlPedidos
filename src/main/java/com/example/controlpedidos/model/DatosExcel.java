package com.example.controlpedidos.model;

public class DatosExcel {


    private int valorOrdered;
    private int valorReserved;

    public DatosExcel(int valorOrdered, int valorReserved) {
        this.valorOrdered = valorOrdered;
        this.valorReserved = valorReserved;
    }

    public void sumar(int v1, int v2) {
        this.valorOrdered += v1;
        this.valorReserved += v2;
    }

    public int getValorOrdered() {
        return valorOrdered;
    }

    public void setValorOrdered(int valorOrdered) {
        this.valorOrdered = valorOrdered;
    }

    public int getValorReserved() {
        return valorReserved;
    }

    public void setValorReserved(int valorReserved) {
        this.valorReserved = valorReserved;
    }
}
