package com.freeapp.utility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/* Two ways to read the same .xlsx file: one row by scenario ID (as a Map),
   or every row at once (as Object[][], the shape @DataProvider needs). */

public class ExcelReader
{
    /* Finds one row by its scenarioId column, returns it as a column-name-to-value map.
       Map<String, String> = both keys and values are String;
       The call getScenarioData(TEST_DATA_PATH, "LoginData", "TC04") just supplies values for those three, in order.
       What's actually in the map for "TC04": LoginData has 4 columns (scenarioId, username, password, expectedOutcome),
       so the map getScenarioData builds for row TC04 has 4 entries:
{
    "scenarioId"      -> "TC04",
    "username"        -> "",
    "password"        -> "secret_sauce",
    "expectedOutcome" -> "Epic sadface: Username is required"
} */

    public static Map<String, String> getScenarioData(String filePath, String sheetName, String scenarioId)
    {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) /* opens and parses the .xlsx; both close automatically after */
        {
            Sheet sheet = getSheet(workbook, sheetName);
            Row headerRow = sheet.getRow(0); /* row 0 is always the header */
            int scenarioIdColumn = findColumn(headerRow, "scenarioId");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) /* data rows start at 1 */
            {
                Row row = sheet.getRow(i);
                if (row.getCell(scenarioIdColumn).getStringCellValue().equalsIgnoreCase(scenarioId))
                {
                    return rowToMap(headerRow, row);
                }
            }
            throw new IllegalArgumentException("Scenario not found: " + scenarioId);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
    }

    /* Reads every data row into Object[][], the exact return type TestNG
       requires from a @DataProvider method. */

    public static Object[][] getData(String filePath, String sheetName)
    {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis))
        {
            Sheet sheet = getSheet(workbook, sheetName);
            int rowCount = sheet.getLastRowNum(); /* also the number of data rows, since row 0 is the header */
            int columnCount = sheet.getRow(0).getLastCellNum();
            Object[][] data = new Object[rowCount][columnCount];

            for (int i = 1; i <= rowCount; i++) /* data rows start at 1 */
            {
                Row row = sheet.getRow(i);
                for (int col = 0; col < columnCount; col++)
                {
                    data[i - 1][col] = row.getCell(col).getStringCellValue(); /* sheet row i -> array index i-1 */
                }
            }
            return data;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }
    }

    /* Fails loudly if the sheet name doesn't exist. */
    private static Sheet getSheet(Workbook workbook, String sheetName)
    {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null)
        {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }
        return sheet;
    }

    /* Finds a column by its header text, not a hardcoded position. */
    private static int findColumn(Row headerRow, String columnName)
    {
        for (Cell cell : headerRow)
        {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName))
            {
                return cell.getColumnIndex();
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    /* Pairs the header row with one data row into column-name -> value entries. */
    private static Map<String, String> rowToMap(Row headerRow, Row row)
    {
        Map<String, String> data = new LinkedHashMap<>(); /* keeps column order for readability */
        for (Cell headerCell : headerRow)
        {
            String columnName = headerCell.getStringCellValue();
            data.put(columnName, row.getCell(headerCell.getColumnIndex()).getStringCellValue());
        }
        return data;
    }
}
