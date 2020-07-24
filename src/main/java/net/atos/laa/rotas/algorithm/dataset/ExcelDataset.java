package net.atos.laa.rotas.algorithm.dataset;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Manages access to a single source of rota data in Excel format. Enables
 * access to the list of schemes and the list of solicitors for each scheme,
 * that are stored in the dataset.
 */
public class ExcelDataset implements Dataset {

    /**
     * Stores the workbook of the Excel file
     */
    private Workbook workbook;

    /**
     * Stores the sheet of the Excel workbook
     */
    private Sheet sheet;

    /**
     * Stores the coordinates of the start cells for every dataset section
     */
    private ArrayList<Point> startCells;

    /**
     * Stores the list of schemes extracted from the dataset file
     */
    private ArrayList<Scheme> schemes;

    /**
     * Stores the list of solicitors extracted from the dataset file
     */
    private ArrayList<Solicitor> solicitors;

    /**
     * Stores the list of firms extracted from the dataset file
     */
    private ArrayList<Firm> firms;


    /**
     * Creates and opens a connection to a dataset for accessing data in the
     * Excel format
     *
     * @param sourceFilePath The path to the dataset file
     * @param sheetName      The name of the Excel sheet that contains the data
     * @throws IOException
     */
    public ExcelDataset(String sourceFilePath, String sheetName) throws IOException {

        // Check if the specified dataset file exists
        if (!new File(sourceFilePath).exists()) {
            throw new FileNotFoundException("Dataset file does not exist");
        }

        workbook = WorkbookFactory.create(new File(sourceFilePath));
        sheet = null;

        // Check if the sheet name exists
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equals(sheetName)) {
                sheet = workbook.getSheet(sheetName);
                break;
            }
        }

        if (sheet == null)
            throw new IOException("No sheet exists in the workbook with the specified name");
        deserialiseDataset();
    }

    /**
     * Goes through the dataset and produces an in-memory relationable
     * representation of the data
     *
     * @throws IOException
     */
    private void deserialiseDataset() throws IOException {
        findStartCells();
        schemes = new ArrayList<>();
        solicitors = new ArrayList<>();
        firms = new ArrayList<>();

        DataFormatter dataFormatter = new DataFormatter();
        Random random = new Random();

        // Iterate over each dataset section
        for (Point startCell : startCells) {
            boolean endSection = false;
            int rowCounter = startCell.y + 1;
            Row row = sheet.getRow(rowCounter);

            // Iterate over rows from start cell
            while (row != null) {
                if (dataFormatter.formatCellValue(row.getCell(
                        startCell.x)).equals("Lead Office Account Number")) break;

                boolean rowIsNull = true;
                for (int i = 0; i < 16; i++) {
                    if (!dataFormatter.formatCellValue(row.getCell((int) startCell.getX() + i)).equals(""))
                        rowIsNull = false;
                }

                if (rowIsNull) break;

                // Process each row
                if (!dataFormatter.formatCellValue(row.getCell((int) startCell.getX() + 6)).equals("")
                        && !dataFormatter.formatCellValue(row.getCell((int) startCell.getX() + 2)).equals("")
                        && !dataFormatter.formatCellValue(row.getCell((int) startCell.getX() + 3)).equals("")) {

                    // Retrieve scheme name
                    String schemeName = "";
                    Cell cell = row.getCell((int) startCell.getX() + 6);
                    String cellValue = dataFormatter.formatCellValue(cell);

                    if (getSchemeId(cellValue) == -1)
                        schemes.add(new Scheme(random.nextInt(), cellValue));
                    schemeName = cellValue;

                    // Retrieve firm name
                    String firmName = "";
                    cell = row.getCell((int) startCell.getX() + 2);
                    cellValue = dataFormatter.formatCellValue(cell);

                    if (getFirmId(cellValue) == -1)
                        firms.add(new Firm(random.nextInt(), cellValue));
                    firmName = cellValue;

                    // Create solicitor
                    cell = row.getCell((int) startCell.getX() + 3);
                    cellValue = dataFormatter.formatCellValue(cell);

                    solicitors.add(new Solicitor(random.nextInt(),
                            cellValue, getFirmId(firmName), getSchemeId(schemeName)));
                }

                row = sheet.getRow(++rowCounter);
            }
        }
    }

    /**
     * Searches the Excel sheet for all cells indicating the start of a dataset
     * fragment
     */
    private void findStartCells() {
        startCells = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        for (Row row : sheet) {
            for (Cell cell : row) {
                String cellValue = dataFormatter.formatCellValue(cell);

                // This value is the first cell in the dataset header
                if (cellValue.equals("Lead Office Account Number")) {
                    startCells.add(new Point(cell.getColumnIndex(), cell.getRowIndex()));
                }
            }
        }
    }

    /**
     * Searches for a scheme with the specified name in the deserialised data
     *
     * @param name The name of the scheme to search for
     * @return Returns the ID of the specified scheme name, or -1 if not found
     */
    private int getSchemeId(String name) {
        for (Scheme scheme : schemes) {
            if (scheme.getName().equals(name)) return scheme.getSchemeId();
        }

        return -1;
    }

    /**
     * Searches for a scheme with the specified ID in the deserialised data
     *
     * @param schemeId The ID of the scheme to search for
     * @return Returns a <code>Scheme</code> containing data for the specified
     * scheme, or null if not found
     */
    private Scheme getSchemeForId(int schemeId) {
        for (Scheme scheme : schemes) {
            if (scheme.getSchemeId() == schemeId) return scheme;
        }

        return null;
    }

    /**
     * Searches for a firm with the specified name in the deserialised data
     *
     * @param name The name of the firm to search for
     * @return Returns the ID of the specified firm name, or -1 if not found
     */
    private int getFirmId(String name) {
        for (Firm firm : firms) {
            if (firm.getName().equals(name)) return firm.getFirmId();
        }

        return -1;
    }

    @Override
    public ArrayList<Scheme> getSchemes() {
        ArrayList<Scheme> _schemes = new ArrayList<>();

        for (Scheme scheme : schemes) {
            _schemes.add(scheme);
        }

        return schemes;
    }

    @Override
    public ArrayList<Solicitor> getSolicitorsForScheme(int schemeId) {
        ArrayList<Solicitor> _solicitors = new ArrayList<>();
        Scheme scheme = getSchemeForId(schemeId);
        System.out.println(schemeId);
        for (Solicitor sol : solicitors) {
            System.out.println(sol.getName());
            if (sol.getSchemeId() == scheme.getSchemeId()) {
                _solicitors.add(sol);
            }
        }

        return _solicitors;
    }

    @Override
    public boolean disconnect() {
        try {
            if (workbook != null) {
                workbook.close();
                return true;
            }

        } catch (Exception ignored) {
        }

        return false;
    }
}
