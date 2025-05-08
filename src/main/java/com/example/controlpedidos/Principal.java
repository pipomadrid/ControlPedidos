package com.example.controlpedidos;

import com.example.controlpedidos.model.ControlPedidosBean;
import com.example.controlpedidos.model.ReservedBean;
import com.example.controlpedidos.model.SubsidiaryBean;

import static com.example.controlpedidos.ExcelUtilities.*;

public class Principal {

    public static void main(String[] args) {

        ReservedBean reservedBean = new ReservedBean();
        SubsidiaryBean subsidiaryBean = new SubsidiaryBean();
        reservedBean.setFecha(obtenerFechaReserved(reservedBean.getFileReserved()));

        // Crea Excel Control Pedidos Inicial con Cabeceras
        ControlPedidosBean controlPedidosBean = crearArchivoDestinoNuevo(reservedBean);

        subsidiaryBean.setMapSubsidiary(procesarFicheroSubsidiary(subsidiaryBean));
        procesarFicheroReserved(reservedBean,subsidiaryBean.getMapSubsidiary());

        // Crea un nuevo Map con los Part Code y las cantidades correspondientes tomadas de Reserved
      //  compararYEstablecerCantidad(subsidiaryBean.getMapSubsidiary(),reservedBean.getMapReserved());

        // Escribe los datos en el fichero
        cargaDatosExistentes(controlPedidosBean.getFileControlPedidos(), subsidiaryBean.getMapSubsidiary(),true);
        System.out.println(subsidiaryBean.getMapSubsidiary().toString());
        

    }
}
