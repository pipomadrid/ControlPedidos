package com.pedrojosesaez.controlpedidos.model;

import java.io.File;
import java.util.Map;

public class SubsidiaryBean {

    Map<String,DatosExcel> mapSubsidiary;
    int filaInicio;
    int columnaInicio;



    int columnaOrderedQty;
    String fecha;
    File fileSubsidiary;

    public SubsidiaryBean(File fileSubsidiary, int filaInicio, int columnaInicio,int columnaOrderedQty) {
        this.filaInicio = filaInicio;
        this.columnaInicio = columnaInicio;
        this.fileSubsidiary = fileSubsidiary;
        this.columnaOrderedQty = columnaOrderedQty;
    }

    public SubsidiaryBean() {
        this.filaInicio = 20;
        this.columnaInicio = 1;
        this.fileSubsidiary = new File("C:\\Users\\PedroJoseSaezSanchez\\Documents\\Sara\\CS0536 Subsiders sales orders status.cleaned.xlsx");
        this.columnaOrderedQty = 19;
    }


    public int getColumnaInicio() {
        return columnaInicio;
    }

    public void setColumnaInicio(int columnaInicio) {
        this.columnaInicio = columnaInicio;
    }

    public int getFilaInicio() {
        return filaInicio;
    }

    public void setFilaInicio(int filaInicio) {
        this.filaInicio = filaInicio;
    }

    public Map<String, DatosExcel> getMapSubsidiary() {
        return mapSubsidiary;
    }

    public void setMapSubsidiary(Map<String, DatosExcel> mapSubsidiary) {
        this.mapSubsidiary = mapSubsidiary;
    }

    public File getFileSubsidiary() {
        return fileSubsidiary;
    }

    public void setFileSubsidiary(File fileSubsidiary) {
        this.fileSubsidiary = fileSubsidiary;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    public int getColumnaOrderedQty() {
        return columnaOrderedQty;
    }

    public void setColumnaOrderedQty(int columnaOrderedQty) {
        this.columnaOrderedQty = columnaOrderedQty;
    }


}
