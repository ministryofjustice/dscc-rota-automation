package net.atos.laa.rotas.algorithm.rota;

import java.util.ArrayList;
import java.util.HashMap;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.dataset.Solicitor;

public class RotaStats {
    private ArrayList<Solicitor> solicitors;
    private HashMap<Integer, Integer> totalAllocations;
    private HashMap<Integer, Integer> courtAllocations;
    private HashMap<Integer, Integer> policeAllocations;
    private HashMap<Integer, ArrayList<Integer>> dayAllocations;
    private HashMap<Integer, Integer> schemeRanges;
    private HashMap<ArrayList<Scheme>, SchemeSets> schemeSets;
    private ArrayList<Integer> schemeIDs;
    private float rangeAverage;
    private float daysRangeAverage;
    private float courtAverage;
    private float policeAverage;
    private int daysRange;
    private int totalRange;
    private int courtRange;
    private int policeRange;
    private int clashes;
    private int colSeperation;
    private float fitness;

    private final float clashWeighting = 1300;
    private final float rangeWeighting = 10;
    private final float rangeAverageWeighting = 70;
    private final float daysWeighting = 7.5f;
    private final float courtRangeWeighting = 15f;
    private final float courtAverageWeighting = 20;
    private final float policeRangeWeighting = 15f;
    private final float policeAverageWeighting = 20;
    private final float seperationWeighting = 5f;

    public RotaStats() {
        solicitors = new ArrayList<>();
        totalAllocations = new HashMap<>();
        courtAllocations = new HashMap<>();
        policeAllocations = new HashMap<>();
        dayAllocations = new HashMap<>();
        schemeRanges = new HashMap<>();
        schemeSets = new HashMap<>();
        schemeIDs = new ArrayList<>();
    }

    private void calculateClashes(ArrayList<RotaColumn> columnList) {
        int clash = 0;

        for (int col = 0; col < columnList.size(); col++) {
            RotaColumn column = columnList.get(col);
            RotaSlot[] slots = column.getSlots();

            for (int row = 0; row < slots.length; row++) {
                if (slots[row].getSolicitor() == null && !slots[row].getIsBlank()) {
                    clash++;
                }
            }
        }

        clashes = clash;
    }

    private void calculateSeperation(ArrayList<RotaColumn> columnList) {
        int colSep = 0;

        for (int col = 0; col < columnList.size(); col++) {
            //System.out.println(columnList.get(col).getSlots().length);
            colSep += colSeperation(columnList.get(col));
            //System.out.println(colSep);
        }

        colSeperation = colSep;
    }

//    private int rowSeperation(){
//
//    }

    private int colSeperation(RotaColumn col) {
        int value = 0;
        int prevSameFirmNo = 0;
        Solicitor prevSol, sol;
        RotaSlot[] slots = col.getSlots();
        int nonBlank;
        //System.out.println(slots.length);
        for (nonBlank = 0; slots[nonBlank].getSolicitor() == null; nonBlank++) {
            if (nonBlank >= slots.length - 1) {
                return 10000;
            }
        }
        //System.out.println(nonBlank);
        prevSol = slots[nonBlank].getSolicitor();

        for (int i = nonBlank + 1; i < slots.length; i++) {
            if (slots[i].getSolicitor() == null) {
                continue;
            }

            //System.out.println(i);
            //System.out.println(slots.length);
            sol = slots[i].getSolicitor();

            if (prevSol.getFirmId() == sol.getFirmId()) {
                prevSameFirmNo++;
                value = value + 4 ^ prevSameFirmNo;

            } else {
                prevSameFirmNo = 0;
            }

            prevSol = sol;
        }
        return value;
    }

    public void printRange() {
        for (int scheme : schemeIDs) {
            System.out.println("Scheme: " + scheme);
            for (Solicitor sol : solicitors) {
                if (sol.getSchemeId() == scheme) {
                    int temp = totalAllocations.get(sol.getSolicitorId());
                    System.out.printf("%-35s Scheme: %d Total: %d Court: %d Police: %d DaysOfWeek: ", sol.getName(), sol.getSchemeId(), temp, courtAllocations.get(sol.getSolicitorId()), policeAllocations.get(sol.getSolicitorId()));
                    ArrayList<Integer> days = dayAllocations.get(sol.getSolicitorId());
                    System.out.printf("Mon: %-5d Tue: %-5d Wed: %-5d Thur: %-5d Fri %-5d Sat: %-5d Sun: %-5d \n", days.get(0), days.get(1), days.get(2), days.get(3), days.get(4), days.get(5), days.get(6));

                }
            }
        }
    }

    private void calculateDaysRange() {
        int largest;
        int smallest;
        int largestrange = 0;
        int temp;
        float totalRange = 0;

        for (Solicitor sol : solicitors) {
            largest = 0;
            smallest = 10000;
            ArrayList<Integer> days = dayAllocations.get(sol.getSolicitorId());
            for (int dayAlloc : days) {
                temp = dayAlloc;
                if (temp > largest) {
                    largest = temp;
                }
                if (temp < smallest) {
                    smallest = temp;
                }
            }
            if (largestrange < (largest - smallest)) {
                largestrange = largest - smallest;
            }
            totalRange = totalRange + largest - smallest;
        }
        daysRange = largestrange;
        float size = solicitors.size();
        daysRangeAverage = totalRange / size;
    }


    private int calculateRange(HashMap<Integer, Integer> allocations) {
        int largest;
        int smallest;
        int largestrange = 0;
        int temp;
        schemeRanges = new HashMap<>();
        for (int scheme : schemeIDs) {
            largest = 0;
            smallest = 10000;
            for (Solicitor sol : solicitors) {
                if (sol.getSchemeId() == scheme) {
                    temp = allocations.get(sol.getSolicitorId());
                    if (temp > largest) {
                        largest = temp;
                    }
                    if (temp < smallest) {
                        smallest = temp;
                    }
                }
                if (largestrange < (largest - smallest)) {
                    largestrange = largest - smallest;
                }
            }
            schemeRanges.put(scheme, largest - smallest);
        }
        float totalRange = 0;
        for (int id : schemeIDs) {
            totalRange = totalRange + schemeRanges.get(id);
        }
        float size = schemeIDs.size();
        if (allocations == policeAllocations) {
            policeAverage = totalRange / size;
        } else if (allocations == courtAllocations) {
            courtAverage = totalRange / size;
        } else if (allocations == totalAllocations) {
            rangeAverage = totalRange / size;
        }
        return largestrange;
    }

    public float evaluateRota(ArrayList<RotaColumn> columnList) {
        calculateClashes(columnList);
        totalRange = calculateRange(totalAllocations);
        courtRange = calculateRange(courtAllocations);
        policeRange = calculateRange(policeAllocations);
        calculateDaysRange();
        calculateSeperation(columnList);

        float fitness;

        fitness = clashes * clashWeighting + totalRange * rangeWeighting + rangeAverage * rangeAverageWeighting
                + daysRangeAverage * daysWeighting
                + courtRange * courtRangeWeighting + courtAverage * courtAverageWeighting
                + policeRange * policeRangeWeighting + policeAverage * policeAverageWeighting
                + colSeperation * seperationWeighting;
        this.fitness = fitness;
        return fitness;
    }


    public void printStats() {
        System.out.println("Clashes = " + clashes);
        System.out.println("Largest TotalRange = " + totalRange + " Average TotalRange = " + rangeAverage);
        System.out.println("Largest PoliceRange = " + policeRange + " Average PoliceRange = " + policeAverage);
        System.out.println("Largest CourtRange = " + courtRange + " Average CourtRange = " + courtAverage);
        System.out.println("Largest DayRange = " + daysRange + " Average DayRange = " + daysRangeAverage);
        System.out.println("ColSep = " + colSeperation);
    }


    public void setSolicitors(ArrayList<Solicitor> solicitors) {
        this.solicitors = solicitors;
    }

    public void setTotalAllocations(HashMap<Integer, Integer> totalAllocations) { this.totalAllocations = totalAllocations; }

    public void setCourtAllocations(HashMap<Integer, Integer> courtAllocations) { this.courtAllocations = courtAllocations; }

    public void setPoliceAllocations(HashMap<Integer, Integer> policeAllocations) { this.policeAllocations = policeAllocations; }

    public void setDayAllocations(HashMap<Integer, ArrayList<Integer>> dayAllocations) { this.dayAllocations = dayAllocations; }

    public void setSchemeIDs(ArrayList<Integer> schemeIDs) { this.schemeIDs = schemeIDs; }

    public float getRangeAverage() { return rangeAverage; }

    public int getTotalRange() { return totalRange; }

    public int getClashes() { return clashes; }

    public float getFitness() { return fitness; }

    public float getDaysRangeAverage() { return daysRangeAverage; }

    public int getDaysRange() { return daysRange; }

    public float getCourtAverage() { return courtAverage; }

    public float getPoliceAverage() { return policeAverage; }

    public int getCourtRange() { return courtRange; }

    public int getPoliceRange() { return policeRange; }

    public int getColSeperation() { return colSeperation; }
}
