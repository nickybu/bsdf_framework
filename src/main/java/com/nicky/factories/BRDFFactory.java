package com.nicky.factories;

import com.google.gson.JsonObject;
import com.nicky.brdfs.BRDF;

/**
 * <h1>BRDF Factory</h1>
 * Represents a Factory in the Factory/Registry Design Pattern to arbitrarily create instances of BRDF class with default values
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public interface BRDFFactory {
    /**
     * Sets up default values for a BRDF
     *
     * @param propertiesJSON A JSON structuring the values of a BRDF
     */
    void setProperties(JsonObject propertiesJSON) throws IndexOutOfBoundsException;

    /**
     * Creates an instance of the relevant BRDF and initialises it with default values
     *
     * @return BRDF A BRDF instance.
     */
    BRDF createBRDF();
}
