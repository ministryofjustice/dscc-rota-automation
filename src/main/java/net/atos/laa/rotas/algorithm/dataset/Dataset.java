package net.atos.laa.rotas.algorithm.dataset;

import java.util.ArrayList;

/**
 * Manages access to a single source of rota data. Enables access to the list
 * of schemes and the list of solicitors for each scheme, that are stored in
 * the dataset.
 */
public interface Dataset {

    /**
     * Queries the dataset to extract all of the schemes
     *
     * @return Returns a list of schemes and their IDs
     */
    public ArrayList<Scheme> getSchemes();

    /**
     * Queries the dataset to extract all of the solicitors for a specific
     * scheme
     *
     * @param schemeId The ID of the scheme to get the solicitors for
     * @return Returns a list of solicitors
     */
    public ArrayList<Solicitor> getSolicitorsForScheme(int schemeId);

    /**
     * Close the connection to the dataset file
     *
     * @return Returns <code>false</code> if there was an error
     */
    public boolean disconnect();
}
