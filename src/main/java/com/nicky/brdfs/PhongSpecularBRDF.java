package com.nicky.brdfs;

import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;

/**
 * <h1>Phong Specular BRDF</h1>
 * Represents the specular component of the Normalised Phong BRDF
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class PhongSpecularBRDF implements BRDF {

    private Spectrum specularReflectivity;

    /**
     * Specular exponent of the surface
     * 0: dull
     * The higher the value is, the glossier the surface is, i.e. sharper specular reflections
     */
    private float specularExponent;


    public PhongSpecularBRDF(Spectrum specularReflectivity, float specularExponent) {
        this.specularExponent = specularExponent;
        this.specularReflectivity = specularReflectivity;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out) {
        // Find reflected ray for perfect specular reflection
        Vector3f normal = new Vector3f(1, 0, 0).normalize();
        Vector3f reflectedRay = new Vector3f();
        in.reflect(normal, reflectedRay);

        // Angle between perfect specular reflective direction and outgoing direction
        float alpha = out.dot(reflectedRay);
        alpha = Math.max(0, alpha);
//        if (alpha > 0) {
//            System.out.println("In: " + in.toString());
//            System.out.println("Out: " + out.toString());
//            System.out.println(alpha);
//
//        }
        float alpha_ = (float) Math.pow(alpha, specularExponent);

        // Calculate reflectance (ignore ambient)
        Spectrum reflectance = specularReflectivity.copy();
        reflectance.mul(alpha_);

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
        // Find reflection direction
        Vector3f reflectedRay = new Vector3f();

        float cosND = Math.max(-in.dot(normal), 0);
        float dn = 2 * cosND;
        reflectedRay.x = (dn * normal.x) + in.x;
        reflectedRay.y = (dn * normal.y) + in.y;
        reflectedRay.z = (dn * normal.z) + in.z;

        // Calculate specular reflection
        Spectrum reflectance = specularReflectivity.copy();
        reflectance.mul(specularExponent);

        return new Pair<>(reflectedRay, reflectance);
    }

    @Override
    public LinkedHashMap<String, Pair<String, String>> getParameters() {
        LinkedHashMap<String, Pair<String, String>> parameters = new LinkedHashMap<>();
        Pair<String, String> pair1 = new Pair<>("Specular Reflectivity", specularReflectivity.toString());
        Pair<String, String> pair2 = new Pair<>("Specular Exponent", String.valueOf(specularExponent));

        parameters.put("Spectrum", pair1);
        parameters.put("float", pair2);
        return parameters;
    }

    @Override
    public String getName() {
        return "PhongSpecularBRDF";
    }

    @Override
    public String getReflectionType() {
        return "specular";
    }

    @Override
    public String serialise() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"specularExponent\": ").append(specularExponent).append(",");
        sb.append("\"specularReflectivity\": [").append(specularReflectivity.toString()).append("]");
        return sb.toString();
    }
}
