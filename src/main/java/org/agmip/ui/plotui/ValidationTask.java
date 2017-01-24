package org.agmip.ui.plotui;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.agmip.common.Functions;
import org.agmip.util.MapUtil;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class ValidationTask extends Task<LinkedHashMap<File, ArrayList<HashMap<String, String>>>> {

    private final static Logger LOG = LoggerFactory.getLogger(ValidationTask.class);
    private final List<File> inputCsvFiles;
    private final HashMap<String, Integer> titlePozMap = new HashMap();
    private final static String[] MODEL_OUTPUT_VARS = {"HWAH_S", "CWAH_S", "ADAT_S", "MDAT_S", "HADAT_S", "LAIX_S", "PRCP_S", "ETCP_S", "NUCM_S", "NLCM_S", "EPCP_S", "ESCP_S", "SRAA_S", "TMAXA_S", "TMINA_S", "TAVGA_S", "CO2D_S"};

    public ValidationTask(List<File> inputCsvFiles) {
        this.inputCsvFiles = inputCsvFiles;
        titlePozMap.put("EXNAME", 2);
        int i = 52;
        for (String var : MODEL_OUTPUT_VARS) {
            titlePozMap.put(var, i++);
        }
//        titlePozMap.put("HWAH_S", i++);
//        titlePozMap.put("CWAH_S", i++);
//        titlePozMap.put("ADAT_S", i++);
//        titlePozMap.put("MDAT_S", i++);
//        titlePozMap.put("HADAT_S", i++);
//        titlePozMap.put("LAIX_S", i++);
//        titlePozMap.put("PRCP_S", i++);
//        titlePozMap.put("ETCP_S", i++);
//        titlePozMap.put("NUCM_S", i++);
//        titlePozMap.put("NLCM_S", i++);
//        titlePozMap.put("EPCP_S", i++);
//        titlePozMap.put("ESCP_S", i++);
//        titlePozMap.put("SRAA_S", i++);
//        titlePozMap.put("TMAXA_S", i++);
//        titlePozMap.put("TMINA_S", i++);
//        titlePozMap.put("TAVGA_S", i++);
//        titlePozMap.put("CO2D_S", i++);
    }
    
    public ValidationTask(List<File> inputCsvFiles, String... validateVars) {
        this.inputCsvFiles = inputCsvFiles;
        titlePozMap.put("EXNAME", 2);
        int i = 52;
        HashSet<String> vars = new HashSet(Arrays.asList(validateVars));
        for (String var : MODEL_OUTPUT_VARS) {
            if (vars.contains(var)) {
                titlePozMap.put(var, i);
            }
            i++;
        }
    }

    @Override
    public LinkedHashMap<File, ArrayList<HashMap<String, String>>> execute() throws TaskExecutionException {
        
        LOG.info("Start validation");
        
        LinkedHashMap<File, ArrayList<HashMap<String, String>>> ret = new LinkedHashMap();

        for (File csvFile : inputCsvFiles) {

            ArrayList<HashMap<String, String>> validateReport = new ArrayList();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
                    CSVReader reader = new CSVReader(br, getListSeparator(br));) {

                // Go through the file and try to detect if there is any bad record.
                try {
                    setIndex(reader);
                } catch (BadAcmoTitleException ex) {
                    LOG.warn("Can not detect valid title line for {}", csvFile.getPath());
                    continue;
                }

                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    
                    HashMap<String, String> unitReport = new HashMap();
                    boolean isBlank = false;
                    for (String var : titlePozMap.keySet()) {
                        String val = getValue(nextLine, var, "").trim();
                        if (val.equals("")) {
                            val = "X";
                            isBlank = true;
                        } else if (!var.equalsIgnoreCase("exname")) {
                            val = "";
                        }
                        unitReport.put(var.toLowerCase(), val);
                    }
                    if (isBlank) {
                        LOG.debug("Detect blank records!");
                        validateReport.add(unitReport);
                    }
                }

            } catch (FileNotFoundException ex) {
                LOG.error(Functions.getStackTrace(ex));
            } catch (IOException ex) {
                LOG.error(Functions.getStackTrace(ex));
            }

            if (!validateReport.isEmpty()) {
                ret.put(csvFile, validateReport);
            }

        }

        LOG.info("Finish validation");
        return ret;
    }

    private char getListSeparator(BufferedReader in) throws IOException {
        // Set a mark at the beginning of the file, so we can get back to it.
        in.mark(7168);
        String listSeperator = ",";
        String sample;
        while ((sample = in.readLine()) != null) {
            if (sample.startsWith("#")) {
                listSeperator = sample.substring(1, 2);
                LOG.debug("FOUND SEPARATOR: " + listSeperator);
                break;
            } else if (sample.startsWith("\"#\"")) {
                listSeperator = sample.substring(3, 4);
                LOG.debug("FOUND SEPARATOR: " + listSeperator);
                break;
            }
        }
        in.reset();
        return listSeperator.charAt(0);
    }

    private void setIndex(CSVReader reader) throws IOException, BadAcmoTitleException {
        String[] titles = reader.readNext();
        while (titles != null && !titles[0].trim().equals("#") && !titles[0].trim().equals("\"#\"")) {
            titles = reader.readNext();
        }
        if (titles == null) {
            throw new BadAcmoTitleException();
        }
        for (int i = 1; i < titles.length; i++) {
            if (titlePozMap.containsKey(titles[i])) {
                titlePozMap.put(titles[i], i);
            }
        }
//        String[] keys = titlePozMap.keySet().toArray(new String[]{});
//        HashSet<String> titleSet = new HashSet(Arrays.asList(keys));
//        for (String key : keys) {
//            if (!titleSet.contains(key)) {
//                titlePozMap.remove(key);
//            }
//        }
    }

    private String getValue(String[] line, String var, String defVal) {
        int idx = MapUtil.getObjectOr(titlePozMap, var.toUpperCase(), -1);
        if (idx < 0 || idx > line.length - 1) {
            return defVal;
        } else {
            return line[idx];
        }
    }

    private class BadAcmoTitleException extends Exception {
    }
}
