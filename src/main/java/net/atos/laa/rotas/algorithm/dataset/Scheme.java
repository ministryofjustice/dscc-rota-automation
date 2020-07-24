package net.atos.laa.rotas.algorithm.dataset;

public class Scheme {
    private int schemeId;
    private String name;

    /**
     * @param schemeId The unique ID of the scheme
     * @param name The name of the scheme
     */

    public Scheme(int schemeId, String name) {
        this.schemeId = schemeId;
        this.name = name;
    }

    public int getSchemeId() {
        return schemeId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
