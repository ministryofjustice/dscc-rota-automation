package net.atos.laa.rotas.algorithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.atos.laa.rotas.algorithm.rota.Rota;
import net.atos.laa.rotas.algorithm.rota.RotaColumn;
import net.atos.laa.rotas.algorithm.rota.RotaSlot;
import net.atos.laa.rotas.algorithm.rota.RotaStats;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Serialisation {

    /**
     * Prints a rota out
     * @param rota The rota to print
     */
    public static void printRota(Rota rota) {
        System.out.print("\n                    ");

        // Print column names
        for (RotaColumn column : rota.getColumnList()) {
            System.out.printf(" %-35s", column.getTitle());
        }

        // Print column types
        System.out.print("\n                    ");

        for (RotaColumn column : rota.getColumnList()) {
            System.out.printf(" %-35s", column.getType());
        }

        System.out.println();

        // Print rows
        for (Map.Entry<LocalDate, Integer> dateSlot : rota.getDateList().entrySet()) {
            DateTimeFormatter formatter
                    = DateTimeFormatter.ofPattern("dd/MM/yyyy (EEE)");
            System.out.print(dateSlot.getKey().format(formatter) + "  ->");

            for (RotaColumn column : rota.getColumnList()) {
                if (column.getSlots()[dateSlot.getValue()].getIsBlank()) {
                    System.out.printf(" %-35s", "");

                } else {
                    RotaSlot slotSolicitor
                            = column.getSlots()[dateSlot.getValue()];

                    if (slotSolicitor.getSolicitor() != null) {
                        System.out.printf(" %-35s", slotSolicitor.getSolicitor().getName());
                    }else if (slotSolicitor.getSolicitor()==null && !slotSolicitor.getIsBlank()){
                        System.out.printf(" %-35s", "++CLASH FIX");
                    }

                }
            }

            System.out.println();
        }
    }

    /**
     * Exports a rota to an Excel file with cell colours
     * @param filePath The path to save to
     * @param rota The rota to save
     * @return
     */
    public static boolean exportRota(String filePath, Rota rota, ArrayList<Integer> firms) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Rota");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

            // Create cell styles and fonts
            CellStyle headerStyle = genHeaderStyle(workbook);
            CellStyle headerStyle2 = genHeaderStyle2(workbook);
            CellStyle blankStyle = genBlankStyle(workbook);
            CellStyle clashStyle = genClashStyle(workbook);
            CellStyle dateBorderStyle = workbook.createCellStyle();
            dateBorderStyle.setBorderRight(BorderStyle.MEDIUM);
            XSSFFont defaultFont = genDefaultFont(workbook);
            headerStyle.setFont(defaultFont);
            headerStyle2.setFont(defaultFont);
            HashMap<Integer, CellStyle> firmStyles = new HashMap<>();

            // Output header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0);
            headerRow.getCell(0).setCellValue("Day");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerStyle.setWrapText(true);
            sheet.setColumnWidth(0, 3000);
            headerRow.createCell(1);
            headerRow.getCell(1).setCellValue("Date");
            headerRow.getCell(1).setCellStyle(headerStyle2);
            sheet.setColumnWidth(1, 3000);

            for (int i = 0; i < rota.getColumnList().size(); i++) {
                headerRow.createCell(i + 2);
                headerRow.getCell(i + 2).setCellValue(rota.getColumnList().get(i).getTitle());
                headerRow.getCell(i + 2).setCellStyle(headerStyle);
                sheet.setColumnWidth(i + 2, 5000);
            }

            // Output rest of rota
            int row = 1;
            for (LocalDate date = rota.getStartDate();
                 date.isBefore(rota.getEndDate()) || date.isEqual(rota.getEndDate());
                 date = date.plusDays(1)) {

                // Output date information
                Row dataRow = sheet.createRow(row);
                dataRow.createCell(0);
                dataRow.getCell(0).setCellValue(date.format(dayFormatter));
                dataRow.createCell(1);
                dataRow.getCell(1).setCellValue(date.format(formatter));
                dataRow.getCell(1).setCellStyle(dateBorderStyle);

                // Output allocations
                for (int col = 0; col < rota.getColumnList().size(); col++) {
                    Cell allocation = dataRow.createCell(col + 2);

                    // Slot is blank
                    RotaSlot slot = rota.getColumnList().get(col).getSlots()[row - 1];

                    if (slot.getIsBlank()) {
                        allocation.setCellValue("");
                        allocation.setCellStyle(blankStyle);

                    } else {

                        // Slot has a clash
                        if (slot.getSolicitor() == null) {
                            allocation.setCellStyle(clashStyle);

                        } else {
                            allocation.setCellValue(slot.getSolicitor().getName());
                            allocation.setCellStyle(getFirmStyle(firmStyles,
                                    slot.getSolicitor().getFirmId(), workbook));
                        }
                    }
                }

                row++;
            }

            row++;
            Row dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Clashes: " + rota.getRotaStats().getClashes());
            dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Largest Range: " + rota.getRotaStats().getTotalRange());
            dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Average Range: " + rota.getRotaStats().getTotalRange());
            dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Range is the difference in allocations between the solicitor with the largest and the smallest number of allocations.");
            dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Range is calculated per unique group of columns.");
            dataRow = sheet.createRow(row++);
            dataRow.createCell(1);
            dataRow.getCell(1).setCellValue("Average range is the average of the ranges of all of the unique column groups, and largest is the largest range out of all unique column groups.");

            // Write the Excel file
            FileOutputStream out = new FileOutputStream(new File(filePath));
            workbook.write(out);
            out.close();
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    public static void exportStats(RotaStats stats, String filename, int i){
        File file = new File("H:/Group Project/Output/"+filename+".txt");
        try (FileWriter fr = new FileWriter(file, true)) {
            BufferedWriter br = new BufferedWriter(fr);
            fr.append('\n');
            fr.write("Rota " + i + ", " + stats.getClashes() + ", " + stats.getTotalRange() +", " + stats.getRangeAverage()
                    + ", " + stats.getDaysRange() + ", " + stats.getDaysRangeAverage() + ", " + stats.getCourtRange()
                    + ", " + stats.getCourtAverage() + ", " + stats.getPoliceRange() + ", " + stats.getPoliceAverage()
                    + ", " + stats.getColSeperation() + ", " + stats.getFitness());

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private static CellStyle genHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle genHeaderStyle2(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THICK);
        return style;
    }

    private static CellStyle genBlankStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle genClashStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SPARSE_DOTS);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static XSSFFont genDefaultFont(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setFontName("Calibri");
        font.setBold(true);
        return font;
    }

    private static CellStyle getFirmStyle(HashMap<Integer, CellStyle> firmStyles, int firmId, XSSFWorkbook workbook) {
        if (firmStyles.get(firmId) == null) {
            CellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFPalette pallet = hwb.getCustomPalette();
            ArrayList<HSSFColor> colors = new ArrayList<>();

            Random rand = new Random();

            byte[] b = new byte[3];
            rand.nextBytes(b);
            HSSFColor color = pallet.findSimilarColor(b[0], b[1], b[2]);
            while (colors.contains(color)) {
                rand.nextBytes(b);
                color = pallet.findSimilarColor(b[0], b[1], b[2]);
            }

            short index = color.getIndex();
            style.setFillForegroundColor(index);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            firmStyles.put(firmId, style);
            return style;

        } else return firmStyles.get(firmId);
    }
}
