/**
 * Created by Ajeet Vishwakarma
 * Date : 04-09-2026
 * Time : 06:37 pm
 */

package com.orangehrm.utilities;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public final class ExcelReader {

    private static final Logger logger = LoggerManager.getLogger(ExcelReader.class);

    private ExcelReader() {
        // Prevent object creation
    }

    /**
     * Returns the number of data rows in the Excel sheet.
     * Header row is not included.
     *
     * @param filePath  Excel file path
     * @param sheetName Sheet name
     * @return number of data rows
     */
    public static int getRowCount(String filePath, String sheetName) {

        validateFilePath(filePath);
        validateSheetName(sheetName);

        try (FileInputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet not found: " + sheetName
                );
            }

            int rowCount = sheet.getPhysicalNumberOfRows();

            // Exclude header row
            return Math.max(rowCount - 1, 0);

        } catch (IOException e) {
            logger.error(
                    "Unable to read Excel file: {}",
                    filePath,
                    e
            );

            throw new RuntimeException(
                    "Unable to read Excel file: " + filePath,
                    e
            );
        }
    }

    /**
     * Returns the number of columns in the specified sheet.
     *
     * @param filePath  Excel file path
     * @param sheetName Sheet name
     * @return number of columns
     */
    public static int getColumnCount(String filePath, String sheetName) {

        validateFilePath(filePath);
        validateSheetName(sheetName);

        try (FileInputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet not found: " + sheetName
                );
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return 0;
            }

            return headerRow.getLastCellNum();

        } catch (IOException e) {
            logger.error(
                    "Unable to read Excel file: {}",
                    filePath,
                    e
            );

            throw new RuntimeException(
                    "Unable to read Excel file: " + filePath,
                    e
            );
        }
    }


//    public static String getCellData(String filePath, String sheetName, int rowNumber, int columnNumber) {
//
//        validateFilePath(filePath);
//        validateSheetName(sheetName);
//
//        if (rowNumber < 0 || columnNumber < 0) {
//            throw new IllegalArgumentException(
//                    "Row and column numbers cannot be negative"
//            );
//        }
//
//        try (FileInputStream inputStream = new FileInputStream(filePath);
//             Workbook workbook = new XSSFWorkbook(inputStream)) {
//
//            Sheet sheet = workbook.getSheet(sheetName);
//
//            if (sheet == null) {
//                throw new IllegalArgumentException(
//                        "Sheet not found: " + sheetName
//                );
//            }
//
//            Row row = sheet.getRow(rowNumber);
//
//            if (row == null) {
//                return "";
//            }
//
//            Cell cell = row.getCell(columnNumber);
//
//            if (cell == null) {
//                return "";
//            }
//
//            DataFormatter formatter = new DataFormatter();
//
//            return formatter.formatCellValue(cell);
//
//        } catch (IOException e) {
//            logger.error(
//                    "Unable to read cell data from Excel: {}",
//                    filePath,
//                    e
//            );
//
//            throw new RuntimeException(
//                    "Unable to read cell data from Excel: " + filePath,
//                    e
//            );
//        }
//    }

    public static Object[][] getTestData(String filePath, String sheetName) {

        validateFilePath(filePath);
        validateSheetName(sheetName);

        try (FileInputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet not found: " + sheetName
                );
            }

            int rowCount = sheet.getPhysicalNumberOfRows();

            if (rowCount <= 1) {
                return new Object[0][0];
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Header row is missing in sheet: " + sheetName
                );
            }

            int columnCount = headerRow.getLastCellNum();

            if (columnCount <= 0) {
                return new Object[0][0];
            }

            Object[][] testData = new Object[rowCount - 1][columnCount];

            DataFormatter formatter = new DataFormatter();

            for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                for (int columnIndex = 0;
                     columnIndex < columnCount;
                     columnIndex++) {

                    if (row == null) {
                        testData[rowIndex - 1][columnIndex] = "";
                        continue;
                    }

                    Cell cell = row.getCell(columnIndex);

                    testData[rowIndex - 1][columnIndex] =
                            cell != null
                                    ? formatter.formatCellValue(cell)
                                    : "";
                }
            }

            logger.info(
                    "Test data read successfully from Excel. Sheet: {} | Rows: {} | Columns: {}",
                    sheetName,
                    rowCount - 1,
                    columnCount
            );

            return testData;

        } catch (IOException e) {

            logger.error(
                    "Unable to read test data from Excel: {}",
                    filePath,
                    e
            );

            throw new RuntimeException(
                    "Unable to read test data from Excel: " + filePath,
                    e
            );
        }
    }

    private static void validateFilePath(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Excel file path cannot be null or empty"
            );
        }
    }

    private static void validateSheetName(String sheetName) {

        if (sheetName == null || sheetName.isBlank()) {
            throw new IllegalArgumentException(
                    "Sheet name cannot be null or empty"
            );
        }
    }

}
