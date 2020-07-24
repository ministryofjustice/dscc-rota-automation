package net.atos.laa.rotas.algorithm.rota;

/**
 * A data structure representing a single column in the rota. It maintains
 * a list of slots as long as the rota, which reference solicitors assigned
 * to them
 */
public class RotaColumn {
    private ColumnType type;
    private String title;
    private RotaSlot[] slots;

    /**
     * Creates a new rota column
     * @param type The type of the column
     * @param title The title of the column
     * @param numberOfSlots The number of slots long this column is
     */
    RotaColumn(ColumnType type, String title, int numberOfSlots) {
        this.type = type;
        this.title = title;
        slots = new RotaSlot[numberOfSlots];

        // Add the specified number of slots to the column
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new RotaSlot();
        }
    }

    public ColumnType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public RotaSlot[] getSlots() {
        return slots;
    }
}
