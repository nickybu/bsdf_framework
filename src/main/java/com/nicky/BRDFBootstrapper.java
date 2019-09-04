package com.nicky;

import com.google.gson.JsonObject;
import com.nicky.brdfs.BRDF;
import com.nicky.factories.BRDFFactory;

/**
 * <h1>BRDF Bootstrapper</h1>
 * Bootstraps the BRDF creation using the Factories
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class BRDFBootstrapper {

    private BRDFFactory brdfFactory;

    /**
     * Sets up the correct BRDF Factory
     *
     * @param brdfFactory The BRDFFactory to be used
     */
    public void setBrdfFactory(BRDFFactory brdfFactory) {
        this.brdfFactory = brdfFactory;
    }

    /**
     * Sets the properties of the BRDF and creates a new instance of the appropriate BRDF
     *
     * @param properties BRDF properties
     * @return BRDF BRDF instance.
     */
    public BRDF setupBRDF(JsonObject properties) throws IndexOutOfBoundsException, IllegalArgumentException {
        try {
            brdfFactory.setProperties(properties);
            return brdfFactory.createBRDF();
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw e;
        }
    }
}
