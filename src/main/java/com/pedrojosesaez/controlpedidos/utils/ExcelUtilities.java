package com.pedrojosesaez.controlpedidos.utils;

import com.pedrojosesaez.controlpedidos.SelectFileController;
import com.pedrojosesaez.controlpedidos.model.ControlPedidosBean;
import com.pedrojosesaez.controlpedidos.model.DatosExcel;
import com.pedrojosesaez.controlpedidos.model.PackingListBean;
import com.pedrojosesaez.controlpedidos.model.SubsidiaryBean;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Clase de utilidades para manejar datos de excel
 */
public class ExcelUtilities {

    // --------------------------------- CABECERAS---------------------------

    /**
     * Crea cabecera de excel con datos iniciales(part code y Qty de momento)
     *
     * @param destinySheet : Hoja de destino Control de pedidos
     * @param packingListBean : Bean del excel Reserved
     */
    protected static void crearCabeceraInicial(Sheet destinySheet, PackingListBean packingListBean,SubsidiaryBean subsidiaryBean) {

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
        cellOrdered.setCellValue("Ordered Qty (Subsidiary " +  subsidiaryBean.getFecha() + ")");

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
        cellReserved.setCellValue("Packing List Quantity " + packingListBean.getFecha());
    }

    /**
     * Crear cabecera de archivo de control existente
     *
     * @param file         : Archivo de Control de pedidos existente
     * @param packingListBean :Bean del excel Reserved
     */
    public static void crearCabecera(File file, PackingListBean packingListBean) {
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
                cellReserved2.setCellValue("PackingList Quantity " + packingListBean.getFecha());
                isCeldaConDatos = false;
            }
        }

        cerrarLibro(file, sheet1);
    }


    // --------------------------------- CARGA DE DATOS EN ARCHIVO---------------------------

    /**
     * Metodo que carga el excel de control con los datos existentes en el Map de Subsidiary, si es la primera vez carga los datos de Subsidiary
     * si no carga los del Packing list
     *
     * @param fileControlPedidos : Archivo de destino de Control de pedidos
     * @param mapSubsidiary      : Map de datos de Subsidiary
     */
    public static void cargaDatosExistentes2(File fileControlPedidos, Map<String, DatosExcel> mapSubsidiary, boolean isPrimeraVez) {
        // Abrimos Hoja del excel destino para añadir cabecera
        Sheet destinySheet = abrirHoja(fileControlPedidos);
        // Obtenemos el mapa con los part code que no existen en Subsidiary pero si en Packing List para añadirlos al final del excel de Control
        Map<String, DatosExcel> mapNoEnSubsidiary = mapSubsidiary.entrySet().stream().filter(entry -> entry.getValue().getValorOrdered() == 0).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue, (e1, e2) -> e1,
                TreeMap::new));
        // Obtenemos el mapa con los part code que existen en Subsidiary pero si en Packing List para añadirlos al principio del excel de control
        Map<String, DatosExcel> mapEnSubsidiary = mapSubsidiary.entrySet().stream().filter(entry -> entry.getValue().getValorOrdered() > 0).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue, (e1, e2) -> e1,
                TreeMap::new));
        //Añadimos los datos del Map al excel de Control de pedidos la primera vez
        if (isPrimeraVez) {
            int rowNum = 1; // Empezamos en la segunda fila (índice 1) para los datos
            // Cargamos los datos que están presentes en Subsidiary primero
            rowNum = cargarMapaEnControl(mapEnSubsidiary, destinySheet, rowNum,3);
            // Cargamos los datos que no están presentes en Subsidiary al final
            cargarMapaEnControl(mapNoEnSubsidiary, destinySheet, rowNum,3);
        } else {
            int lastRow = destinySheet.getLastRowNum();
            int celdaVacia = obtenerPrimeraCeldaVacia(destinySheet.getRow(lastRow));
            // Cargamos en el Control los Part Code que no están en Subsidiary pero si en el Packing List a partir de la última fila con datos
            cargarMapaEnControl(mapNoEnSubsidiary, destinySheet, lastRow,celdaVacia);
            XSSFCellStyle estilo = crearEstiloCeldasNormales(destinySheet);
            // Cargamos el resto de valores del Packing List para el Part Code existente en el Control
            for (int rowNum = 1; rowNum <= destinySheet.getLastRowNum(); rowNum++) {
                Row row = destinySheet.getRow(rowNum);
                if (row != null) {
                    String valorCeldaPartCode = destinySheet.getRow(rowNum).getCell(0).getStringCellValue().trim();
                    int cantidadDePartCode=0;
                    if(mapSubsidiary.get(valorCeldaPartCode)!=null){
                        cantidadDePartCode = mapSubsidiary.get(valorCeldaPartCode).getValorPackingList();
                    }
                    Cell celdaPackinglist2 = destinySheet.getRow(rowNum).createCell(celdaVacia);  // Columna de Packing list
                    celdaPackinglist2.setCellStyle(estilo);
                    celdaPackinglist2.setCellValue(cantidadDePartCode);
                }
            }
        }
        crearColumnaCalculoTotales(destinySheet);

        // Guardar y cerrar el libro
        cerrarLibro(fileControlPedidos, destinySheet);

    }

    public static int obtenerPrimeraCeldaVacia(Row row) {
        int cellIndex = 0;

        while (true) {
            Cell cell = row.getCell(cellIndex);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return cellIndex; // Primera celda vacía
            }
            cellIndex++;
        }
    }

    private static int cargarMapaEnControl(Map<String, DatosExcel> mapa, Sheet destinySheet, int rowNum,int columnaPacking) {
        for (Map.Entry<String, DatosExcel> entry : mapa.entrySet()) {
            if (!entry.getKey().isBlank()) {
                XSSFCellStyle estilo = crearEstiloCeldasNormales(destinySheet);
                Row row = destinySheet.createRow(rowNum++);

                Cell celdaClave = row.createCell(0);// Columna de claves
                celdaClave.setCellStyle(estilo);
                celdaClave.setCellValue(entry.getKey());

                Cell celdaOrdered = row.createCell(1);  // Columna de Ordered
                celdaOrdered.setCellStyle(estilo);
                celdaOrdered.setCellValue(entry.getValue().getValorOrdered());

                Cell celdaPackinglist = row.createCell(columnaPacking);  // Columna del packing list
                celdaPackinglist.setCellStyle(estilo);
                celdaPackinglist.setCellValue(entry.getValue().getValorPackingList());

                // Si hay Packing List anteriores con celdas vacías les ponemos 0 y damos formato
                if(columnaPacking > 3){
                    for(int i = 3;i<columnaPacking;i++){
                        if(row.getCell(i)==null) {
                            Cell celdasAnterioresPackingList = row.createCell(i);
                            celdasAnterioresPackingList.setCellStyle(estilo);
                            celdasAnterioresPackingList.setCellValue(0);
                        }
                    }
                }
            }
        }
        return rowNum;
    }


    private static void crearColumnaCalculoTotales(Sheet sheet) {
        // Estilo con fondo amarillo
        CellStyle yellowStyle = sheet.getWorkbook().createCellStyle();
        Font fuenteNegrita = sheet.getWorkbook().createFont();
        yellowStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellowStyle.setAlignment(HorizontalAlignment.CENTER);
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
     *
     * @param packingListBean : Bean de datos del excel Reserved
     * @return ControlPedidosBean : Bean de datos del excel Control de pedidos
     */
    public static ControlPedidosBean crearArchivoDestinoNuevo(PackingListBean packingListBean,SubsidiaryBean subsidiaryBean) {
        final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        // Formato para nombres de archivo
        DateTimeFormatter formatoArchivo = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        // Formatear la fecha y hora
        String fechaHoraFormateada = fechaHoraActual.format(formatoArchivo);
        String rutaDocumentos = System.getProperty("user.home") + File.separator + "Documents";
        String rutaArchivo = rutaDocumentos + File.separator + "Control_pedidos_" + fechaHoraFormateada + ".xlsx";
        File file = new File(rutaArchivo);
        preferences.put("FileControl", file.getAbsolutePath());
        ControlPedidosBean controlPedidosBean = new ControlPedidosBean(file, fechaHoraFormateada);
        //preferences.put("File",file3.getAbsolutePath());
        Workbook workbook = new XSSFWorkbook();
        // Crea nueva hoja dentro del libro excel
        Sheet sheet1 = workbook.createSheet("Control Pedidos");
        crearCabeceraInicial(sheet1, packingListBean,subsidiaryBean);

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
     * Procesa el fichero Packing List y devuelve un Map con los Part code y las Qty sumadas para cada Part code
     *
     * @param packingListBean: Bean de datos del excel Packing List
     */
    public static void procesarFicheroPackingList(PackingListBean packingListBean, Map<String, DatosExcel> mapSubsidiary) {

        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHojaPackaging(packingListBean.getFilePackingList());
        Workbook workbook = originSheet.getWorkbook();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        //Recorremos las columnas Part y Qty y damos valor al Map
        for (int i = packingListBean.getFilaInicial(); i <= originSheet.getLastRowNum(); i++) {
            Row rowOrigin = originSheet.getRow(i);
            Cell cellPart = rowOrigin.getCell(packingListBean.getColumnaPart());
            Cell cellCantidad = rowOrigin.getCell(packingListBean.getColumnaCantidad());
            if (cellPart != null && cellCantidad != null) {
                // Evaluar la celda Part (String)
                String partValue = "";
                // Obtener valor precalculado de cellPart (que puede tener fórmula)
                if (cellPart.getCellType() == CellType.FORMULA) {
                    CellType cachedType = cellPart.getCachedFormulaResultType();
                    if (cachedType == CellType.STRING) {
                        partValue = cellPart.getStringCellValue().trim();
                    } else if (cachedType == CellType.NUMERIC) {
                        partValue = String.valueOf((int) cellPart.getNumericCellValue());
                    }
                } else if (cellPart.getCellType() == CellType.STRING) {
                    partValue = cellPart.getStringCellValue().trim();
                }

                int cantidad = 0;
                // Obtener valor precalculado de cellCantidad
                if (cellCantidad.getCellType() == CellType.FORMULA) {
                    CellType cachedType = cellCantidad.getCachedFormulaResultType();
                    if (cachedType == CellType.NUMERIC) {
                        cantidad = (int) cellCantidad.getNumericCellValue();
                    } else if (cachedType == CellType.STRING) {
                        try {
                            cantidad = Integer.parseInt(cellCantidad.getStringCellValue().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Valor no numérico en cantidad fila " + i + ": " + cellCantidad.getStringCellValue());
                            break; // saltar fila con valor inválido
                        }
                    }
                } else if (cellCantidad.getCellType() == CellType.NUMERIC) {
                    cantidad = (int) cellCantidad.getNumericCellValue();
                } else if (cellCantidad.getCellType() == CellType.STRING) {
                    try {
                        cantidad = Integer.parseInt(cellCantidad.getStringCellValue().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Valor no numérico en cantidad fila " + i + ": " + cellCantidad.getStringCellValue());
                        break;
                    }
                }

                agregarOSumar(mapSubsidiary, partValue, 0, cantidad);
            }
        }
    }

    /**
     * Procesa el fichero Subsidiary y devuelve una Lista de los Part Code sin duplicados
     *
     * @param subsidiaryBean: Bean de datos del excel subsidiary
     * @return Map
     */
    public static Map<String, DatosExcel> procesarFicheroSubsidiary(SubsidiaryBean subsidiaryBean) {
        Map<String, DatosExcel> partCodeYCantidad = new TreeMap<>();
        final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
        String ultimaClave = null;
        preferences.put("FileSub", subsidiaryBean.getFileSubsidiary().getAbsolutePath());
        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHoja(subsidiaryBean.getFileSubsidiary());
        // Extraemos la fecha del archivo
        Optional<String> valorCeldaFecha = Optional.ofNullable(originSheet.getRow(subsidiaryBean.getFilaFecha()))
                .map(row -> row.getCell(1))
                .filter(cell -> cell.getCellType() == CellType.STRING)
                .map(Cell::getStringCellValue);
        if(valorCeldaFecha.isPresent()){
            // Expresión regular para encontrar la fecha (YYYY-MM-DD)
            Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
            String fecha = valorCeldaFecha.get() + " hola";
            System.out.println(fecha);
            Matcher matcher = pattern.matcher(valorCeldaFecha.get());
            if (matcher.find()) {
                subsidiaryBean.setFecha(matcher.group());
            }else{
                subsidiaryBean.setFecha("");
            }
        }else{
            subsidiaryBean.setFecha("");
        }
        //Recorremos las columnas Part y Qty y damos valor al Map
        for (int i = subsidiaryBean.getFilaInicio(); i <= originSheet.getLastRowNum(); i++) {
            Row rowOrigin = originSheet.getRow(i);
            Cell cellPart = rowOrigin.getCell(subsidiaryBean.getColumnaInicio());
            Cell cellOrderedQty = rowOrigin.getCell(subsidiaryBean.getColumnaOrderedQty());
            if (cellPart != null) {
                String clave = cellPart.getStringCellValue().trim();
                if (!clave.isEmpty()) {
                    ultimaClave = clave;
                }
                agregarOSumar(partCodeYCantidad, ultimaClave, (int) cellOrderedQty.getNumericCellValue(), 0);
            }
        }
        return partCodeYCantidad;
    }

    /**
     * Procesa el fichero Control y obtiene los Part Code existentes
     *
     * @param subsidiaryBean: Bean de datos del excel subsidiary
     * @param  controlPedidosBean: Bean de control de pedidos
     * @return Map
     */
    public static Map<String, DatosExcel> procesarFicheroControl(SubsidiaryBean subsidiaryBean,ControlPedidosBean controlPedidosBean) {
        Map<String, DatosExcel> partCodeYCantidad = new TreeMap<>();
        final Preferences preferences = Preferences.userNodeForPackage(SelectFileController.class);
        preferences.put("FileSub", subsidiaryBean.getFileSubsidiary().getAbsolutePath());
        // Abrimos Hoja del excel origen para extraer datos
        Sheet originSheet = abrirHoja(controlPedidosBean.getFileControlPedidos());
        //Recorremos las columnas Part y Qty y damos valor al Map
        for (int i = 1; i <= originSheet.getLastRowNum(); i++) {
            Row rowOrigin = originSheet.getRow(i);
            Cell cellPart = rowOrigin.getCell(0);
            Cell cellValue = rowOrigin.getCell(1);
            if (cellPart != null) {
                String clave = cellPart.getStringCellValue().trim();
                int valor = (int) cellValue.getNumericCellValue();
                agregarOSumar(partCodeYCantidad, clave, valor, 0);
            }
        }
        return partCodeYCantidad;
    }

    // Obtiene la fecha del archivo PackingList
    public static String obtenerFechaPackingList(File filePackingList) {

        String text = filePackingList.getName();

        // Expresión regular para encontrar la fecha (YYYY-MM-DD)
        Pattern pattern = Pattern.compile("\\d{4}\\.\\d{2}\\.\\d{2}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        } else {
            System.out.println("No se encontró una fecha en el texto.");
            return "";
        }
    }

    // Obtiene la fecha del archivo Reserved
    public static List<String> obtenerFechaPackingListEnControl(File fileControl) {
        // Abrimos Hoja del excel origen para extraer datos
        List<String> listaFechas = new ArrayList<>();
        Sheet originSheet = abrirHoja(fileControl);
        Row rowFecha = originSheet.getRow(0);
        int numCelda = 3;
        while (rowFecha.getCell(numCelda) != null) {
            Cell cellFecha = rowFecha.getCell(numCelda++);
            String text = cellFecha.getStringCellValue();

            // Expresión regular para encontrar la fecha (YYYY-MM-DD)
            Pattern pattern = Pattern.compile("\\d{4}\\.\\d{2}\\.\\d{2}");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                listaFechas.add(matcher.group());
            }
        }
        return listaFechas;
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

    // Crea libro y abre hoja de excel
    private static Sheet abrirHojaPackaging(File file) {
        Sheet sheet;
        try (InputStream mainstream = new FileInputStream(file)) {
            Workbook wb = WorkbookFactory.create(mainstream);
            sheet = wb.getSheet("Packing list");
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
     * @param valorPackingList : Valor de columna Packing List procedente de Packing List
     */
    private static void agregarOSumar(Map<String, DatosExcel> mapa, String clave, int valorOrdered, int valorPackingList) {
        if (mapa.containsKey(clave)) {
            mapa.get(clave).sumar(valorOrdered, valorPackingList);
        } else {
            mapa.put(clave, new DatosExcel(valorOrdered, valorPackingList));
        }
    }
    public static boolean isExcelFileOpen(String path) {
        File file = new File(path);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.tryLock()) {
            return lock == null;
        } catch (IOException e) {
            // Si hay error al intentar bloquear, asumimos que está abierto
            return true;
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
        style.setAlignment(HorizontalAlignment.CENTER);
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
     * @param mapPackingList: Map de los datos obtenidos del Excel PackingList
     */
    protected static void compararYEstablecerCantidad(Map<String, Integer> mapSubsidiary, Map<String, Integer> mapPackingList) {
        for(String clave: mapSubsidiary.keySet()){
            if(mapPackingList.containsKey(clave)){
                mapSubsidiary.put(clave, mapPackingList.get(clave));
            }
        }
    }

}
