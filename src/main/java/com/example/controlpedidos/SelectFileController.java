package com.example.controlpedidos;

import com.example.controlpedidos.model.ControlPedidosBean;
import com.example.controlpedidos.model.ReservedBean;
import com.example.controlpedidos.model.SubsidiaryBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;

import java.awt.*;
import java.io.*;
import java.util.Optional;
import java.util.prefs.Preferences;

import static com.example.controlpedidos.ExcelUtilities.*;
import static com.example.controlpedidos.ExcelUtilities.cargaDatosExistentes;

public class SelectFileController {


    private final FileChooser fileChooser;
    private final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
    private File fileSub;
    private File fileRes;
    private File fileControl;
    @FXML
    private Button btnControl;
    @FXML
    private Button btnSub;

    @FXML
    private TextField textFieldSub;
    @FXML
    private TextField textFieldRes;
    @FXML
    private TextField textFieldControl = new TextField();
    @FXML
    private CheckBox chexBox;

    // Constructor clase
    public SelectFileController() {
        // Inicializar el FileChooser y configurar el filtro
        fileChooser = new FileChooser();

        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "Archivos de Excel", "*.xls", "*.xlsx");
        fileChooser.getExtensionFilters().add(filter);
    }

    @FXML
    public void initialize() {
        // Establecer el valor inicial del TextField
        String rutaControl = preferences.get("FileControl","");
        String rutaSub = preferences.get("FileSub","");
        textFieldControl.setText(rutaControl);
        textFieldSub.setText(rutaSub);

        if(!rutaSub.isEmpty() && !rutaControl.isEmpty() && existeArchivo(rutaControl) && existeArchivo(rutaSub)){
            chexBox.setSelected(false);
            btnSub.setDisable(true);
            textFieldSub.setEditable(false);
            fileControl = new File(textFieldControl.getText());
        }else{
            chexBox.setSelected(true);
            btnControl.setDisable(true);
            textFieldControl.setText("");
            textFieldSub.setText("");
            textFieldRes.setText("");
        }

    }

    // Boton archivo 1
    @FXML
    protected void onSelectFile1ButtonClick() {
        Stage stage = new Stage();
        fileSub = fileChooser.showOpenDialog(stage);
        // Verificar si se seleccionó un archivo
        if (fileSub != null) {
            textFieldSub.setText(fileSub.getAbsolutePath());
            if(!fileSub.getPath().contains("Subsiders")){
                mostrarAlerta("No es un archivo Subsidiary",Alert.AlertType.ERROR);
                textFieldSub.setText("");
            }else{
                preferences.put("FileSub",fileSub.getAbsolutePath());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }
    // Boton archivo 2
    @FXML
    protected void onSelectFile2ButtonClick() {
        Stage stage = new Stage();
        fileRes = fileChooser.showOpenDialog(stage);
        // Verificar si se seleccionó un archivo
        if (fileRes != null) {
            textFieldRes.setText(fileRes.getAbsolutePath());
            if(!fileRes.getPath().contains("Reserved")){
                mostrarAlerta("No es un archivo Reserved",Alert.AlertType.ERROR);
                textFieldRes.setText("");
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    // Boton archivo 3
    @FXML
    protected void onSelectFile3ButtonClick() {
        Stage stage = new Stage();
        fileControl = fileChooser.showOpenDialog(stage);
        // Verificar si se seleccionó un archivo
        if (fileControl != null) {
            textFieldControl.setText(fileControl.getAbsolutePath());
            if(!fileControl.getPath().contains("Control_pedidos")){
                mostrarAlerta("No es un archivo de Control de Pedidos",Alert.AlertType.ERROR);
                textFieldControl.setText("");
            }else{
                preferences.put("FileControl",fileControl.getAbsolutePath());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }


    // Check
    @FXML
    protected void onSelectCheck() {
        Stage stage = new Stage();
        if(chexBox.isSelected()){
            Optional<ButtonType> resultado = mostrarAlerta("¿Estas segura?",Alert.AlertType.CONFIRMATION);
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                textFieldControl.setText("");
                btnControl.setDisable(true);
                btnSub.setDisable(false);
                textFieldSub.setText("");
                textFieldRes.setText("");
            }else {
                chexBox.setSelected(false);
            }
        }else {
            textFieldControl.setText(preferences.get("FileControl", ""));
            textFieldSub.setText(preferences.get("FileSub",""));
            btnControl.setDisable(false);
            btnSub.setDisable(true);
        }
    }

    // Boton ejecutar
    @FXML
    protected void procesarFicheros(){
        if(chexBox.isSelected()){
            // TODO validar formulario
            ReservedBean reservedBean = new ReservedBean();
            SubsidiaryBean subsidiaryBean = new SubsidiaryBean();
            reservedBean.setFecha(obtenerFechaReserved(reservedBean.getFileReserved()));
            // Crea Excel Control Pedidos Inicial con Cabeceras
            ControlPedidosBean controlPedidosBean = crearArchivoDestinoNuevo(reservedBean);
            subsidiaryBean.setMapSubsidiary(procesarFicheroSubsidiary(subsidiaryBean));
            procesarFicheroReserved(reservedBean,subsidiaryBean.getMapSubsidiary());
            // Escribe los datos en el fichero
            cargaDatosExistentes(controlPedidosBean.getFileControlPedidos(), subsidiaryBean.getMapSubsidiary(),true);
            textFieldControl.setText(preferences.get("FileControl","Archivo"));
            mostrarAlerta("Archivo creado con éxito",Alert.AlertType.INFORMATION);
            chexBox.setSelected(false);
            btnControl.setDisable(false);
            btnSub.setDisable(true);
            fileControl = new File(textFieldControl.getText());
            abrirExcel(controlPedidosBean);
        }else {
            if (validarFormulario()) {
                ReservedBean reservedBean = new ReservedBean();
                SubsidiaryBean subsidiaryBean = new SubsidiaryBean();
                reservedBean.setFecha(obtenerFechaReserved(reservedBean.getFileReserved()));
                subsidiaryBean.setMapSubsidiary(procesarFicheroSubsidiary(subsidiaryBean));
                procesarFicheroReserved(reservedBean, subsidiaryBean.getMapSubsidiary());
                ControlPedidosBean controlPedidosBean = new ControlPedidosBean();
                if (fileControl != null) {
                    controlPedidosBean.setFileControlPedidos(fileControl);
                    crearCabecera(fileControl, reservedBean);
                    cargaDatosExistentes(controlPedidosBean.getFileControlPedidos(), subsidiaryBean.getMapSubsidiary(), false);
                    mostrarAlerta("Archivo modificado con éxito", Alert.AlertType.INFORMATION);
                    abrirExcel(controlPedidosBean);
                } else {
                    mostrarAlerta("No hay ningún archivo de Control seleccionado", Alert.AlertType.ERROR);
                    textFieldControl.setText("");
                }
            }
        }

    }

    // Abre el libro de Excel de Control de pedidos
    private static void abrirExcel(ControlPedidosBean controlPedidosBean) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(controlPedidosBean.getFileControlPedidos());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("El entorno de escritorio no es compatible.");
        }
    }


    // Validar el tipo de valor de cada celda para obtener el resultado
    private String validarTipoCelda(Cell cell,int colIndex){
        String valorCelda="";
        String espacio =" ";
        if(colIndex == 1){
            espacio="\n";
        }

        switch (cell.getCellType()) {
            case STRING:
                valorCelda = cell.getStringCellValue() + espacio;
                break;
            case NUMERIC:
                valorCelda = cell.getNumericCellValue() + espacio;
                break;
            case FORMULA:
                valorCelda = cell.getCellFormula() + espacio;
                break;
        }
        return valorCelda;
    }


    public Optional<ButtonType> mostrarAlerta(String mensaje,Alert.AlertType alertType) {
        Alert alerta = new Alert(alertType);
        alerta.setTitle("Aviso");
        alerta.setHeaderText(mensaje);
        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado;
    }

    private boolean existeArchivo(String ruta){
        File file = new File(ruta);
        return file.exists();
    }

    private boolean validarFormulario(){
        String error = "";
        if(!existeArchivo(textFieldRes.getText()) && !textFieldRes.getText().contains("Reserved")){
            error = "Archivo Reserved no válido \n";
        }
        if(!existeArchivo(textFieldSub.getText()) && !textFieldSub.getText().contains("Subsiders")){
            error += "Archivo Subsidiary no válido \n";
        }
        if(!existeArchivo(textFieldControl.getText()) && !textFieldControl.getText().contains("Control_pedidos")){
            error += "Archivo Control de pedidos no válido \n";
        }
        // TODO Comparar fechas Reserved por si ya se ha realizado antes la operación con el mismo archivo
        if(!error.isEmpty()){
            mostrarAlerta(error,Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

}