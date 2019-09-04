package com.nicky;

import com.nicky.brdfs.BRDF;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.joml.Vector3f;

import java.util.logging.Logger;

/**
 * <h1>BRDF Verifier</h1>
 * Verifies whether a BRDF is physically-based by ensuring the BRDF obeys reciprocity and energy conservation.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class BRDFVerifier {

    private static final Logger LOGGER = Logger.getLogger(BRDFManager.class.getName());
    private RandomDataGenerator random = new RandomDataGenerator();

    public BRDFVerifier() {
    }

    /**
     * Checks whether a BRDF is physically based and correct
     * For Energy Conservation it samples samplesPerTest (outgoing) for each numTests (incoming)
     *
     * @param brdf The brdf to check
     * @return boolean Returns whether the BRDF is physically-based.
     */
    public boolean isPhysicallyBased(BRDF brdf, int numTests, int samplesPerTest, boolean testConvergence) {
        if (!obeysReciprocity(brdf, numTests, samplesPerTest)) {
            LOGGER.warning("[" + brdf.getName() + "] BRDF does not obey Helmholtz Reciprocity.");
            return false;
        } else {
            LOGGER.info("[" + brdf.getName() + "] BRDF obeys Helmholtz Reciprocity. Ran " + numTests + " tests and sampled " + samplesPerTest + " outgoing directions.");
        }
        if(testConvergence) {
            if (!isEnergyConserving(brdf)) {
                LOGGER.warning("[" + brdf.getName() + "] BRDF is not energy conserving.");
                return false;
            }
        } else {
            if (!isEnergyConserving(brdf, numTests, samplesPerTest)) {
                LOGGER.warning("[" + brdf.getName() + "] BRDF is not energy conserving.");
                return false;
            } else {
                LOGGER.info("[" + brdf.getName() + "] BRDF is energy conserving. Ran " + numTests + " tests and sampled " + samplesPerTest + " outgoing directions.");
            }
        }

        return true;
    }

    /**
     * Checks whether the BRDF obeys Helmholtz Reciprocity law
     * Describes how incoming and outgoing light rays can be considered as reversals of one another.
     *
     * @param brdf the brdf to check
     * @return boolean Returns whether the BRDF obeys Helmholtz Reciprocity.
     */
    private boolean obeysReciprocity(BRDF brdf, int numTests, int samplesPerTest) {
        random.reSeed(100);

        for (int inRayCount = 0; inRayCount < numTests; inRayCount++) {
            Vector3f incomingDir = sampleUpperHemisphere();
            for (int samples = 0; samples < samplesPerTest; samples++) {
                Vector3f outgoingDir = sampleUpperHemisphere();
                if (!brdf.f(incomingDir, outgoingDir).equals(brdf.f(outgoingDir, incomingDir))) {
                    return false;
                }
            }
        }
        LOGGER.info("["+brdf.getName()+"]: obeys the law of reciprocity.");
        return true;
    }

    public boolean isEnergyConserving(BRDF brdf, int samplesPerTest) {
        RandomDataGenerator random = new RandomDataGenerator();
        random.reSeed(100);

        Vector3f normal = new Vector3f(1, 0, 0).normalize();
        Vector3f incomingDir = sampleUpperHemisphere();

        double pdf = (1.0f / (2.0f * Math.PI));
        double sum = 0;

        for (int samples = 1; samples <= samplesPerTest; samples++) {
            double xi_1 = random.nextUniform(0.0, 1.0, false);
            Vector3f outgoingDir = sampleUpperHemisphere();

            double brdfWeight = brdf.f(incomingDir, outgoingDir).toScalar();
            double cos_theta = xi_1;
            double sample = (brdfWeight * cos_theta) / pdf;
            LOGGER.info("["+brdf.getName()+"]: Sample ["+ samples+"] cos_theta = " + cos_theta + ", value = " + sample);
            sum += sample;
        }

        double estimator = sum / samplesPerTest;
        LOGGER.info("["+brdf.getName()+"]: Test ["+ 1+"] Monte Carlo Estimator <I> = " + estimator);

        // Test failed
        if(estimator > 1) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the BRDF is energy conserving
     * States that the exitant radiance must be less than or equal to the incoming
     * radiance for all possible incident radiance functions.
     * Uses random sampling
     * Uses Monte Carlo Integration
     *
     * @param brdf the brdf to check
     * @return boolean Returns whether the BRDF is energy conserving.
     */
    public boolean isEnergyConserving(BRDF brdf, int numTests, int samplesPerTest) {
        random.reSeed(99);

        Vector3f normal = new Vector3f(1, 0, 0).normalize();
        double pdf = (1.0f / (2.0f * Math.PI));
        double runningAverage = 0.0f;
        double prevRunningAverage = 0.0f;

        for (int inRayCount = 1; inRayCount <= numTests; inRayCount++) {
            Vector3f incomingDir = sampleUpperHemisphere();
//            System.out.println("incoming: " + incomingDir);
            double sum = 0;
            for (int samples = 1; samples <= samplesPerTest; samples++) {
                // Choose random point on sphere surface
                Vector3f outgoingDir = sampleUpperHemisphere();
                double brdfWeight = brdf.f(incomingDir, outgoingDir).toScalar();
                double cos_theta = normal.dot(outgoingDir);
                double sample = (brdfWeight * cos_theta) / pdf;
                sum += sample;
//                LOGGER.info("["+brdf.getName()+"]: Sample ["+ samples+"] cos_theta = " + cos_theta + ", value = " + sample);
            }
            double estimator = sum / samplesPerTest;
//            LOGGER.info("["+brdf.getName()+"]: Test ["+ inRayCount+"] Monte Carlo Estimator <I> = " + estimator);
            // Test failed
            if(estimator > 1) {
                return false;
            }
            if(inRayCount >= 2) {
                prevRunningAverage = runningAverage;
            }
            runningAverage = prevRunningAverage + ((estimator-prevRunningAverage) / inRayCount);
        }

        if(runningAverage > 1) {
            return false;
        }

        LOGGER.info("["+brdf.getName()+"]: Average of Monte Carlo Estimators: " + runningAverage);
        return true;
    }

    /**
     * Checks whether the BRDF is energy conserving
     * States that the exitant radiance must be less than or equal to the incoming
     * radiance for all possible incident radiance functions.
     * Uses random sampling
     * Uses a tolerance
     * Uses Monte Carlo Integration
     *
     * @param brdf the brdf to check
     * @return boolean Returns whether the BRDF is energy conserving.
     */
    private boolean isEnergyConserving(BRDF brdf) {
        random.reSeed(100);

        Vector3f normal = new Vector3f(1, 0, 0).normalize();

        double tolerance = 0.05;
        double brdfWeight = -1;
        double estimator;

        Vector3f incomingDir = sampleUpperHemisphere();
        double sum = 0;
        int samplesDone = 0;

        do {
            samplesDone++;
            // Choose random point on sphere surface
            Vector3f outgoingDir = sampleUpperHemisphere();
            brdfWeight = brdf.f(incomingDir, outgoingDir).toScalar();
            double pdf = (1.0f / (2.0f * Math.PI));
            double cos_theta = normal.dot(outgoingDir);
            double sample = (brdfWeight * (cos_theta)) / pdf;
            sum += sample;
            LOGGER.info("["+brdf.getName()+"]: Sample ["+ samplesDone +"] cos_theta = " + cos_theta + ", value = " + sample);

            estimator = sum / samplesDone;
            LOGGER.info("["+brdf.getName()+"]: Monte Carlo Estimator <I> = " + estimator);

            // Test failed
            if(estimator > 1) {
                return false;
            }
        } while(estimator < ((brdfWeight*3) - tolerance) || estimator > ((brdfWeight*3) + tolerance));

        return false;
    }

    public Vector3f sampleUpperHemisphere() {
        double x,y,z, xi_1, xi_2;

        xi_1 = random.nextUniform(0.0, 1.0, false);
        xi_2 = random.nextUniform(-1.0, 1.0, true);

        double phi = xi_1 * 2 * Math.PI;
        double sin_u2 = (1 - Math.pow(xi_2, 2));

        x = xi_1;
        y = Math.sin(phi) * sin_u2;
        z = Math.cos(phi) * sin_u2;

        return new Vector3f((float) x, (float) y, (float) z).normalize();
    }
}
