package com.nicky.factories;

import com.google.gson.JsonObject;
import com.nicky.Spectrum;
import com.nicky.brdfs.BRDF;
import com.nicky.brdfs.PhongDiffuseBRDF;

/**
 * <h1>Lambertian BRDF Factory</h1>
 * Represents a Factory for the Phong Diffuse BRDF
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class PhongDiffuseBRDFFactory implements BRDFFactory {
    private float diffuseReflectivityR;
    private float diffuseReflectivityG;
    private float diffuseReflectivityB;

    private Spectrum reflectivity = new Spectrum();

    @Override
    public void setProperties(JsonObject propertiesJSON) throws IndexOutOfBoundsException, IllegalArgumentException {

        try {
            reflectivity.setR(propertiesJSON.getAsJsonArray("diffuseReflectivity").get(0).getAsFloat());
            reflectivity.setG(propertiesJSON.getAsJsonArray("diffuseReflectivity").get(1).getAsFloat());
            reflectivity.setB(propertiesJSON.getAsJsonArray("diffuseReflectivity").get(2).getAsFloat());
            reflectivity.validate();
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public BRDF createBRDF() {
        return new PhongDiffuseBRDF(reflectivity);
    }
}
