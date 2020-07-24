package net.atos.laa.rotas.algorithm.rota;

import net.atos.laa.rotas.algorithm.dataset.Solicitor;

/**
 * A data structure representing a single slot in the rota. Each slot references the solicitor
 * assigned to it. If a slot is blank it means that a solicitor will not be assigned to it
 */
public class RotaSlot {

  private Solicitor solicitor;
  private boolean isBlank;

  public RotaSlot() {
    isBlank = false;
  }

  public Solicitor getSolicitor() {
    return solicitor;
  }

  public void setSolicitor(Solicitor solicitor) {
    this.solicitor = solicitor;
  }

  public boolean getIsBlank() {
    return isBlank;
  }

  public void setIsBlank(boolean isBlank) {
    this.isBlank = isBlank;
  }
}
