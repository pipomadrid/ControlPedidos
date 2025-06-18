package com.pedrojosesaez.controlpedidos.model;

public class DatosExcel {


    private int valorOrdered;
    private int valorPackingList;

    public DatosExcel(int valorOrdered, int valorPackingList) {
        this.valorOrdered = valorOrdered;
        this.valorPackingList = valorPackingList;
    }

    public void sumar(int v1, int v2) {
        this.valorOrdered += v1;
        this.valorPackingList += v2;
    }

    public int getValorOrdered() {
        return valorOrdered;
    }

    public void setValorOrdered(int valorOrdered) {
        this.valorOrdered = valorOrdered;
    }

    public int getValorPackingList() {
        return valorPackingList;
    }

    public void setValorvalorPackingList(int valorReserved) {
        this.valorPackingList = valorReserved;
    }
}
