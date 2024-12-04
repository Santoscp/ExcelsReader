package main;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class lector {
	  public static void main(String[] args) {
	        String excelFilePath = "C:\\Excels\\test1.xlsx";
	        // Definir la fecha específica a partir de la cual se validará
	        String fechaEspecificaString = "30/10/2024"; // Formato: dd/MM/yyyy
	        Date fechaEspecifica = null;

	        // Formateador de fecha
	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	        
	        try {
	            // Convertir la fecha específica a objeto Date
	            fechaEspecifica = sdf.parse(fechaEspecificaString);
	        } catch (ParseException e) {
	            System.out.println("Error al analizar la fecha específica: " + e.getMessage());
	            return; // Si hay un error, salir del programa
	        }

	        try (FileInputStream fis = new FileInputStream(excelFilePath);
	             Workbook workbook = new XSSFWorkbook(fis)) {

	            Sheet sheet = workbook.getSheetAt(0);

	            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
	                Row row = sheet.getRow(rowIndex);
	                if (row == null) continue;

	                // Verifica si la fila está vacía
	                if (isRowEmpty(row)) {
	                    System.out.println("Fila en blanco encontrada en la fila: " + (rowIndex + 1));
	                    continue; // Continuar con la siguiente fila
	                }

	                // Leer los valores de las columnas
	                String idArticulo = getCellValue(row.getCell(0)).trim();
	                String referencia = getCellValue(row.getCell(1)).trim();
	                String referenciaProveedor = getCellValue(row.getCell(2)).trim();
	                String descripcion = getCellValue(row.getCell(3)).trim();
	                String ubicacion = getCellValue(row.getCell(4)).trim();
	                String familia = getCellValue(row.getCell(5)).trim();
	                String fechaAlta = formatDate(row.getCell(6));  // Formatear la fecha
	                String fechaUltimaCompra = formatDate(row.getCell(7));  // Formatear la fecha
	                String fechaUltimaVenta = formatDate(row.getCell(8));  // Formatear la fecha
	                String precioUltimaCompra = getCellValue(row.getCell(9)).trim();
	                String precioMedioCosto = getCellValue(row.getCell(10)).trim();
	                String pvp1 = getCellValue(row.getCell(11)).trim();
	                String pvp2 = getCellValue(row.getCell(12)).trim();
	                String pvp3 = getCellValue(row.getCell(13)).trim();
	                String iva = getCellValue(row.getCell(14)).trim();
	                String stockMinimo = getCellValue(row.getCell(15)).trim();
	                String stockMaximo = getCellValue(row.getCell(16)).trim();
	                String observaciones = getCellValue(row.getCell(17)).trim();
	                String noActualizarPrecio = getCellValue(row.getCell(18)).trim();
	                String noInventariable = getCellValue(row.getCell(19)).trim();

	                // Comprobar si la fecha de última compra es igual o posterior a la fecha específica
	                if (isFechaUltimaCompraDesde(fechaEspecifica, row.getCell(7))) {
	                    // Mostrar datos en consola solo si la fecha es válida
	                    System.out.println("Leyendo fila " + (rowIndex + 1) + ": " +
	                            "IdArticulo: " + idArticulo + ", " +
	                            "Referencia: " + referencia + ", " +
	                            "Referencia Proveedor: " + referenciaProveedor + ", " +
	                            "Descripción: " + descripcion + ", " +
	                            "Ubicación: " + ubicacion + ", " +
	                            "Familia: " + familia + ", " +
	                            "Fecha de Alta: " + fechaAlta + ", " +
	                            "Fecha Última Compra: " + fechaUltimaCompra + ", " +
	                            "Fecha Última Venta: " + fechaUltimaVenta + ", " +
	                            "Precio Última Compra: " + precioUltimaCompra + ", " +
	                            "Precio Medio Costo: " + precioMedioCosto + ", " +
	                            "P.V.P. (1): " + pvp1 + ", " +
	                            "P.V.P. (2): " + pvp2 + ", " +
	                            "P.V.P. (3): " + pvp3 + ", " +
	                            "I.V.A.: " + iva + ", " +
	                            "Stock Mínimo: " + stockMinimo + ", " +
	                            "Stock Máximo: " + stockMaximo + ", " +
	                            "Observaciones: " + observaciones + ", " +
	                            "No Actualizar Precio: " + noActualizarPrecio + ", " +
	                            "No Inventariable: " + noInventariable);
	                }
	            }

	        } catch (IOException e) {
	            System.out.println("Error al leer el archivo: " + e.getMessage());
	        }
	    }

	    private static boolean isRowEmpty(Row row) {
	        for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
	            Cell cell = row.getCell(colIndex);
	            if (cell != null && getCellValue(cell).trim().length() > 0) {
	                return false;
	            }
	        }
	        return true;
	    }

	    private static String getCellValue(Cell cell) {
	        if (cell == null) return "";
	        switch (cell.getCellType()) {
	            case STRING:
	                return cell.getStringCellValue();
	            case NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
	                } else {
	                    return String.valueOf(cell.getNumericCellValue());
	                }
	            case BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case FORMULA:
	                return cell.getCellFormula();
	            case BLANK:
	                return "";
	            default:
	                return "";
	        }
	    }

	    private static String formatDate(Cell cell) {
	        if (cell == null) return "";
	        if (cell.getCellType() == CellType.STRING) {
	            return cell.getStringCellValue(); // Si es una cadena, devuelve el valor tal cual
	        } else if (DateUtil.isCellDateFormatted(cell)) {
	            Date date = cell.getDateCellValue();
	            return new SimpleDateFormat("dd/MM/yyyy").format(date); // Formatear la fecha
	        }
	        return ""; // Si no es un tipo de fecha, devuelve una cadena vacía
	    }

	    private static boolean isFechaUltimaCompraDesde(Date fechaEspecifica, Cell cell) {
	        // Verifica que la celda no sea nula y contenga una fecha válida
	        if (cell == null) return false;

	        // Verificar el tipo de celda antes de obtener el valor
	        if (cell.getCellType() == CellType.STRING) {
	            // Si es una celda de tipo STRING, intenta convertirla a una fecha
	            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	            try {
	                Date date = sdf.parse(cell.getStringCellValue());
	                return !date.before(fechaEspecifica); // Verifica si la fecha es igual o posterior
	            } catch (ParseException e) {
	                System.out.println("Error al analizar la fecha: " + e.getMessage());
	                return false; // No es una fecha válida
	            }
	        } else if (DateUtil.isCellDateFormatted(cell)) {
	            // Si es una fecha, comprobar si es igual o posterior a la fecha específica
	            Date fechaUltimaCompra = cell.getDateCellValue();
	            return !fechaUltimaCompra.before(fechaEspecifica); // Verifica si la fecha es igual o posterior
	        }
	        return false; // Si no es una celda válida, retornamos falso
	    }
	}


