package net.atos.laa.rotas.demo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.atos.laa.rotas.algorithm.BlanksModifier;
import net.atos.laa.rotas.algorithm.ColumnTemplate;
import net.atos.laa.rotas.algorithm.RotaManager;
import net.atos.laa.rotas.algorithm.Serialisation;
import net.atos.laa.rotas.algorithm.dataset.Dataset;
import net.atos.laa.rotas.algorithm.dataset.ExcelDataset;
import net.atos.laa.rotas.algorithm.dataset.SQLiteDataset;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.rota.ColumnType;

public class MainWindowController implements Initializable {

  private static Stage columnsStage;
  public TextField sheetName;

  public TableView<TableModel> table; // this is the Table
  public TableColumn<TableModel, String> columnTitle;
  public TableColumn<TableModel, ColumnType> columnType;
  public TableColumn<TableModel, Scheme> columnScheme;
  public TableColumn<TableModel, String> columnPattern;

  @FXML
  private TextField datasetPath;
  @FXML
  private TextField outputFolder;
  public Button btnSheetNameEntered;
  public Button btnGenerate;
  public Button btnCancel;
  public Button btnRemove;
  public Button btnAdd;
  public Button btnDestination;
  public Button btnChooseFile;
  public Button btnReset;
  public DatePicker dateStart;
  public DatePicker dateEnd;
  public File directory;

  private String datasetFile;
  public ArrayList<ColumnTemplate> columns;
  private Dataset importedDataset;
  private String datasetType;
  private String outputPath;

  private ObservableList<TableModel> tableData() {
    ObservableList<TableModel> tableRows = FXCollections.observableArrayList();
    for (int i = 0; i < columns.size(); i++) {
      tableRows.add(new TableModel(this.columns.get(i).getTitle(), this.columns.get(i).getType(),
          this.columns.get(i).getSchemes(), this.columns.get(i).getChosenModifiers()));
    }
    return tableRows;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    columnTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

    columnType.setCellValueFactory(new PropertyValueFactory<>("type"));

    columnScheme.setCellValueFactory(new PropertyValueFactory<>("scheme"));

    columnPattern.setCellValueFactory(new PropertyValueFactory<>("pattern"));

  }

  static public Stage getPrimaryStage() {
    return MainWindowController.columnsStage;
  }

  private void setPrimaryStage(Stage stage) {
    MainWindowController.columnsStage = stage;
  }


  /**
   * Triggered when the choose dataset button is clicked
   */
  public void chooseDatasetClicked(ActionEvent actionEvent) throws IOException {
    FileChooser chooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
        "Excel files, SQL files", "*.xlsx", "*.xls", "*.xlsm", "*.sq3");
    chooser.getExtensionFilters().add(extFilter);
    File selectedFile = chooser.showOpenDialog(getPrimaryStage());

    if (selectedFile != null) {
      datasetPath.setText(selectedFile.getAbsolutePath());
      datasetFile = selectedFile.getAbsolutePath();
      datasetType = null;

      if (selectedFile.toString().endsWith(".sq3")) {
        datasetType = "sql";
        loadSchemes();
        sheetName.setEditable(false);
        sheetName.setDisable(true);
        btnSheetNameEntered.setDisable(true);

      } else {
        sheetName.setEditable(true);
        sheetName.setDisable(false);
        btnSheetNameEntered.setDisable(false);
        datasetType = "excel";
      }
    }
  }

  /**
   * Triggered when the sheet name entered button is clicked
   */
  public void sheetNameEnteredClicked(ActionEvent actionEvent) {
    this.columns = new ArrayList<>();
    if (sheetName.getText().length() > 0) {
      loadSchemes();
    }
    table.setItems(tableData());
  }

  /**
   * Initialises the dataset and gets a list of all schemes in it
   */
  private void loadSchemes() {
    if (datasetType.equals("excel")) {
      try {
        importedDataset = new ExcelDataset(datasetFile, sheetName.getText());
        updateDatesAndButtonsState();

      } catch (Exception e) {
        if (e.getMessage() == "No sheet exists in the workbook with the specified name") {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setHeaderText("No sheet exists in the workbook with the specified name");
          alert.showAndWait();

        } else {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setHeaderText("An error occurred");
          alert.showAndWait();
        }
      }

    } else {
      try {
        importedDataset = new SQLiteDataset(datasetFile);
        updateDatesAndButtonsState();
        this.columns = new ArrayList<>();

      } catch (Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("An error occurred");
        alert.showAndWait();
      }
    }


  }


  /**
   * Triggered when the add column button is clicked
   */
  public void addColumnClicked(ActionEvent AddColumnEvent) throws IOException {
    showColumnDialog(dateStart.getValue(), dateEnd.getValue(), importedDataset.getSchemes());

  }


  public Stage showColumnDialog(LocalDate start, LocalDate end, ArrayList<Scheme> schemes)
      throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
        "../../../../../../resources/ColumnWindow.fxml"));
    Parent newRoot = fxmlLoader.load();

    Stage columnStage = new Stage();
    setPrimaryStage(columnStage);

    columnStage.setTitle("New Column");
    columnStage.setScene(new Scene(newRoot));
    columnStage.setResizable(false);

    ColumnWindowController columnWindowController = fxmlLoader.getController();
    columnWindowController.getData(start, end, schemes, this);

    columnStage.show();

    return columnStage;
  }

  /**
   * Triggered when the end date is selected
   */
  public void endDateSelected(ActionEvent actionEvent) {
    if (dateEnd != null && dateStart != null) {
      btnAdd.disableProperty().bind(
          dateEnd.valueProperty().isNull().or(dateStart.valueProperty().isNull()));
      btnRemove.disableProperty().bind(
          dateEnd.valueProperty().isNull().or(dateStart.valueProperty().isNull()));
      updateDatesAndButtonsState();

    } else {
      btnAdd.disableProperty().bind(
          dateEnd.valueProperty().isNotNull().or(dateStart.valueProperty().isNotNull()));
    }
  }

  /**
   * Enables and disables date selectors and output selection elements based on current state
   */
  private void updateDatesAndButtonsState() {
    if (dateEnd.isDisabled() && dateStart.isDisabled()) {
      dateEnd.setDisable(false);
      dateStart.setDisable(false);
    }

    if (!btnAdd.isDisabled()) {
      btnDestination.setDisable(false);
    }
  }

  /**
   * Triggered when the choose output folder button is clicked
   */
  public void chooseOutputFolderClicked(ActionEvent actionEvent) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    directory = dirChooser.showDialog(getPrimaryStage());

    if (directory != null) {
      outputPath = (directory.getAbsolutePath()) + "/output.xlsx";
      outputFolder.setText(outputPath);
      btnGenerate.setDisable(false);
    }
  }

  /**
   * Triggered when the generate rota button is clicked
   */
  public void generateClicked(ActionEvent actionEvent) {
    try {
      System.out.println(this.columns.get(0).getTitle());
      RotaManager manager = new RotaManager(importedDataset, dateStart.getValue(),
          dateEnd.getValue(), this.columns);

      manager.generateRota();

      Serialisation.exportRota(outputPath, manager.getBestRota(), manager.getFirmsUsed());
      importedDataset.disconnect();

    } catch (Exception e) {
      importedDataset.disconnect();
      e.printStackTrace();
    }
  }

  /**
   * Searches for a scheme name in the specified list of schemes
   *
   * @param schemes The list of schemes to search
   * @param name The name of the scheme to search for
   * @return A scheme object corresponding to the specified name, or null if the name was not found
   */
  private Scheme findScheme(ArrayList<Scheme> schemes, String name) {
    for (Scheme scheme : schemes) {
      if (scheme.getName().equals(name)) {
        return scheme;
      }
    }

    return null;
  }

  /**
   * Triggered when the cancel button is clicked
   */
  public void cancelClicked(ActionEvent actionEvent) {
    Stage s = Main.getPrimaryStage();
    s.close();
  }

  public void gatherColumnData(ColumnType type, String title, ArrayList<Scheme> schemes,
      int[] blanksPattern, ArrayList<BlanksModifier> blanksModifiers) {
    this.columns.add(new ColumnTemplate(type, title, schemes, blanksPattern, blanksModifiers));
    table.setItems(tableData());
    checkNumColumns();
  }

  public void removeColumnClicked(ActionEvent actionEvent) {
    if (this.columns.size() > 0) {
      int index = table.getSelectionModel().getSelectedIndex();
      this.columns.remove(index);
      table.setItems(tableData());
    }
    checkNumColumns();
  }

  private void checkNumColumns() {
    btnReset.setDisable(this.columns.size() <= 0);
  }

  public void resetClicked(ActionEvent actionEvent) {
    resetColumns();
  }

  private void resetColumns() {
    this.columns = new ArrayList<>();
    table.setItems(tableData());
    checkNumColumns();
  }

}
