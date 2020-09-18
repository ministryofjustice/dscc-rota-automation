package net.atos.laa.rotas.algorithm;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import net.atos.laa.rotas.algorithm.dataset.Scheme;
import net.atos.laa.rotas.algorithm.rota.ColumnType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ColumnTemplateTest {

  private final ColumnType columnType = ColumnType.COURT;
  private final String title = "Court";
  private static final Scheme scheme = new Scheme(1, "Springfield");
  private static final ArrayList schemes = new ArrayList();
  private ColumnTemplate columnTemplate;

  @BeforeAll
  public static void before() {
    schemes.add(scheme);
  }

  @Test
  public void testTextShownIsEveryDayWhenPatternNotSelectedAndNoPatternNotSelected() {
    columnTemplate = new ColumnTemplate(columnType, title, schemes, null, null);
    String modifiers = columnTemplate.getChosenModifiers();
    assertEquals("Every Day", modifiers);
  }

  @Test
  public void testTextShownIsPIfPatternSelectedAndNoPatternNotSelected() {
    int[] blanksPattern = new int[1];
    columnTemplate = new ColumnTemplate(columnType, title, schemes, blanksPattern, null);
    String modifiers = columnTemplate.getChosenModifiers();
    assertEquals("P", modifiers);
  }

  @Test
  public void testTextShownIsPAndNPIfPatternAndNoPatternSelected() {
    int[] blanksPattern = new int[1];
    ArrayList<BlanksModifier> blanksPatternModifiers = new ArrayList<>();
    columnTemplate = new ColumnTemplate(columnType, title, schemes, blanksPattern, blanksPatternModifiers);
    String modifiers = columnTemplate.getChosenModifiers();
    assertEquals("P & NP", modifiers);
  }

  @Test
  public void testTextShownIsNPIfPatternIsNotSelectedAndNoPatternIsSelected() {
    ArrayList<BlanksModifier> blanksPatternModifiers = new ArrayList<>();
    columnTemplate = new ColumnTemplate(columnType, title, schemes, null, blanksPatternModifiers);
    String modifiers = columnTemplate.getChosenModifiers();
    assertEquals("NP", modifiers);
  }

  @Test
  public void testAddingAnIndividualDateToTheRota() {
    ArrayList<BlanksModifier> blanksPatternModifiers = new ArrayList<>();
    LocalDate localDate = LocalDate.now();
    BlanksModifier blanksModifier = new BlanksModifier(BlanksModifierType.ADD, localDate);
    blanksPatternModifiers.add(blanksModifier);
    columnTemplate = new ColumnTemplate(columnType, title, schemes, null, blanksPatternModifiers);
    columnTemplate.setBlankSlots(new ArrayList<>());
    columnTemplate.addOrRemoveIndividualDates();
    assertEquals(LocalDate.now(), columnTemplate.getBlankSlots().get(0));
  }

  @Test
  public void testRemovingAnIndividualDateFromTheRota() {
    ArrayList<BlanksModifier> blanksPatternModifiers = new ArrayList<>();
    ArrayList<LocalDate> blankSlots = new ArrayList<>();
    LocalDate dayOne = LocalDate.of(2020, 1, 1);
    LocalDate dayTwo = LocalDate.of(2020, 1, 2);
    blankSlots.add(dayOne);
    blankSlots.add(dayTwo);
    BlanksModifier blanksModifier = new BlanksModifier(BlanksModifierType.REMOVE, dayOne);
    blanksPatternModifiers.add(blanksModifier);
    columnTemplate = new ColumnTemplate(columnType, title, schemes, null, blanksPatternModifiers);
    columnTemplate.setBlankSlots(blankSlots);
    columnTemplate.addOrRemoveIndividualDates();
    assertEquals(1, columnTemplate.getBlankSlots().size());
  }


}
