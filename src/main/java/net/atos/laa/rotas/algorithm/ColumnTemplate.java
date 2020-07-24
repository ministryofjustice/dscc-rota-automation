package net.atos.laa.rotas.algorithm;

import java.time.LocalDate;
import java.util.ArrayList;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.rota.ColumnType;

public class ColumnTemplate {
    private ColumnType type;
    private String title;
    private ArrayList<Scheme> schemes;
    private int[] blanksPattern;
    private ArrayList<BlanksModifier> blanksPatternModifiers;
    private ArrayList<LocalDate> blankSlots;

    public ColumnTemplate(ColumnType type, String title, ArrayList<Scheme> schemes,
                          int[] blanksPattern, ArrayList<BlanksModifier> blanksPatternModifiers) {
        this.type = type;
        this.title = title;
        this.schemes = schemes;
        this.blanksPattern = blanksPattern;
        this.blanksPatternModifiers = blanksPatternModifiers;
    }

    /**
     * Expands the specified pattern of blanks out into a list of dates
     * @param startDate The start date of the rota
     * @param endDate The end date of the rota
     */
    public void expandBlanksPattern(LocalDate startDate, LocalDate endDate) {
        blankSlots = new ArrayList<>();

        // Add the dates described by the specified pattern
        if (blanksPattern != null) {
            LocalDate end = endDate.plusDays(1);
            int[] weekCopy = blanksPattern.clone();

            for (LocalDate tempDate = startDate; !tempDate.equals(end);) {
                int day = tempDate.getDayOfWeek().getValue() - 1;

                if (weekCopy[day] == 1) {
                    blankSlots.add(tempDate);

                    if (blanksPattern[day] > 1) {
                        weekCopy[day] = blanksPattern[day];
                    }

                } else if (weekCopy[day] < 0) {
                    blankSlots.add(tempDate);
                    blanksPattern[day] = -1 * blanksPattern[day];
                    weekCopy[day] = blanksPattern[day];

                } else if (weekCopy[day] > 1) {
                    weekCopy[day] = weekCopy[day] - 1;
                }

                tempDate = tempDate.plusDays(1);
            }
        }

        // Add or remove individual dates from the list created by the pattern
        if (blanksPatternModifiers != null) {
            for (BlanksModifier date : blanksPatternModifiers) {
                if (date.getModifierType() == BlanksModifierType.ADD) {
                    if (!blankSlots.contains(date.getDate())) {
                        blankSlots.add(date.getDate());
                    }
                }

                else {
                    System.out.println("remove");
                    if (blankSlots.contains(date.getDate())) {
                        blankSlots.remove(date.getDate());
                    }
                }
            }
        }
    }

    public ColumnType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Scheme> getSchemes() {
        return schemes;
    }

    public ArrayList<LocalDate> getBlankSlots() {
        return blankSlots;
    }

    public String getChosenModifiers() {
        if (blanksPattern != null) {
            if (blanksPatternModifiers != null) {
                return "P & NP";
            }
            else {
                return "P";
            }
        } else {
            if (blanksPatternModifiers != null) {
                return "NP";
            } else {
                return "Every Day";
            }
        }
    }
}
