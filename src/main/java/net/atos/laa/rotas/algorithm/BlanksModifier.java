package net.atos.laa.rotas.algorithm;

import java.time.LocalDate;

public class BlanksModifier {

  private final BlanksModifierType modifierType;
  private final LocalDate date;
  private String slotText;

  public BlanksModifier(BlanksModifierType modifierType, LocalDate date) {
    this.modifierType = modifierType;
    this.date = date;
  }

  public BlanksModifier(BlanksModifierType modifierType, LocalDate date, String slotText) {
    this.modifierType = modifierType;
    this.date = date;
    this.slotText = slotText;
  }

  public BlanksModifierType getModifierType() {
    return modifierType;
  }

  public LocalDate getDate() {
    return date;
  }
}
