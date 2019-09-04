package com.nicky;

/**
 * <h1>Spectrum</h1>
 * Represents the required complexity of a reflectivity RGB component
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Spectrum {

    // RGB Components
    private float r;
    private float g;
    private float b;

    public Spectrum(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Spectrum(Spectrum s) {
        this.r = s.getR();
        this.g = s.getG();
        this.b = s.getB();
    }

    public Spectrum() {
    }

    /**
     * Parses a Spectrum represented as a String
     *
     * @param s String to parse
     * @return float[] Returns RGB values in float array.
     */
    public static float[] parseString(String s) {
        String[] sArray = s.split(",");
        float[] f = new float[3];

        for (int i = 0; i < 3; i++) {
            f[i] = Float.valueOf(sArray[i]);
        }
        return f;
    }

    /**
     * Returns the average of the RGB values
     *
     * @return float Returns a scalar value.
     */
    public float toScalar() {
        return (r + g + b) / 3;
    }

    /**
     * Checks whether the RGB values are valid
     *
     * @return boolean Returns whether the Spectrum is valid.
     */
    public boolean isValid() {
        if ((r >= 0 && r <= 1) && (g >= 0 && g <= 1) && (b >= 0 && b <= 1)) {
            return true;
        }
        return false;
    }

    /**
     * Multiplies the Spectrum by a scalar value.
     *
     * @param scalar The scalar value
     */
    public void mul(float scalar) {
        this.r *= scalar;
        this.g *= scalar;
        this.b *= scalar;
    }

    /**
     * Divides the Spectrum by a scalar value.
     *
     * @param scalar The scalar value
     */
    public void div(float scalar) {
        this.r /= scalar;
        this.g /= scalar;
        this.b /= scalar;
    }

    /**
     * Adds a scalar value to the Spectrum.
     *
     * @param scalar The scalar value.
     */
    public void add(float scalar) {
        this.r += scalar;
        this.g += scalar;
        this.b += scalar;
    }

    /**
     * Adds a Spectrum to the Spectrum.
     *
     * @param spectrum The Spectrum to add.
     */
    public void add(Spectrum spectrum) {
        this.r += spectrum.getR();
        this.g += spectrum.getG();
        this.b += spectrum.getB();
    }

    /**
     * Copies the current Spectrum.
     *
     * @return A copy of the Spectrum
     */
    public Spectrum copy() {
        return new Spectrum(this);
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    /**
     * Validates the Spectrum
     *
     * @throws IllegalArgumentException
     */
    public void validate() throws IllegalArgumentException {
        if (!isValid()) {
            throw new IllegalArgumentException("Spectrum is invalid: " + toString());
        }
    }

    @Override
    public String toString() {
        return r + "," + g + "," + b;
    }

    @Override
    public boolean equals(Object obj) {
        Spectrum s = (Spectrum) obj;
        boolean equal = true;

        if (this.getR() != s.getR()) {
            equal = false;
        }

        if (this.getG() != s.getG()) {
            equal = false;
        }

        if (this.getB() != s.getB()) {
            equal = false;
        }

        return equal;
    }
}
