package net.atos.laa.rotas.algorithm.dataset;

public class Firm {
    private int firmId;
    private String name;

    public Firm(int firmId, String name) {
        this.firmId = firmId;
        this.name = name;
    }

    public int getFirmId() {
        return firmId;
    }

    public String getName() {
        return name;
    }
}
