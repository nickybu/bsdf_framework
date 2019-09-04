package com.nicky.brdfs;

import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;

/**
 * <h1>Phong Diffuse BRDF</h1>
 * Represents the diffuse component of the Normalised Phong BRDF
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class PhongDiffuseBRDF implements BRDF {

    private Spectrum diffuseReflectivity;

    public PhongDiffuseBRDF(Spectrum diffuseReflectivity) {
        this.diffuseReflectivity = diffuseReflectivity;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out) {

        Spectrum reflectance = diffuseReflectivity.copy();
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

        // Calculate specular reflection
        Spectrum reflectance = diffuseReflectivity.copy();

        return new Pair<>(reflectedRay, reflectance);
    }

    @Override
    public LinkedHashMap<String, Pair<String, String>> getParameters() {
        LinkedHashMap<String, Pair<String, String>> parameters = new LinkedHashMap<>();
        Pair<String, String> pair = new Pair<>("Diffuse Reflectivity", diffuseReflectivity.toString());

        parameters.put("Spectrum", pair);
        return parameters;
    }

    @Override
    public String getName() {
        return "PhongDiffuseBRDF";
    }

    @Override
    public String getReflectionType() {
        return "diffuse";
    }

    @Override
    public String serialise() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"diffuseReflectivity\": [").append(diffuseReflectivity.toString()).append("]");
        return sb.toString();
    }
}
