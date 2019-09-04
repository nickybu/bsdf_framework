package com.nicky.brdfs;


import com.nicky.Spectrum;
import javafx.util.Pair;
import org.joml.Vector3f;

import java.util.LinkedHashMap;

/**
 * <h1>BRDF Interface</h1>
 * The BRDF interface is a collection of abstract methods inherited by any BRDF implementation.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public interface BRDF {

    /**
     * Calculates the reflection at an assumed point x given the incoming and outgoing directions
     * (not spatially varying)
     *
     * @param in  The incident ray's source position
     * @param out The outward ray's source position
     * @return Spectrum Returns the reflection RGB values encapsulated as a Spectrum.
     */
    Spectrum f(Vector3f in, Vector3f out);

    /**
     * Calculates the reflection of a specific component (diffuse/specular) at an assumed point x given the incoming and outgoing directions
     * (not spatially varying)
     *
     * @param in             Incident Ray
     * @param out            Outgoing Ray
     * @param reflectionType Diffuse or Specular
     * @return Spectrum Returns the reflection Spectrum.
     */
    Spectrum f(Vector3f in, Vector3f out, String reflectionType);

    /**
     * Approximates the outgoing direction and calculates the reflection at an assumed point x
     *
     * @param in     Incident Ray
     * @param normal The normal
     * @return Pair<Vector3f   ,       Spectrum> Returns the outgoing direction and reflection Spectrum.
     */
    Pair<Vector3f, Spectrum> sampleF(Vector3f in, Vector3f normal);

    /**
     * Gets the name of the BRDF
     *
     * @return String The brdf name.
     */
    String getName();

    /**
     * Gets the reflection type
     *
     * @return String Diffuse or Specular.
     */
    String getReflectionType();

    /**
     * Return the BRDFs parameters to dynamically build the user interface in an external viewing tool
     *
     * @return LinkedHashMap<String   ,       Pair   <   String   ,       String>> Returns a LinkedHashMap<Parameter Variable Type, Pair<Parameter Name, Parameter Value>>
     */
    LinkedHashMap<String, Pair<String, String>> getParameters();

    /**
     * Serialise BRDF parameters in JSON format
     *
     * @return String The serialised BRDF
     */
    String serialise();
}
