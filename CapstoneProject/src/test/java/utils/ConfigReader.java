package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties prop;
    private static Sheet sheet;

    public static void loadConfig(String resourcePath) {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            prop = new Properties();
            prop.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + resourcePath, e);
        }
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }

    public static void setExcelFile(String excelFileName, String sheetName) throws InvalidFormatException {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(excelFileName)) {
            Workbook wb = WorkbookFactory.create(in);
            sheet = wb.getSheet(sheetName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Excel: " + excelFileName, e);
        }
    }

    public static String getCellData(int rowNum, int colNum) {
        return sheet.getRow(rowNum).getCell(colNum).toString();
    }

    public static int getRowCount() {
        return sheet.getPhysicalNumberOfRows() - 1;
    }
}
