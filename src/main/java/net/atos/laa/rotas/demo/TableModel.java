package net.atos.laa.rotas.demo;

import java.util.ArrayList;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.rota.ColumnType;


public class TableModel {
    private String title;
    private ColumnType type;
    private ArrayList<Scheme> schemes;
    private String pattern;

    public TableModel() {

    }

    public TableModel(String title, ColumnType type, ArrayList<Scheme> schemes, String pattern){
        setTitle(title);
        setType(type);
        setScheme(schemes);
        setPattern(pattern);
    }

    // getters

    public String getTitle() {
        return title;
    }

    public ColumnType getType() {
        return type;
    }

    public ArrayList<Scheme> getScheme() {
        return schemes;
    }

    public String getPattern() {
        return pattern;
    }

    // setters

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public void setScheme(ArrayList<Scheme> schemes) {
        this.schemes = schemes;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
