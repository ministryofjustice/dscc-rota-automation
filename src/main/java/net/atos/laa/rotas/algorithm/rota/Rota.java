package net.atos.laa.rotas.algorithm.rota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import net.atos.laa.rotas.algorithm.ColumnTemplate;

/**
 * A data structure for storing a duty solicitor rota.
 */
public class Rota {

  private final LocalDate startDate;
  private final LocalDate endDate;
  private final LinkedHashMap<LocalDate, Integer> dateList;
  private ArrayList<RotaColumn> columnList;
  private final int length;
  private final RotaStats stats;

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Creates a new rota with a specific date range and a list of columns
   *
   * @param startDate The start date of the rota
   * @param endDate The end date of the rota
   * @param columnTemplates A list of <code>ColumnTemplate</code>s the indicate the columns that the
   * rota should have
   */
  public Rota(LocalDate startDate, LocalDate endDate, ArrayList<ColumnTemplate> columnTemplates) {
    this.startDate = startDate;
    this.endDate = endDate;
    dateList = new LinkedHashMap<>();
    stats = new RotaStats();

    // Create the list of dates in the specified range
    int indexCounter = 0;
    for (LocalDate date = startDate;
        date.isBefore(endDate) || date.isEqual(endDate);
        date = date.plusDays(1)) {
      dateList.put(date, indexCounter++);
    }

    length = indexCounter;
    addColumns(columnTemplates);
  }

  /**
   * Converts each column template into an actual column in the rota, with cells, and applies the
   * blank slots to it
   *
   * @param columnTemplates The list of column templates for the rota
   */
  private void addColumns(ArrayList<ColumnTemplate> columnTemplates) {
    columnList = new ArrayList<>();

    for (ColumnTemplate template : columnTemplates) {
      RotaColumn column = new RotaColumn(template.getType(), template.getTitle(), length);
      columnList.add(column);

      // Set the slots at the specified blank dates to blank
      if (template.getBlankSlots() != null) {
        for (LocalDate date : template.getBlankSlots()) {
          column.getSlots()[dateList.get(date)].setIsBlank(true);
        }
      }
    }
  }

  public LinkedHashMap<LocalDate, Integer> getDateList() {
    return dateList;
  }

  public ArrayList<RotaColumn> getColumnList() {
    return columnList;
  }

  public RotaStats getRotaStats() {
    return stats;
  }
}
