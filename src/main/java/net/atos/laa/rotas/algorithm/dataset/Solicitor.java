package net.atos.laa.rotas.algorithm.dataset;

public class Solicitor {

  private final int solicitorId;
  private final String name;
  private final int firmId;
  private final int schemeId;

  public Solicitor(int solicitorId, String name, int firmId, int schemeId) {
    this.solicitorId = solicitorId;
    this.name = name;
    this.firmId = firmId;
    this.schemeId = schemeId;
  }

  public int getSolicitorId() {
    return solicitorId;
  }

  public String getName() {
    return name;
  }

  public int getFirmId() {
    return firmId;
  }

  public int getSchemeId() {
    return schemeId;
  }
}
