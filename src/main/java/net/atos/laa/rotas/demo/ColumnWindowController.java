package net.atos.laa.rotas.demo;


import static net.atos.laa.rotas.algorithm.rota.ColumnType.COURT;
import static net.atos.laa.rotas.algorithm.rota.ColumnType.POLICE;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import net.atos.laa.rotas.algorithm.BlanksModifier;
import net.atos.laa.rotas.algorithm.BlanksModifierType;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.rota.ColumnType;

public class ColumnWindowController implements Initializable {

  public ListView<Scheme> TargetList = new ListView<>();
  public ListView<Scheme> SourceList = new ListView<>();
  public Button addScheme;
  public Button removeScheme;
  public Button btnAdd;
  public TextField columnTitle;
  public CheckBox patternCheckBox;
  public CheckBox noPatternCheckBox;
  public DatePicker dateChooser;
  public ListView<LocalDate> noPatternAdded = new ListView<>();
  public ListView<LocalDate> noPatternRemoved = new ListView<>();
  public Button btnAddedAdd;
  public Button btnAddedRemove;
  public Button btnRemovedAdd;
  public Button btnRemovedRemove;


  private LocalDate startDate, endDate;
  private ArrayList<Scheme> allSchemes;
  public ComboBox<ColumnType> typeList;
  public ComboBox<String> recurrenceMonday;
  public ComboBox<String> recurrenceTuesday;
  public ComboBox<String> recurrenceWednesday;
  public ComboBox<String> recurrenceThursday;
  public ComboBox<String> recurrenceFriday;
  public ComboBox<String> recurrenceSaturday;
  public ComboBox<String> recurrenceSunday;

  private ObservableList<Scheme> excludedSchemes;
  private ObservableList<Scheme> includedSchemes;

  private ObservableList<ColumnType> columnTypes;
  private ObservableList<String> recurrenceType;

  private ObservableList<LocalDate> addedDates;
  private ObservableList<LocalDate> removedDates;

  private MainWindowController MWC;

  private int[] blanksPattern = {0, 0, 0, 0, 0, 0, 0};
  private ArrayList<BlanksModifier> blanksModifiers;
  private int numAdded = 0;
  private int numRemoved = 0;

  public void getData(LocalDate start, LocalDate end, ArrayList<Scheme> listSchemes,
      MainWindowController MWC) {
    this.startDate = start;
    this.endDate = end;
    this.allSchemes = new ArrayList<Scheme>(listSchemes);
    this.MWC = MWC;
    excludedSchemes = FXCollections.observableList(allSchemes);

    SourceList.setItems(excludedSchemes);
    includedSchemes = FXCollections.observableArrayList();
    TargetList.setItems(includedSchemes);

    columnTypes = FXCollections.observableArrayList();
    columnTypes.addAll(POLICE, COURT);
    typeList.setItems(columnTypes);

    addedDates = FXCollections.observableArrayList();
    noPatternAdded.setItems(addedDates);

    removedDates = FXCollections.observableArrayList();
    noPatternRemoved.setItems(removedDates);

    limitDateChooser();

    patternBox();

    updatePatterns();

    addScheme.setOnAction(new MoveScheme());
    removeScheme.setOnAction(new MoveScheme());

    btnAddedAdd.setOnAction(new AddDate());
    btnRemovedAdd.setOnAction(new AddDate());

    btnAddedRemove.setOnAction(new RemoveDate());
    btnRemovedRemove.setOnAction(new RemoveDate());

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  private void patternBox() {
    recurrenceType = FXCollections.observableArrayList();
    recurrenceType.addAll("Blank", "Every week", "Every 2 weeks starting 1st week",
        "Every 2 weeks starting 2nd week");
    recurrenceMonday.setItems(recurrenceType);
    recurrenceTuesday.setItems(recurrenceType);
    recurrenceWednesday.setItems(recurrenceType);
    recurrenceThursday.setItems(recurrenceType);
    recurrenceFriday.setItems(recurrenceType);
    recurrenceSaturday.setItems(recurrenceType);
    recurrenceSunday.setItems(recurrenceType);
  }

  private void setDates() {
    if (patternCheckBox.isSelected()) {
      blanksPattern[0] = gatherRecurrenceType(recurrenceMonday.getValue());
      blanksPattern[1] = gatherRecurrenceType(recurrenceTuesday.getValue());
      blanksPattern[2] = gatherRecurrenceType(recurrenceWednesday.getValue());
      blanksPattern[3] = gatherRecurrenceType(recurrenceThursday.getValue());
      blanksPattern[4] = gatherRecurrenceType(recurrenceFriday.getValue());
      blanksPattern[5] = gatherRecurrenceType(recurrenceSaturday.getValue());
      blanksPattern[6] = gatherRecurrenceType(recurrenceSunday.getValue());
    }

    if (noPatternCheckBox.isSelected()) {
      blanksModifiers = new ArrayList<>();

      for (int i = 0; i < this.numAdded; i++) {
        blanksModifiers.add(new BlanksModifier(BlanksModifierType.ADD, addedDates.get(i)));
      }
      for (int i = 0; i < this.numRemoved; i++) {
        blanksModifiers.add(new BlanksModifier(BlanksModifierType.REMOVE, removedDates.get(i)));
      }
    }
  }

  private int gatherRecurrenceType(String s) {
    if (s == "Blank") {
      return 1;
    } else if (s == "Every week") {
      return 0;
    } else if (s == "Every 2 weeks starting 1st week") {
      return 2;
    } else if (s == "Every 2 weeks starting 2nd week") {
      return -2;
    } else {
      return -1;
    }
  }


  private void updatePatterns() {
    noPatternCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
          Boolean newValue) {
        if (newValue) {
          btnAddedAdd.setDisable(false);
          btnAddedRemove.setDisable(false);
          btnRemovedAdd.setDisable(false);
          btnRemovedRemove.setDisable(false);
          noPatternAdded.setDisable(false);
          noPatternRemoved.setDisable(false);
          dateChooser.setDisable(false);

        } else {
          btnAddedAdd.setDisable(true);
          btnAddedRemove.setDisable(true);
          btnRemovedAdd.setDisable(true);
          btnRemovedRemove.setDisable(true);
          noPatternAdded.setDisable(true);
          noPatternRemoved.setDisable(true);
          dateChooser.setDisable(true);
        }
      }
    });

    patternCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
          Boolean newValue) {
        if (newValue) {
          recurrenceMonday.setDisable(false);
          recurrenceTuesday.setDisable(false);
          recurrenceWednesday.setDisable(false);
          recurrenceThursday.setDisable(false);
          recurrenceFriday.setDisable(false);
          recurrenceSaturday.setDisable(false);
          recurrenceSunday.setDisable(false);

        } else {
          recurrenceMonday.setDisable(true);
          recurrenceTuesday.setDisable(true);
          recurrenceWednesday.setDisable(true);
          recurrenceThursday.setDisable(true);
          recurrenceFriday.setDisable(true);
          recurrenceSaturday.setDisable(true);
          recurrenceSunday.setDisable(true);
        }
      }
    });
  }

  private void limitDateChooser() {
    dateChooser.setEditable(false);
    dateChooser.setDayCellFactory(days -> new DateCell() {
      @Override
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        setStyle("-fx-background-color: #99ff99; -fx-text-fill: #000000");
        if (date.isBefore(MWC.dateStart.getValue())) {
          setDisable(true);
          setStyle("-fx-background-color: #ffc0cb; -fx-text-fill: #ffc0cb");
        }
        if (date.isAfter(MWC.dateEnd.getValue())) {
          setDisable(true);
          setStyle("-fx-background-color: #ffc0cb; -fx-text-fill: #ffc0cb");
        }

      }
    });
  }

  public void addClicked(ActionEvent actionEvent) {
    setDates();
    ArrayList<Scheme> schemes = new ArrayList<>(includedSchemes);
    if (!(patternCheckBox.isSelected())) {
      this.blanksPattern = null;
    }

    MainWindowController.getPrimaryStage().close();
    MWC.gatherColumnData(this.typeList.getValue(),
        this.columnTitle.getText(), schemes, blanksPattern, blanksModifiers);
  }

  public void cancelClicked(ActionEvent actionEvent) {
    MainWindowController.getPrimaryStage().close();
  }


  private class MoveScheme implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent event) {

      if (event.getSource().equals(removeScheme)) {
        Scheme str = TargetList.getSelectionModel()
            .getSelectedItem();
        if (str != null) {
          includedSchemes.remove(str);
          excludedSchemes.add(str);
        }
      } else if (event.getSource().equals(addScheme)) {
        Scheme str = SourceList.getSelectionModel()
            .getSelectedItem();
        if (str != null) {
          SourceList.getSelectionModel().clearSelection();
          excludedSchemes.remove(str);
          includedSchemes.add(str);
        }
      }
    }
  }

  private class AddDate implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent event) {

      if (event.getSource().equals(btnAddedAdd)) {
        if (dateChooser.getValue() != null) {
          addedDates.add(dateChooser.getValue());
          dateChooser.setValue(null);
          numAdded++;
        }
      } else if (event.getSource().equals(btnRemovedAdd)) {
        if (dateChooser.getValue() != null) {
          removedDates.add(dateChooser.getValue());
          dateChooser.setValue(null);
          numRemoved++;
        }
      }
    }
  }

  private class RemoveDate implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent event) {

      if (event.getSource().equals(btnAddedRemove)) {
        LocalDate target = noPatternAdded.getSelectionModel().getSelectedItem();
        if (target != null) {
          addedDates.remove(target);
          noPatternAdded.getSelectionModel().clearSelection();
          numAdded--;
        }
      } else if (event.getSource().equals(btnRemovedRemove)) {
        LocalDate target = noPatternRemoved.getSelectionModel().getSelectedItem();
        if (target != null) {
          removedDates.remove(target);
          noPatternRemoved.getSelectionModel().clearSelection();
          numRemoved--;
        }
      }
    }
  }
}


