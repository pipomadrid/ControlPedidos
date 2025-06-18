package com.pedrojosesaez.controlpedidos.model;

import java.io.File;
import java.util.Map;

public class SubsidiaryBean {

    Map<String,DatosExcel> mapSubsidiary;
    int filaInicio;
    int columnaInicio;
    int filaFecha;



    int columnaOrderedQty;
    String fecha;
    File fileSubsidiary;

    public SubsidiaryBean(File fileSubsidiary, int filaInicio, int columnaInicio,int columnaOrderedQty,int filaFecha) {
        this.filaInicio = filaInicio;
        this.columnaInicio = columnaInicio;
        this.fileSubsidiary = fileSubsidiary;
        this.columnaOrderedQty = columnaOrderedQty;
        this.filaFecha = filaFecha;
    }

    public SubsidiaryBean(String file) {
        if(file.contains("cleaned")){
            this.filaInicio = 20;
            this.filaFecha = 14;
        }else{
            this.filaInicio = 18;
            this.filaFecha = 12;
        }
        this.columnaInicio = 1;
        this.columnaOrderedQty = 19;
        this.fileSubsidiary = new File(file);

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

    public int getFilaFecha() {
        return filaFecha;
    }

    public void setFilaFecha(int filaFecha) {
        this.filaFecha = filaFecha;
    }



}
