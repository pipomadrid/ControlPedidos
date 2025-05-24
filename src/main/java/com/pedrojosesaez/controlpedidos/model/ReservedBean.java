package com.pedrojosesaez.controlpedidos.model;

import java.io.File;
import java.util.Map;

public class ReservedBean {

    File fileReserved;
    int columnaPart;
    int columnaCantidad;
    int filaInicial;
    Map<String,Integer> mapReserved;
    String fecha;

    public ReservedBean(File fileReserved, int columnaPart, int columnaCantidad, int filaInicial) {
        this.fileReserved = fileReserved;
        this.columnaPart = columnaPart;
        this.columnaCantidad = columnaCantidad;
        this.filaInicial = filaInicial;
    }

    public ReservedBean( String  file) {
        this.fileReserved = new File(file);
        this.columnaPart = 10;
        this.columnaCantidad = 18;
        this.filaInicial = 19;
    }


    public File getFileReserved() {
        return fileReserved;
    }

    public void setFileReserved(File fileReserved) {
        this.fileReserved = fileReserved;
    }

    public int getColumnaPart() {
        return columnaPart;
    }

    public void setColumnaPart(int columnaPart) {
        this.columnaPart = columnaPart;
    }

    public int getColumnaCantidad() {
        return columnaCantidad;
    }

    public void setColumnaCantidad(int columnaCantidad) {
        this.columnaCantidad = columnaCantidad;
    }

    public int getFilaInicial() {
        return filaInicial;
    }

    public void setFilaInicial(int filaInicial) {
        this.filaInicial = filaInicial;
    }

    public Map<String, Integer> getMapReserved() {
        return mapReserved;
    }

    public void setMapReserved(Map<String, Integer> mapReserved) {
        this.mapReserved = mapReserved;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }


}
