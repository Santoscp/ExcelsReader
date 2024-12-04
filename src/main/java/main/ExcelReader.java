// Importa las librerías necesarias
package main;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelReader {

//    private static final String DB_URL = "jdbc:mysql://junction.proxy.rlwy.net:13313/railway"; // Cambia por tu base de datos
    private static final String DB_URL = "jdbc:mysql://localhost:3306/prueba"; // Cambia por tu base de datos
    private static final String USER = "root"; // Cambia por tu usuario
//    private static final String PASSWORD = "nohFdPlUDTycnmDBMgNBMvqyhTleGDHX"; // Cambia por tu contraseña
    private static final String PASSWORD = ""; // Cambia por tu contraseña

    public static void main(String[] args) {
        String excelFilePath = "C:\\Excels\\test1.xlsx";
        String fechaEspecificaString = "01/10/2024";  // Fecha desde la cual queremos insertar
        SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy"); // Formato de la fecha en Excel
        SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd"); // Formato de la fecha para la BD
        Date fechaEspecifica = null;

        try {
            fechaEspecifica = sdfInput.parse(fechaEspecificaString); // Convertir la fecha específica a un objeto Date
        } catch (ParseException e) {
            System.out.println("Error al analizar la fecha específica: " + e.getMessage());
            return; // Salir del programa si hay un error de análisis de fecha
        }

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

            Sheet sheet = workbook.getSheetAt(0);
            String insertSQL = "INSERT INTO articulos (id_articulo, IdArticulo, Referencia, `Referencia Proveedor`, Descripción, Ubicación, Familia, `Fecha de Alta`, `Fecha Ultima Compra`, `Fecha Ultima Venta`, `Precio Última Compra`, `Precio Medio Costo`, `P.V.P. (1)`, `P.V.P. (2)`, `P.V.P. (3)`, `I.V.A.`, `Stock Mínimo`, `Stock Máximo`, Observaciones, `No Actualizar Precio`, `No Inventariable`, precio_medio) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Empezar desde 1 para saltar la cabecera
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // Leer y formatear la fecha de última compra
                String fechaUltimaCompraString = formatDate(row.getCell(7), sdfInput);
                Date fechaUltimaCompra = fechaUltimaCompraString != null ? sdfInput.parse(fechaUltimaCompraString) : null;

                // Validar la fecha de última compra es igual o posterior a la fecha específica
                if (fechaUltimaCompra == null || fechaUltimaCompra.before(fechaEspecifica)) {
                    continue; // Saltar la inserción si la fecha no cumple con la condición
                }

                // Resto del código de lectura de datos de la fila
                String id_articulo = getCellValue(row.getCell(0)).trim();
                String idArticulo = getCellValue(row.getCell(0)).trim();
                String referencia = getCellValue(row.getCell(1)).trim();
                String referenciaProveedor = getCellValue(row.getCell(2)).trim();
                String descripcion = getCellValue(row.getCell(3)).trim();
                String ubicacion = getCellValue(row.getCell(4)).trim();
                String familia = getCellValue(row.getCell(5)).trim();

                // Formatear fechas al formato `yyyy-MM-dd` antes de la inserción
                String fechaAlta = formatDate(row.getCell(6), sdfDB);
                String fechaUltimaVenta = formatDate(row.getCell(8), sdfDB);
                String fechaUltimaCompraDB = sdfDB.format(fechaUltimaCompra);

                String precioUltimaCompra = getCellValue(row.getCell(9)).trim();
                String precioMedioCosto = getCellValue(row.getCell(10)).trim();
                String precioPVPC1 = getCellValue(row.getCell(11)).trim();
                String precio_medio = getCellValue(row.getCell(11)).trim();
                String precioPVPC2 = getCellValue(row.getCell(12)).trim();
                String precioPVPC3 = getCellValue(row.getCell(13)).trim();
                String iva = getCellValue(row.getCell(14)).trim();
                String stockMinimo = getCellValue(row.getCell(15)).trim();
                String stockMaximo = getCellValue(row.getCell(16)).trim();
                String observaciones = getCellValue(row.getCell(17)).trim();

                int noActualizarPrecio = getBooleanAsInt(row.getCell(18));
                int noInventariable = getBooleanAsInt(row.getCell(19));

                // Insertar datos en la base de datos
                preparedStatement.setString(1, id_articulo);
                preparedStatement.setString(2, idArticulo);
                preparedStatement.setString(3, referencia);
                preparedStatement.setString(4, referenciaProveedor.isEmpty() ? null : referenciaProveedor);
                preparedStatement.setString(5, descripcion);
                preparedStatement.setString(6, ubicacion.isEmpty() ? null : ubicacion);
                preparedStatement.setString(7, familia.isEmpty() ? null : familia);
                preparedStatement.setString(8, fechaAlta);
                preparedStatement.setString(9, fechaUltimaCompraDB); // Insertar fecha formateada
                preparedStatement.setString(10, fechaUltimaVenta);
                preparedStatement.setString(11, precioUltimaCompra);
                preparedStatement.setString(12, precioMedioCosto);
                preparedStatement.setString(13, precioPVPC1);
                preparedStatement.setString(14, precioPVPC2);
                preparedStatement.setString(15, precioPVPC3);
                preparedStatement.setString(16, iva);
                preparedStatement.setString(17, stockMinimo);
                preparedStatement.setString(18, stockMaximo);
                preparedStatement.setString(19, observaciones.isEmpty() ? null : observaciones);
                preparedStatement.setInt(20, noActualizarPrecio);
                preparedStatement.setInt(21, noInventariable);
                preparedStatement.setString(22, precio_medio);

                preparedStatement.executeUpdate();
                System.out.println("Producto insertado: " + idArticulo);
            }

            preparedStatement.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al insertar en la base de datos: " + e.getMessage());
        } catch (ParseException e) {
            System.out.println("Error al analizar fecha en la fila: " + e.getMessage());
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
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

    private static String formatDate(Cell cell, SimpleDateFormat dateFormat) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(cell)) {
            return null;
        }
        Date date = cell.getDateCellValue();
        return dateFormat.format(date);
    }

    private static int getBooleanAsInt(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.BOOLEAN) {
            return 0;
        }
        return cell.getBooleanCellValue() ? 1 : 0;
    }
}
