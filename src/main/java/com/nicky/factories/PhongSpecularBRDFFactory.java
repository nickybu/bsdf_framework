package com.nicky.factories;

import com.google.gson.JsonObject;
import com.nicky.Spectrum;
import com.nicky.brdfs.BRDF;
import com.nicky.brdfs.PhongSpecularBRDF;

/**
 * <h1>Phong Specular BRDF Factory</h1>
 * Represents a Factory for the Phong Specular BRDF
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class PhongSpecularBRDFFactory implements BRDFFactory {

    private Spectrum specularReflectivity = new Spectrum();

    private float specularExponent;

    @Override
    public void setProperties(JsonObject propertiesJSON) throws IndexOutOfBoundsException, IllegalArgumentException {

        try {
            specularReflectivity.setR(propertiesJSON.getAsJsonArray("specularReflectivity").get(0).getAsFloat());
            specularReflectivity.setG(propertiesJSON.getAsJsonArray("specularReflectivity").get(1).getAsFloat());
            specularReflectivity.setB(propertiesJSON.getAsJsonArray("specularReflectivity").get(2).getAsFloat());
            specularExponent = propertiesJSON.get("specularExponent").getAsFloat();
            specularReflectivity.validate();
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public BRDF createBRDF() {
        return new PhongSpecularBRDF(specularReflectivity, specularExponent);
    }
}
