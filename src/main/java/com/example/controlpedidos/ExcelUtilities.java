package com.example.controlpedidos;

import com.example.controlpedidos.model.ControlPedidosBean;
import com.example.controlpedidos.model.DatosExcel;
import com.example.controlpedidos.model.ReservedBean;
import com.example.controlpedidos.model.SubsidiaryBean;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase de utilidades para manejar datos de excel
 */
class ExcelUtilities {

    // --------------------------------- CABECERAS---------------------------

    /**
     * Crea cabecera de excel con datos iniciales(part code y Qty de momento)
     * @param destinySheet : Hoja de destino Control de pedidos
     * @param reservedBean : Bean del excel Reserved
     */
    protected static void crearCabeceraInicial(Sheet destinySheet, ReservedBean reservedBean ) {

            Row rowOrigin = destinySheet.getRow(0);
            if (rowOrigin == null) {
                rowOrigin = destinySheet.createRow(0);
            }
            Cell cellPart = rowOrigin.getCell(0);
            if (cellPart == null) {
                cellPart = rowOrigin.createCell(0);
            }
            cellPart.setCellStyle(crearEstiloCelda(destinySheet));
            cellPart.setCellValue("Part Code");

            Cell cellOrdered = rowOrigin.getCell(1);
            if (cellOrdered == null) {
                cellOrdered = rowOrigin.createCell(1);
            }
            cellOrdered.setCellStyle(crearEstiloCelda(destinySheet));
            cellOrdered.setCellValue("Ordered Quantity");

            Cell cellTotales = rowOrigin.getCell(2);
            if (cellTotales == null) {
                cellTotales = rowOrigin.createCell(2);
            }
            cellTotales.setCellStyle(crearEstiloCelda(destinySheet));
            cellTotales.setCellValue("Remaining Quantity");

            Cell cellReserved = rowOrigin.getCell(3);
            if (cellReserved == null) {
                cellReserved = rowOrigin.createCell(3);
            }
            cellReserved.setCellStyle(crearEstiloCelda(destinySheet));
            cellReserved.setCellValue("Reserved Quantity " + reservedBean.getFecha());
        }

    /**
     * Crear cabecera de archivo de control existente
     * @param file : Archivo de Control de pedidos existente
     * @param reservedBean :Bean del excel Reserved
     */
    protected static void crearCabecera(File file, ReservedBean reservedBean) {
        // Crea nueva hoja dentro del libro excel
        Sheet sheet1 = abrirHoja(file);
        int numCelda = 3;
        Row row = sheet1.getRow(0);
        boolean isCeldaConDatos = true;
        while (isCeldaConDatos) {
            if (row.getCell(numCelda) != null) {
                numCelda++;
            } else {
                Cell cellReserved2 = row.createCell(numCelda);
                cellReserved2.setCellStyle(crearEstiloCelda(sheet1));
                cellReserved2.setCellValue("Reserved Quantity " + reservedBean.getFecha());
                isCeldaConDatos = false;
            }
        }

        cerrarLibro(file,sheet1);
    }


    // --------------------------------- CARGA DE DATOS EN ARCHIVO---------------------------

    /**
     * Metodo que carga el excel de control con los datos existentes en el Map de Subsidiary, si es la primera vez carga los datos de Subsidiary
     *  si no carga los del reserved
     * @param fileControlPedidos  : Archivo de destino de Control de pedidos
     * @param mapSubsidiary : Map de datos de Subsidiary
     */
    protected static void cargaDatosExistentes(File fileControlPedidos,Map<String,DatosExcel> mapSubsidiary,boolean isPrimeraVez){
        // Abrimos Hoja del excel destino para añadir cabecera
        Sheet destinySheet = abrirHoja(fileControlPedidos);
        //Añadimos los datos del Map al excel de Control de pedidos
        int rowNum = 1; // Empezamos en la segunda fila (índice 1) para los datos
        for (Map.Entry<String, DatosExcel> entry : mapSubsidiary.entrySet()) {
            if(!entry.getKey().isBlank()) {
                XSSFCellStyle estilo = crearEstiloCeldasNormales(destinySheet);
                if(isPrimeraVez) {
                    Row row = destinySheet.createRow(rowNum++);

                    Cell celdaClave = row.createCell(0);// Columna de claves
                    celdaClave.setCellStyle(estilo);
                    celdaClave.setCellValue(entry.getKey());

                    Cell celdaOrdered = row.createCell(1);  // Columna de Ordered
                    celdaOrdered.setCellStyle(estilo);
                    celdaOrdered.setCellValue(entry.getValue().getValorOrdered());

                    Cell celdaReserved = row.createCell(3);  // Columna de Reserved
                    celdaReserved.setCellStyle(estilo);
                    celdaReserved.setCellValue(entry.getValue().getValorReserved());
                }else{
                    boolean isCeldaConValor = true;
                    int i = 0;
                    while(isCeldaConValor){
                        if(destinySheet.getRow(rowNum).getCell(i) != null){
                            i++;
                        }else{
                            Cell celdaReserved2 = destinySheet.getRow(rowNum++).createCell(i);  // Columna de Reserved
                            celdaReserved2.setCellStyle(estilo);
                            celdaReserved2.setCellValue(entry.getValue().getValorReserved());
                            isCeldaConValor = false;
                        }
                    }
                }
            }
        }
        if(isPrimeraVez){
            crearColumnaCalculoTotales(destinySheet);
        }
        // Guardar y cerrar el libro
      cerrarLibro(fileControlPedidos,destinySheet);
    }


    private static void crearColumnaCalculoTotales(Sheet sheet){
        // Estilo con fondo amarillo
        CellStyle yellowStyle = sheet.getWorkbook().createCellStyle();
        Font fuenteNegrita = sheet.getWorkbook().createFont();
        yellowStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellowStyle.setBorderTop(BorderStyle.THIN);
        yellowStyle.setBorderBottom(BorderStyle.THIN);
        yellowStyle.setBorderLeft(BorderStyle.THIN);
        yellowStyle.setBorderRight(BorderStyle.THIN);
        fuenteNegrita.setBold(true);
        yellowStyle.setFont(fuenteNegrita);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Salta encabezado si es necesario

            int rowIndex = row.getRowNum() + 1; // Excel usa índices 1-based (A1, B2...)

            String formula = String.format("B%d - SUM(D%d:T%d)", rowIndex, rowIndex, rowIndex);

            Cell formulaCell = row.createCell(2);
            formulaCell.setCellFormula(formula);
            formulaCell.setCellStyle(yellowStyle); // Aplica fondo amarillo
        }
    }
    // --------------------------------- PROCESAR ARCHIVOS ---------------------------
    /**
     * Crea archivo excel y hoja y devuelve objeto ControlPedidosBean
     * @param reservedBean : Bean de datos del excel Reserved
     * @return ControlPedidosBean : Bean de datos del excel Control de pedidos
     */
    protected static ControlPedidosBean crearArchivoDestinoNuevo(ReservedBean reservedBean) {
        final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        // Formato para nombres de archivo
        DateTimeFormatter formatoArchivo = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        // Formatear la fecha y hora
        String fechaHoraFormateada = fechaHoraActual.format(formatoArchivo);
        String rutaDocumentos = System.getProperty("user.home") + File.separator + "Documents";
        String rutaArchivo = rutaDocumentos + File.separator + "Control_pedidos_" + fechaHoraFormateada + ".xlsx";
        File file = new File(rutaArchivo);
        preferences.put("FileControl",file.getAbsolutePath());
        ControlPedidosBean controlPedidosBean = new ControlPedidosBean(file,fechaHoraFormateada);
        //preferences.put("File",file3.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook();
        // Crea nueva hoja dentro del libro excel
        Sheet sheet1 = workbook.createSheet("HojaNueva");
        crearCabeceraInicial(sheet1,reservedBean);

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            // Escribir el workbook en un archivo
            workbook.write(fileOut);
            System.out.println("Archivo Excel creado exitosamente en: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al crear el archivo Excel: " + e.getMessage());
        } finally {
            try {
                // Cerrar el workbook para liberar recursos
                workbook.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el workbook: " + e.getMessage());
            }
        }
        return controlPedidosBean;
    }


    /**
     *  Procesa el fichero Reserved y devuelve un Map con los Part code y las Qty sumadas para cada Part code
     * @param reservedBean: Bean de datos del excel Reserved
     *
     */
    protected static void procesarFicheroReserved( ReservedBean reservedBean, Map<String,DatosExcel> mapSubsidiary){

        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHoja(reservedBean.getFileReserved());

        //Recorremos las columnas Part y Qty y damos valor al Map
        for (int i = reservedBean.getFilaInicial(); i <= originSheet.getLastRowNum(); i++) {
            Row rowOrigin = originSheet.getRow(i);
            Cell cellPart = rowOrigin.getCell(reservedBean.getColumnaPart());
            Cell cellCantidad= rowOrigin.getCell(reservedBean.getColumnaCantidad());
            if (cellPart != null && cellCantidad !=null) {
                agregarOSumar(mapSubsidiary,cellPart.getStringCellValue().trim(),0,(int)cellCantidad.getNumericCellValue());
            }
        }
    }

    /**
     *  Procesa el fichero Subsidiary y devuelve una Lista de los Part Code sin duplicados
     * @param subsidiaryBean: Bean de datos del excel subsidiary
     * @return Map
     */
    protected static Map<String, DatosExcel> procesarFicheroSubsidiary(SubsidiaryBean subsidiaryBean){
        Map<String,DatosExcel> partCodeYCantidad =new TreeMap<>();
        final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
        String ultimaClave = null;
        preferences.put("FileSub",subsidiaryBean.getFileSubsidiary().getAbsolutePath());
        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHoja(subsidiaryBean.getFileSubsidiary());
        //Recorremos las columnas Part y Qty y damos valor al Map
        for (int i = subsidiaryBean.getFilaInicio(); i <= originSheet.getLastRowNum(); i++) {
            Row rowOrigin = originSheet.getRow(i);
            Cell cellPart = rowOrigin.getCell(subsidiaryBean.getColumnaInicio());
            Cell cellOrderedQty = rowOrigin.getCell(subsidiaryBean.getColumnaOrderedQty());
            if(cellPart!=null) {
                String clave = cellPart.getStringCellValue().trim();
                if(!clave.isEmpty()){
                    ultimaClave = clave;
                }
                agregarOSumar(partCodeYCantidad, ultimaClave, (int) cellOrderedQty.getNumericCellValue(), 0);
            }
        }
        return partCodeYCantidad;
    }

    // Obtiene la fecha del archivo Reserved
    protected static String obtenerFechaReserved(File fileReserved){
        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHoja(fileReserved);
        Row rowFecha = originSheet.getRow(13);
        Cell cellFecha = rowFecha.getCell(1);
        String text = cellFecha.getStringCellValue();

        // Expresión regular para encontrar la fecha (YYYY-MM-DD)
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.out.println("No se encontró una fecha en el texto.");
            return "";
        }
    }


    // --------------------------------- METODOS AUXIALIARES ---------------------------

    // Crea libro y abre hoja de excel
    private static Sheet abrirHoja(File file) {
        Sheet sheet;
        try (InputStream mainstream = new FileInputStream(file)) {
            Workbook wb = WorkbookFactory.create(mainstream);
            sheet = wb.getSheetAt(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sheet;
    }

    // Guardar y cerrar libro de excel a partir de una hoja
    private static void cerrarLibro(File file, Sheet sheet1){
        Workbook workbook = sheet1.getWorkbook();
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            // Escribir el workbook en un archivo
            workbook.write(fileOut);
            System.out.println("Archivo Excel creado exitosamente en: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al crear el archivo Excel: " + e.getMessage());
        } finally {
            try {
                // Cerrar el workbook para liberar recursos
                workbook.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el workbook: " + e.getMessage());
            }
        }
    }

    /**
     *  Método para agregar y sumar valores si la clave ya existe
     * @param mapa : Map de pares clave(part Code) y datosExcel
     * @param clave : Clave(partCode)
     * @param valorOrdered : Valor de columna Ordered procedente de Subsidiary
     * @param valorReserved : Valor de columna Reserved procedente de Reserved
     */
    private static void agregarOSumar(Map<String, DatosExcel> mapa, String clave, int valorOrdered, int valorReserved) {
        if (mapa.containsKey(clave)) {
            mapa.get(clave).sumar(valorOrdered, valorReserved);
        } else {
            mapa.put(clave, new DatosExcel(valorOrdered, valorReserved));
        }
    }


// --------------------------------- ESTILOS CELDAS---------------------------

    // Método para copiar el estilo de una celda (sin el error de estilo)
    private static void copiarEstiloCelda(Workbook workbookDestino, Cell celdaOrigen, Cell celdaDestino) {
        CellStyle estiloOrigen = celdaOrigen.getCellStyle();

        // Crear un nuevo estilo en el workbook de destino
        CellStyle estiloDestino = workbookDestino.createCellStyle();

        // Clonar el estilo de la celda de origen
        estiloDestino.cloneStyleFrom(estiloOrigen);

        // Aplicar el estilo a la celda destino
        celdaDestino.setCellStyle(estiloDestino);
    }
    private static XSSFCellStyle crearEstiloCeldasNormales(Sheet destinySheet){
        XSSFCellStyle style = (XSSFCellStyle) destinySheet.getWorkbook().createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static XSSFCellStyle crearEstiloCelda(Sheet destinySheet){
        XSSFCellStyle style = (XSSFCellStyle) destinySheet.getWorkbook().createCellStyle();
        // Crear color desde Hexadecimal
        String hex = "#008080"; // Aquí tu color HEX
        java.awt.Color awtColor = java.awt.Color.decode(hex);
        byte[] rgb = new byte[] {
                (byte) awtColor.getRed(),
                (byte) awtColor.getGreen(),
                (byte) awtColor.getBlue()
        };

        XSSFColor color = new XSSFColor(rgb, null); // null para usar default color space
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // --- Cambiar color de texto ---
        XSSFFont font = (XSSFFont) destinySheet.getWorkbook().createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);

        // Aplicar la fuente al estilo de la celda
        style.setFont(font);

        return style;
    }

    /**
     * Método que compara los Part Code los dos Map, en el caso de que sean iguales se da valor al Map Subsidiary con el del Reserved
     * @param mapSubsidiary : Map de los datos obtenidos del Excel Subsidiary
     * @param mapReserved : Map de los datos obtenidos del Excel Reserved
     */
    protected static void compararYEstablecerCantidad(Map<String, Integer> mapSubsidiary, Map<String, Integer> mapReserved) {
        for(String clave: mapSubsidiary.keySet()){
            if(mapReserved.containsKey(clave)){
                mapSubsidiary.put(clave, mapReserved.get(clave));
            }
        }
    }

}
