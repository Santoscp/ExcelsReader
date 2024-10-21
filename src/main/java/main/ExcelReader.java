package main;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class ExcelReader {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/inper";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Valores permitidos para el ENUM de tipo
    private static final Set<String> validTipos = new HashSet<>(Arrays.asList(
        "Gaming", "TodoEnUno", "Sobremesa", "Smartphone", 
        "TelefonosBasicos", "CartuchoTinta", "Tonel", 
        "Tambores", "Papel", "Teclados", "Raton", 
        "Packs", "Monitores", "Otros"
    ));

    // Valores permitidos para el ENUM de marca
    private static final Set<String> validMarcas = new HashSet<>(Arrays.asList(
        "LG", "Samsung", "Apple"
    ));

    public static void main(String[] args) {
        String excelFilePath = "C:\\Excels\\test2.xlsx";

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

            Sheet sheet = workbook.getSheetAt(0);
            String insertSQL = "INSERT INTO producto (nombre, descripcion, imagen, tipo, marca) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                if (isRowEmpty(row)) {
                    System.out.println("Fila en blanco encontrada. Deteniendo la lectura.");
                    break;
                }

                String nombre = getCellValue(row.getCell(0)).trim();
                String descripcion = getCellValue(row.getCell(1)).trim();
                String imagen = getCellValue(row.getCell(2)).trim();
                String tipo = getCellValue(row.getCell(3)).trim();
                String marca = getCellValue(row.getCell(4)).trim();

                // Depuración
                System.out.println("Leyendo fila " + (rowIndex + 1) + ": " + nombre + ", " + descripcion + ", " + imagen + ", " + tipo + ", " + marca);

                // Validar tipo
                if (!validTipos.contains(tipo)) {
                    System.out.println("Valor inválido para 'tipo': " + tipo);
                    continue; // Saltar esta fila si el tipo es inválido
                }

                // Validar marca
                if (!validMarcas.contains(marca)) {
                    System.out.println("Valor inválido para 'marca': " + marca);
                    continue; // Saltar esta fila si la marca es inválida
                }

                preparedStatement.setString(1, nombre);
                preparedStatement.setString(2, descripcion);
                preparedStatement.setString(3, imagen.isEmpty() ? null : imagen);
                preparedStatement.setString(4, tipo);
                preparedStatement.setString(5, marca);

                preparedStatement.executeUpdate();
                System.out.println("Producto insertado: " + nombre);
            }

            preparedStatement.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al insertar en la base de datos: " + e.getMessage());
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
}