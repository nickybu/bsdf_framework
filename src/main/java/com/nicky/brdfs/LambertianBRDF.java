package com.nicky.brdfs;

import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;

/**
 * <h1>Lambertian BRDF</h1>
 * Represents a Lambertian BRDF for diffuse surfaces
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class LambertianBRDF implements BRDF {

    private Spectrum reflectivity;

    public LambertianBRDF(Spectrum reflectivity) {
        this.reflectivity = reflectivity;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out) {

        Spectrum reflectance = reflectivity.copy();
        reflectance.div((float) Math.PI);

        return reflectance;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out, String reflectionType) {
        if (reflectionType.equals(getReflectionType())) {
            return f(in, out);
        }
        return null;
    }

    @Override
    public Pair<Vector3f, Spectrum> sampleF(Vector3f in, Vector3f normal) {
        Vector3f reflectedRay = new Vector3f();

        float cosND = Math.max(-in.dot(normal), 0);
        float dn = 2 * cosND;
        reflectedRay.x = (dn * normal.x) + in.x;
        reflectedRay.y = (dn * normal.y) + in.y;
        reflectedRay.z = (dn * normal.z) + in.z;

        return new Pair<>(reflectedRay, null);
    }

    @Override
    public LinkedHashMap<String, Pair<String, String>> getParameters() {
        LinkedHashMap<String, Pair<String, String>> parameters = new LinkedHashMap<>();
        Pair<String, String> pair = new Pair<>("Diffuse Reflectivity", reflectivity.toString());

        parameters.put("Spectrum", pair);
        return parameters;
    }

    public Spectrum getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(Spectrum reflectivity) {
        this.reflectivity = reflectivity;
    }

    public String getName() {
        return "LambertianBRDF";
    }

    @Override
    public String getReflectionType() {
        return "diffuse";
    }

    @Override
    public String serialise() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"reflectivity\": [").append(reflectivity.toString()).append("]");
        return sb.toString();
    }
}
