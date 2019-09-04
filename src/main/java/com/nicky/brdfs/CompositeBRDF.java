package com.nicky.brdfs;

import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <h1>Composite BRDF</h1>
 * Represents a Composite BRDF made up of other BRDF sub-components
 * Each sub-component has a weighting.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class CompositeBRDF implements BRDF {

    private String name;

    /**
     * Stores BRDF sub-components and their weighting
     */
    private LinkedHashMap<BRDF, Float> components;

    public CompositeBRDF(String name, LinkedHashMap<BRDF, Float> components) {
        this.name = name;
        this.components = components;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out) {

        Spectrum reflectance = new Spectrum();

        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            Spectrum s = entry.getKey().f(in, out);
            s.mul(entry.getValue());
            reflectance.add(s);
        }

        return reflectance;
    }

    @Override
    public Spectrum f(Vector3f in, Vector3f out, String reflectionType) {
        Spectrum reflectance = new Spectrum();

        boolean noComponents = true;

        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            if (entry.getKey().getReflectionType().equals(reflectionType)) {
                Spectrum s = entry.getKey().f(in, out);
                s.mul(entry.getValue());
                reflectance.add(s);
                noComponents = false;
            }
        }

        if (noComponents) {
            return null;
        }

        return reflectance;
    }

    @Override
    public Pair<Vector3f, Spectrum> sampleF(Vector3f in, Vector3f normal) {

        Vector3f reflectedRay = new Vector3f();
        Spectrum reflectance = new Spectrum();

        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            Pair<Vector3f, Spectrum> samplef = entry.getKey().sampleF(in, normal);
            reflectedRay.add(samplef.getKey());
            samplef.getValue().mul(entry.getValue());
            reflectance.add(samplef.getValue());
        }

        reflectance.div(this.getComponents().size());
        reflectedRay.div(this.getComponents().size());

        return new Pair<>(reflectedRay, reflectance);
    }

    @Override
    public LinkedHashMap<String, Pair<String, String>> getParameters() {
        LinkedHashMap<String, Pair<String, String>> parameters = new LinkedHashMap<>();

        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            parameters.put(entry.getKey().getName(), null); // BRDF name
            LinkedHashMap<String, Pair<String, String>> componentParams = entry.getKey().getParameters();
            for (Map.Entry<String, Pair<String, String>> params : componentParams.entrySet()) {
                parameters.put(params.getKey() + "_" + entry.getKey().getName(), params.getValue());
            }

        }
        return parameters;
    }

    @Override
    public String getReflectionType() {
        return null;
    }

    public String getName() {
        return name;
    }

    public LinkedHashMap<BRDF, Float> getComponents() {
        return components;
    }

    @Override
    public String serialise() {
        StringBuilder sb = new StringBuilder();
        int numComponents = 0;
        sb.append("\"components\": [");
        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            numComponents++;
            sb.append("{");
            sb.append("\"name\": ").append("\"").append(entry.getKey().getName()).append("\",");
            if (entry.getKey().getClass().equals(CompositeBRDF.class)) {
                sb.append("\"type\": \"composite\",");
            } else {
                sb.append("\"type\": \"simple\",");
            }
            sb.append("\"weighting\": " + entry.getValue() + ",");
            sb.append(entry.getKey().serialise());
            if (numComponents < components.entrySet().size()) {
                sb.append("},");
            } else {
                sb.append("}");
            }
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Uses Monte Carlo methods to select a BRDF component to evaluate based upon a set of weightings
     *
     * @return BRDF to be evaluated
     */
    private BRDF russianRoulette() {
        // Get number between 0.1 and 1.0
        float randomNum = (float) (Math.random());

        for (Map.Entry<BRDF, Float> entry : components.entrySet()) {
            if (randomNum <= entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
