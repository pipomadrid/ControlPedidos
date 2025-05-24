package com.pedrojosesaez.controlpedidos.model;

import java.io.File;

public class ControlPedidosBean {
    File fileControlPedidos;
    String fechaHora;

    public ControlPedidosBean(File fileControlPedidos, String fechaHora) {
        this.fileControlPedidos = fileControlPedidos;
        this.fechaHora = fechaHora;
    }

    public ControlPedidosBean() {
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public File getFileControlPedidos() {
        return fileControlPedidos;
    }

    public void setFileControlPedidos(File fileControlPedidos) {
        this.fileControlPedidos = fileControlPedidos;
    }





}
