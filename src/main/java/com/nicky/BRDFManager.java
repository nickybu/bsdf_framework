package com.nicky;

import com.google.gson.JsonObject;
import com.nicky.brdfs.BRDF;
import com.nicky.brdfs.CompositeBRDF;
import com.nicky.factories.*;
import com.nicky.resources.Utils;
import org.joml.Vector3f;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * <h1>BRDF Manager</h1>
 * Manages the setup and creation of BRDFs from the defined json files
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class BRDFManager {

    private static final Logger LOGGER = Logger.getLogger(BRDFManager.class.getName());
    private BRDFBootstrapper brdfBootstrapper;
    private BRDFVerifier brdfVerifier;
    /**
     * Stores the Name and Properties of each BRDF
     */
    private Map<String, JsonObject> brdfProperties; // Stores Name and JsonObject
    /**
     * Stores the Alias and BRDF
     */
    private Map<String, BRDF> brdfs;
    /**
     * A list of all the registered BRDF's Names
     */
    private List<String> brdfNameList;
    /**
     * The default normalised direction vector of the incident ray
     */
    private Vector3f incidentRaySource = new Vector3f(1f, 1f, 0f).normalize();


    public BRDFManager() {
        brdfBootstrapper = new BRDFBootstrapper();
        brdfProperties = new HashMap<String, JsonObject>();
        brdfs = new HashMap<String, BRDF>();
        brdfNameList = new ArrayList<>();
        brdfVerifier = new BRDFVerifier();
    }

    /**
     * Initialises the BRDFs and returns a Map with all registered BRDFs
     *
     * @return Map with registered BRDFs and their alias
     * @throws Exception On invalid BRDF definition format.
     */
    public Map<String, BRDF> init() {
        try {
            LOGGER.info("Parsing BRDF Definition files...");
            getAvailableDefinitions("default");
            getAvailableDefinitions("custom");
            brdfProperties = sortBRDFMap(brdfProperties);
            getBRDFs();
            LOGGER.info("Finished registering BRDFs...");
            LOGGER.info("Checking if BRDFs are physically plausible...");
            validateBRDFs();
            return brdfs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a particular BRDF by alias
     *
     * @param alias BRDF Alias
     * @return BRDF BRDF Instance.
     * @throws Exception On BRDF not found.
     */
    public BRDF getBRDFFromAlias(String alias) {
        try {
            LOGGER.info("Parsing BRDF Definition files...");
            getAvailableDefinitions("sunflow");
            getBRDFs();
            LOGGER.info("Finished registering BRDFs...");
            LOGGER.info("Checking if BRDFs are physically based...");
            BRDF brdf = brdfs.get(alias);
            validateBRDF(brdf);
            return brdf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all BRDF definition files in a folder and uses values to initialise BRDFs
     *
     * @param folderName subfolder to search through
     * @throws IOException On BRDF Definition file not found.
     */
    public void getAvailableDefinitions(String folderName) throws IOException {

        Properties configProps = new Properties();
        try {
            InputStream in = BRDFManager.class.getResourceAsStream("/config.properties");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                String values[] = line.split("=", 2);
                configProps.setProperty(values[0], values[1]);
            }
        } catch (IOException e) {
            throw new IOException();
        }

        String path = configProps.getProperty("brdf_defs_location") + folderName;
        File folder = new File(path);
        File[] files = folder.listFiles();

        if (files == null) {
            throw new IOException("No BRDF Definitions found at path: " + path);
        }

        for (File defaultDefinition : Objects.requireNonNull(files)) {
            if (defaultDefinition.isFile()) {
                String fileContent = Utils.loadResource(defaultDefinition.getPath());
                JsonObject fileContentJson = Utils.deserialiseJSON(fileContent);
                brdfProperties.put(fileContentJson.get("alias").getAsString(), fileContentJson);
            }
        }
    }

    /**
     * Creates a factory for each BRDF and registers them
     * Handles both simple and composite BRDFs
     *
     * @throws Exception On invalid BRDF definition structure.
     */
    private void getBRDFs() throws Exception {
        BRDF newBRDF;

        for (Map.Entry<String, JsonObject> entry : brdfProperties.entrySet()) {
            try {
                // Simple BRDF
                if (entry.getValue().get("type").getAsString().equals("simple")) {
                    JsonObject components = entry.getValue().getAsJsonArray("components").get(0).getAsJsonObject();
                    String name = components.get("name").getAsString();
                    BRDFFactory brdfFactory = mapNameToFactory(name);

                    // If a factory does not exist for definition file
                    if (brdfFactory == null) {
                        throw new Exception("Invalid BRDF Found:" + entry.getKey());
                    }

                    brdfBootstrapper.setBrdfFactory(brdfFactory);
                    newBRDF = brdfBootstrapper.setupBRDF(components);
                    brdfs.put(entry.getKey(), newBRDF);

                }
                // Composite BRDF
                else if (entry.getValue().get("type").getAsString().equals("composite")) {
                    int numComponents = entry.getValue().getAsJsonArray("components").size();
                    LinkedHashMap<BRDF, Float> components = new LinkedHashMap<BRDF, Float>();

                    for (int i = 0; i < numComponents; i++) {
                        String componentName = entry.getValue().getAsJsonArray("components").get(i).getAsJsonObject().get("name").getAsString();
                        BRDFFactory brdfFactory = mapNameToFactory(componentName);

                        // If a factory does not exist for definition file
                        if (brdfFactory == null) {
                            throw new Exception("Invalid BRDF Found:" + entry.getKey() + ", " + componentName);
                        }

                        double weighting = entry.getValue().getAsJsonArray("components").get(i).getAsJsonObject().get("weighting").getAsFloat();

                        brdfBootstrapper.setBrdfFactory(brdfFactory);
                        components.put(brdfBootstrapper.setupBRDF(entry.getValue().getAsJsonArray("components").get(i).getAsJsonObject()), (float) weighting);
                    }

                    newBRDF = new CompositeBRDF(entry.getKey(), components);
                    brdfs.put(newBRDF.getName(), newBRDF);

                    // Add BRDF name to brdfNameList
                    brdfNameList.add(newBRDF.getName());
                }
                LOGGER.info("BRDF Registered: " + entry.getKey());
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                throw new Exception("BRDF Definition incorrect: " + entry.getKey());
            } catch (IllegalArgumentException e) {
                throw new Exception(e + " [" + entry.getKey() + "]");
            } finally {
                // Add BRDF name to brdfNameList
                brdfNameList.add(entry.getKey());
            }
        }
    }

    /**
     * Links a BRDF name to its Factory
     *
     * @param brdfName the name of the BRDF
     * @return BRDFFactory The BRDF Factory.
     */
    private BRDFFactory mapNameToFactory(String brdfName) {
        BRDFFactory factory = searchDefaultBRDF(brdfName);

        if (factory != null) {
            return factory;
        } else if (brdfs.get(brdfName) != null) {
            factory = searchDefaultBRDF(brdfs.get(brdfName).getName());
            return factory;
        }

        return null;
    }

    /**
     * Searches the default simple BRDFs for their respective BRDFFactory
     *
     * @param brdfName the name of the BRDF
     * @return BRDFFactory The BRDF Factory.
     */
    private BRDFFactory searchDefaultBRDF(String brdfName) {
        switch (brdfName) {
            case "LambertianBRDF":
                return new LambertianBRDFFactory();
            case "PhongDiffuseBRDF":
                return new PhongDiffuseBRDFFactory();
            case "PhongSpecularBRDF":
                return new PhongSpecularBRDFFactory();
            case "ShinyDiffuseBRDF":
                return new ShinyDiffuseBRDFFactory();
        }
        return null;
    }

    /**
     * Get the names of all registered BRDFs
     *
     * @return List<String> Returns a list with BRDF names.
     */
    public List<String> getBrdfNameList() {
        brdfNameList.sort(String::compareToIgnoreCase);
        return brdfNameList;
    }

    /**
     * Sorts the Map of BRDFs by 'simple' BRDFs first
     *
     * @param map Map storing BRDFs
     * @return Map<String ,   JsonObject> Returns an Ordered map
     */
    public Map<String, JsonObject> sortBRDFMap(Map<String, JsonObject> map) {
        List<Map.Entry<String, JsonObject>> list = new LinkedList<Map.Entry<String, JsonObject>>(map.entrySet());

        // Sort the list, with type=simple before type=composite
        Collections.sort(list, new Comparator<Map.Entry<String, JsonObject>>() {
            @Override
            public int compare(Map.Entry<String, JsonObject> o1, Map.Entry<String, JsonObject> o2) {
                if (o1.getValue().get("type").getAsString().equals("composite")) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        Map<String, JsonObject> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, JsonObject> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Validates all registered BRDFs to ensure that they are Physically-Based.
     */
    private void validateBRDFs() {
        // Incoming directions
        int numTests = 1;
        // Outgoing directions
        int samplesPerTest = (int) Math.pow(4, 5);

        for (Map.Entry<String, BRDF> brdf : brdfs.entrySet()) {
            if (!brdfVerifier.isPhysicallyBased(brdf.getValue(), numTests, samplesPerTest, false)) {
                LOGGER.warning("[" + brdf.getKey() + "] is not physically based.");
            } else {
                LOGGER.info("[" + brdf.getKey() + "] is physically plausible.");
            }
        }
    }

    /**
     * Validates a specific BRDF to ensure that it is Physically-Based.
     *
     * @param brdf The BRDF to be validated.
     */
    private void validateBRDF(BRDF brdf) {
        // Incoming directions
        int numTests = 1;
        // Outgoing directions
        int samplesPerTest = 4;

        if (!brdfVerifier.isPhysicallyBased(brdf, numTests, samplesPerTest, false)) {
            LOGGER.warning(" is not physically based: " + brdf.getName());
        } else {
            LOGGER.info(" is physically plausible: " + brdf.getName());
        }
    }




}
