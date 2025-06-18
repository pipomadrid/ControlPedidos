package com.pedrojosesaez.controlpedidos.model;

import java.io.File;
import java.util.Map;

public class PackingListBean {

    File filePackingList;
    int columnaPart;
    int columnaCantidad;
    int filaInicial;
    Map<String,Integer> mapPackingList;
    String fecha;

    public PackingListBean(File filePackingList, int columnaPart, int columnaCantidad, int filaInicial) {
        this.filePackingList = filePackingList;
        this.columnaPart = columnaPart;
        this.columnaCantidad = columnaCantidad;
        this.filaInicial = filaInicial;
    }

    public PackingListBean(String  file) {
        this.filePackingList = new File(file);
        this.columnaPart = 3;
        this.columnaCantidad = 4;
        this.filaInicial = 20;
    }


    public File getFilePackingList() {
        return filePackingList;
    }

    public void setFileReserved(File fileReserved) {
        this.filePackingList = fileReserved;
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

    public Map<String, Integer> getMapPackingList() {
        return mapPackingList;
    }

    public void setMapPackingList(Map<String, Integer> mapPackingList) {
        this.mapPackingList = mapPackingList;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }


}
