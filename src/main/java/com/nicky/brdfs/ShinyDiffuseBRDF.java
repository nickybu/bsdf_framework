package com.nicky.brdfs;

import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;

/**
 * <h1>ShinyDiffuse BRDF</h1>
 * Represents the ShinyDiffuse BRDF found in Sunflow renderer.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class ShinyDiffuseBRDF implements BRDF {

    /**
     * Specular exponent of the surface
     * 0: dull
     * The higher the value is, the glossier the surface is, i.e. sharper specular reflections
     */
    private float reflection;
    private Spectrum diffuseReflectivity;

    public ShinyDiffuseBRDF(Spectrum diffuseReflectivity, float reflection) {
        this.reflection = reflection;
        this.diffuseReflectivity = diffuseReflectivity;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out) {

        Spectrum reflectance = diffuseReflectivity.copy();

        // Diffuse
        reflectance.div((float) Math.PI);

        if (reflection == 0) {
            return reflectance;
        }

        // Specular
        Vector3f normal = new Vector3f(1, 0, 0).normalize();
        Vector3f reflectedRay = new Vector3f();
        in.reflect(normal, reflectedRay);

        // Angle between perfect specular reflective direction and outgoing direction
        float alpha = out.dot(reflectedRay);
        alpha = Math.max(0, alpha);
        float alpha_ = (float) Math.pow(alpha, reflection);

        // Calculate reflectance (ignore ambient)
        reflectance.mul(alpha_);

        return reflectance;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out, String reflectionType) {

        if (reflectionType.equals("diffuse")) {
            // Diffuse
            Spectrum reflectance = diffuseReflectivity.copy();
            reflectance.div((float) Math.PI);
            return reflectance;
        } else if (reflectionType.equals("specular")) {
            // Specular
            // The probability of the incident and outward directions being perfectly specular is 0
            return new Spectrum(0, 0, 0);
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
        Spectrum reflectance = diffuseReflectivity.copy();
        reflectance.mul(reflection);

        return new Pair<>(reflectedRay, reflectance);
    }

    @Override
    public LinkedHashMap<String, Pair<String, String>> getParameters() {
        LinkedHashMap<String, Pair<String, String>> parameters = new LinkedHashMap<>();
        Pair<String, String> pair1 = new Pair<>("Diffuse Reflectivity", diffuseReflectivity.toString());
        Pair<String, String> pair2 = new Pair<>("Reflection", String.valueOf(reflection));

        parameters.put("Spectrum", pair1);
        parameters.put("float", pair2);
        return parameters;
    }

    @Override
    public String getName() {
        return "ShinyDiffuseBRDF";
    }

    @Override
    public String getReflectionType() {
        return "both";
    }

    @Override
    public String serialise() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"diffuseReflectivity\": [").append(diffuseReflectivity.toString()).append("],");
        sb.append("\"reflection\": ").append(reflection);
        return sb.toString();
    }
}