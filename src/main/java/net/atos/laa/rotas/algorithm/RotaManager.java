package net.atos.laa.rotas.algorithm;

import static net.atos.laa.rotas.algorithm.Serialisation.printRota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import net.atos.laa.rotas.algorithm.dataset.Dataset;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.dataset.Solicitor;
import net.atos.laa.rotas.algorithm.rota.ColumnType;
import net.atos.laa.rotas.algorithm.rota.Rota;
import net.atos.laa.rotas.algorithm.rota.RotaColumn;
import net.atos.laa.rotas.algorithm.rota.RotaSlot;
import net.atos.laa.rotas.algorithm.rota.RotaStats;
import net.atos.laa.rotas.algorithm.rota.SchemeSets;

public class RotaManager {

  private final Dataset dataset;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final ArrayList<ColumnTemplate> columnInfo;
  private Rota workingRota;
  private Rota bestRota;
  private final ArrayList<Solicitor> solicitors;

  private HashMap<Integer, ArrayList<Integer>> dayAllocations;
  private HashMap<Integer, Integer> allocations;
  private HashMap<Integer, Integer> policeAllocations;
  private HashMap<Integer, Integer> courtAllocations;
  private HashMap<ArrayList<Scheme>, SchemeSets> schemes;
  private ArrayList<SchemeSets> schemeSets;
  private int workingSolicitor;


  /**
   * @return Returns the current best rota that the rotamanager has managed to create
   */

  public Rota getBestRota() {
    return bestRota;
  }

  /**
   * @param dataset The dataset which contains all of the solicitors, firms, and schemes
   * @param startDate The start date of the rota
   * @param endDate The end date of the rota
   * @param columnInfo An array of templates for each column that will be created in the rota
   */

  public RotaManager(
      Dataset dataset, LocalDate startDate, LocalDate endDate,
      ArrayList<ColumnTemplate> columnInfo) {
    this.dataset = dataset;
    this.startDate = startDate;
    this.endDate = endDate;

    this.columnInfo = columnInfo;
    solicitors = new ArrayList<>();
    workingSolicitor = 0;

    // Load the solicitors for the specified schemes
    for (int scheme : getSchemesUsed()) {
      solicitors.addAll(dataset.getSolicitorsForScheme(scheme));
    }
    for (ColumnTemplate template : columnInfo) {
      template.expandBlanksPattern(startDate, endDate);
    }
  }

  /**
   * Resets the schemeSet objects which keep track of the lists of solicitors for each column
   */

  public void resetSchemeSets() {
    schemes = new HashMap<>();
    schemeSets = new ArrayList<>();
    ColumnTemplate prevColTemp = columnInfo.get(0);
    ColumnTemplate columnTemplate = null;
    int adjacentCols = 1;
    for (int i = 1; i <= columnInfo.size(); i++) {
      if (i != columnInfo.size()) {
        columnTemplate = columnInfo.get(i);
        if (columnTemplate.getSchemes().equals(prevColTemp.getSchemes())) {
          adjacentCols++;
          prevColTemp = columnTemplate;
          continue;
        }
      }

      if (!schemes.containsKey(prevColTemp.getSchemes())) {
//                System.out.println("Adjacent Cols: " + adjacentCols);
//                System.out.println("Making new schemeset " + prevColTemp.getTitle());
        newSchemeSet(prevColTemp, adjacentCols);
      } else if (schemes.get(prevColTemp.getSchemes()).getType() != prevColTemp.getType()) {
//                System.out.println("Making new schemeset with different type " + prevColTemp.getTitle());
        newSchemeSet(prevColTemp, adjacentCols);
      }
      adjacentCols = 1;
      prevColTemp = columnTemplate;
    }
//        for (SchemeSets schemeSet : schemeSets) {
//            System.out.println("New SchemeSet\n\n");
//            schemeSet.printSols(schemeSet.solicitors);
//        }
  }

  /**
   * Creates a new SchemeSet object and adds it to the list of schemesets
   *
   * @param coltemp The template of the column that the schemeset belongs to
   * @param adjacentCols The amount of adjacent columns of same schemes being used.
   */

  private void newSchemeSet(ColumnTemplate coltemp, int adjacentCols) {
    ArrayList<Solicitor> sols = new ArrayList<>();
    for (Scheme scheme : coltemp.getSchemes()) {

      sols.addAll(dataset.getSolicitorsForScheme(scheme.getSchemeId()));
    }
    SchemeSets schemeSet = new SchemeSets(sols, allocations, coltemp.getType(), adjacentCols);
    schemeSets.add(schemeSet);
    schemes.put(coltemp.getSchemes(), schemeSet);
  }

  /**
   * Looks at the column information and produces a list of IDs of all the schemes used
   *
   * @return A list of the IDs of schemes used by the rota manager
   */
  private ArrayList<Integer> getSchemesUsed() {
    ArrayList<Integer> schemeIds = new ArrayList<Integer>();

    for (ColumnTemplate column : columnInfo) {
      for (Scheme scheme : column.getSchemes()) {
        if (!schemeIds.contains(scheme.getSchemeId())) {
          schemeIds.add(scheme.getSchemeId());
        }
      }
    }

    return schemeIds;
  }

  /**
   * Clears the allocations list and adds a zero value for every solicitor and calls the
   * resetSchemeSets to reset them
   */
  private void resetAllocations() {
    allocations = new HashMap<>();
    policeAllocations = new HashMap<>();
    courtAllocations = new HashMap<>();
    dayAllocations = new HashMap<>();

    for (Solicitor sol : solicitors) {
      int id = sol.getSolicitorId();
      allocations.put(id, 0);
      policeAllocations.put(id, 0);
      courtAllocations.put(id, 0);
      dayAllocations.put(id, newWeek());
    }
    resetSchemeSets();
  }

  /**
   * Simply creates an arraylist of integers and sets all values to 0.
   *
   * @return Returns said array list of 0s
   */
  private ArrayList<Integer> newWeek() {
    ArrayList<Integer> week = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      week.add(0);
    }
    return week;
  }

  /**
   * This function keeps track of the best current rota and compares it to each new rota generated.
   */

  public void generateRota() {
    int solicitorOffset = 0;

    for (int i = 0; i < 100; i++) {

      resetAllocations();
      workingRota = new Rota(startDate, endDate, columnInfo);
      workingSolicitor = 0;

      if (bestRota == null) {
        bestRota = workingRota;

      }

      RotaStats workingStats = workingRota.getRotaStats();
      generateRota(solicitorOffset++);

      workingStats.setSolicitors(solicitors);
      workingStats.setPoliceAllocations(policeAllocations);
      workingStats.setCourtAllocations(courtAllocations);
      workingStats.setTotalAllocations(allocations);
      workingStats.setSchemeIDs(getSchemesUsed());
      workingStats.setDayAllocations(dayAllocations);

      RotaStats bestStats = bestRota.getRotaStats();
      float workingEval = workingStats.evaluateRota(workingRota.getColumnList());
      float bestEval = bestStats.evaluateRota(bestRota.getColumnList());

      if (workingEval <= bestEval) {
        bestRota = workingRota;
        //exportStats(bestRota.getRotaStats(),"bestRotaStats",i);
      }
      // exportStats(workingRota.getRotaStats(),"allRotaStats",i);
    }
    RotaStats bestStats = bestRota.getRotaStats();

    System.out.println("***** LEAST CLASHES ROTA *****");
    printRota(bestRota);

    bestStats.printRange();
    bestStats.printStats();
  }

  /**
   * This generates the rota and fills in each slot accordingly.
   *
   * @param offset Currently redundant
   */

  private void generateRota(int offset) {
    boolean isClash = false;
    int tempRow = 0;
    int tempCol = 0;
    int row = 0;
    int clashSolveTries = 0;

    ColumnTemplate columnTemplate;
    RotaColumn column = workingRota.getColumnList().get(0);
    RotaSlot[] slots = column.getSlots();
    ColumnType type = ColumnType.POLICE;
    // Iterate over rows in the workingRota
    for (SchemeSets schemeSet : schemeSets) {
//            System.out.println("Sets loop");
      schemeSet.equalDist();
      schemeSet.shuffleSols();
      for (row = 0; row < slots.length; row++) {
//                System.out.println("Row loop");
        // Iterate over columns in the workingRota
        for (int col = 0; col < workingRota.getColumnList().size(); col++) {
//                    System.out.println("Col loop");
          columnTemplate = columnInfo.get(col);
          if (schemes.get(columnTemplate.getSchemes()) != schemeSet) {
            continue;
          }
          column = workingRota.getColumnList().get(col);
          slots = column.getSlots();

          // If slot is not full
          if (slots[row].getSolicitor() == null && !slots[row].getIsBlank()) {

            // See if the solicitor to be added is already present on previous, current and next row
            if (!groupOfSolicitors(row, slots.length - 1,
                workingRota.getColumnList().size())
                .contains(peekSolicitor(columnTemplate).getName())) {

              column.getSlots()[row].setSolicitor(getSolicitor(columnTemplate, row));

              //If there had been a clash, return to where it was with the next solicitor to try
              if (isClash) {
                if (tempRow == 0) {
                  row = slots.length - 1;
                  col = tempCol - 1;

                } else {
                  row = tempRow - 1;
                  col = tempCol;
                }

                column = workingRota.getColumnList().get(col);
                columnTemplate = columnInfo.get(col);
                slots = column.getSlots();
                isClash = false;
              }
            } else {
              //If solicitor is present already, save location and check next slots
              //System.out.println(peekSolicitor(columnTemplate).getName());
              if (!isClash) {
                tempRow = row;
                tempCol = col;
                isClash = true;
              }
            }
          }

          // Check if we're at the end and there are unsolved clashes remaining
          if (col == workingRota.getColumnList().size() - 1 && isClash && row == slots.length - 1) {
            if (tempRow == 0) {
              row = slots.length - 1;
              col = tempCol - 1;

            } else {
              row = tempRow - 1;
              col = tempCol;
            }

            // Enough attempts at solving the remaining clashes, stop trying now
            clashSolveTries++;
            if (clashSolveTries >= 2 * slots.length - 2) {
              return;

            }

            column = workingRota.getColumnList().get(col);
            isClash = false;

            if (workingSolicitor == solicitors.size() - 1) {
              workingSolicitor = 0;
            } else {
              workingSolicitor++;
            }
          }
        }
      }
    }
    redistributeColType();
  }

  /**
   * Gets the solicitor currently being worked on, increments the number of times that solicitor has
   * been allocated, then moves to the next solicitor
   *
   * @return The solicitor currently being worked on
   */
  private Solicitor getSolicitor(ColumnTemplate col, int row) {
    Solicitor solicitor = schemes.get(col.getSchemes()).requestSolicitor();
    changeAlloc(allocations, solicitor, +1);

    if (col.getType() == ColumnType.POLICE) {
      changeAlloc(policeAllocations, solicitor, +1);
    } else if (col.getType() == ColumnType.COURT) {
      changeAlloc(courtAllocations, solicitor, +1);
    }

    changeDaysAlloc(dayAllocations, solicitor,
        workingRota.getStartDate().plusDays(row).getDayOfWeek().getValue(), +1);
    return solicitor;
  }

  private ArrayList<Solicitor> swapSolicitor(ArrayList<Solicitor> sols) {
    sols = solicitors;
    for (int i = 0; i < sols.size() / 2; i++) {
      Solicitor temp = sols.get(i * 2);
      sols.set(i * 2, sols.get(i * 2 + 1));
      sols.set(i * 2 + 1, temp);
    }

    if (sols.size() % 2 == 1) {
      Random rand = new Random();
      Solicitor temp = sols.get(sols.size() - 1);
      sols.set(rand.nextInt(sols.size()), temp);
      sols.remove(sols.size() - 1);
    }

    return sols;
  }

  /**
   * Gets the solicitor currently being worked on
   *
   * @return The solicitor currently being worked on
   */
  private Solicitor peekSolicitor(ColumnTemplate col) {
    return schemes.get(col.getSchemes()).peekSolicitor();
  }


  /**
   * The groups the solicitors of the current row, aswell as the row before and after, into one
   * arraylist to be used as a comparison
   *
   * @param row Which row are you comparing?
   * @param maxRows The total no. of rows in rota
   * @param maxcolumn The total no. of cols in rota
   * @return Returns an arraylist containing the names of all solicitors in the row above, below and
   * the same row specified
   */

  private ArrayList<String> groupOfSolicitors(int row, int maxRows, int maxcolumn) {
    ArrayList<String> s = new ArrayList<>();

    if (row > 0) {
      s.addAll(rowOfSolicitors(row - 1, maxcolumn));
    }

    s.addAll(rowOfSolicitors(row, maxcolumn));

    if (row < maxRows) {
      s.addAll(rowOfSolicitors(row + 1, maxcolumn));
    }
    return s;
  }

  /**
   * This groups solicitors in one row into an arraylist
   *
   * @param row Row to group
   * @param toColumn How far along column should you group them?
   * @return The grouped solicitors names in a single arraylist
   */

  private ArrayList<String> rowOfSolicitors(int row, int toColumn) {
    ArrayList<String> s = new ArrayList<>();

    for (int i = 0; i < toColumn; i++) {
      if (workingRota.getColumnList().get(i).getSlots()[row].getSolicitor() != null) {
        s.add(workingRota.getColumnList().get(i).getSlots()[row].getSolicitor().getName());
      }
    }

    return s;
  }

  /**
   * This calls redistribute row on every row in the rota in an attempt to even out the allocations
   * of types
   */

  private void redistributeColType() {
    for (int row = 0; row < workingRota.getColumnList().get(0).getSlots().length; row++) {
      redistributeRow(row);
    }
  }

  /**
   * This redistributes each row to even out type allocation. This happens by checking along the row
   * and comparing each solicitior to see if one has more of a type of allocation than the other
   *
   * @param row The row that is being redistributed
   */

  private void redistributeRow(int row) {
    RotaColumn column;
    ArrayList<Solicitor> solsInRow = solInRow(row);
    for (int col = 0; col < workingRota.getColumnList().size(); col++) {
      column = workingRota.getColumnList().get(col);
      if (column.getSlots()[row].getSolicitor() == null) {
        continue;
      }

      Solicitor sol = column.getSlots()[row].getSolicitor();

      solsInRow.remove(sol);
      HashMap<Integer, Integer> alloc, oppositeAlloc;
      if (column.getType() == ColumnType.COURT) {
        alloc = courtAllocations;
        oppositeAlloc = policeAllocations;
      } else if (column.getType() == ColumnType.POLICE) {
        alloc = policeAllocations;
        oppositeAlloc = courtAllocations;
      } else {
        System.err.println("Column does not exist");
        return;
      }

      for (int i = col; i < workingRota.getColumnList().size(); i++) {
        RotaColumn tempColumn = workingRota.getColumnList().get(i);
        if (column.getType() == tempColumn.getType() || (
            schemes.get(columnInfo.get(col).getSchemes()) != schemes
                .get(columnInfo.get(i).getSchemes()))
            || tempColumn.getSlots()[row].getSolicitor() == null) {
          continue;
        }
        Solicitor solicitor = tempColumn.getSlots()[row].getSolicitor();
        if (alloc.get(sol.getSolicitorId()) > alloc.get(solicitor.getSolicitorId())
            && oppositeAlloc.get(sol.getSolicitorId()) < oppositeAlloc
            .get(solicitor.getSolicitorId())) {
          System.out.println(
              "Switching Sol: " + sol.getName() + ", " + alloc.get(sol.getSolicitorId())
                  + " with Sol: " + solicitor.getName() + ", " + alloc
                  .get(solicitor.getSolicitorId()));
          tempColumn.getSlots()[row].setSolicitor(sol);
          changeAlloc(alloc, sol, -1);
          changeAlloc(oppositeAlloc, sol, +1);
          column.getSlots()[row].setSolicitor(solicitor);
          changeAlloc(alloc, solicitor, +1);
          changeAlloc(oppositeAlloc, solicitor, -1);
          break;
        }
      }

    }
  }

  /**
   * Gets an arraylist of firmIDs, for all of the firms that are used in this rota
   *
   * @return Arraylist of firmIDs
   */

  public ArrayList<Integer> getFirmsUsed() {
    ArrayList<Integer> firms = new ArrayList<>();

    for (Solicitor solicitor : solicitors) {
      if (!firms.contains(solicitor.getFirmId())) {
        firms.add(solicitor.getFirmId());
      }
    }

    return firms;
  }

  /**
   * Short method to change allocations by a certain amount
   *
   * @param allocations Which allocation to change
   * @param sol Which solicitors allocation is changing
   * @param change How much the allocation is changing by.
   */

  private void changeAlloc(HashMap<Integer, Integer> allocations, Solicitor sol, int change) {
    allocations.put(sol.getSolicitorId(), allocations.get(sol.getSolicitorId()) + change);
  }


  /**
   * Short method to change day allocation by certain amount
   *
   * @param daysAlloc Which allocation to change
   * @param sol Which solicitor to change
   * @param day Which day to change
   * @param change How much to change by
   */
  private void changeDaysAlloc(HashMap<Integer, ArrayList<Integer>> daysAlloc, Solicitor sol,
      int day, int change) {
    ArrayList<Integer> week = daysAlloc.get(sol.getSolicitorId());
    week.set(day - 1, week.get(day - 1) + change);
    daysAlloc.put(sol.getSolicitorId(), week);
  }

  /**
   * Gets an arraylist of Solicitors of a certain row
   *
   * @param row Which row to group
   * @return The arraylist of solicitors
   */
  private ArrayList<Solicitor> solInRow(int row) {
    ArrayList<Solicitor> sols = new ArrayList<>();

    for (int col = 0; col < workingRota.getColumnList().size(); col++) {
      RotaColumn column = workingRota.getColumnList().get(col);

      if (!column.getSlots()[row].getIsBlank() && column.getSlots()[row].getSolicitor() != null) {
        sols.add(column.getSlots()[row].getSolicitor());
      }
    }
    return sols;
  }
}
