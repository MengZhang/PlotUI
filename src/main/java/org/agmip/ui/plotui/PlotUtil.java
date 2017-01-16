package org.agmip.ui.plotui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.agmip.common.Functions;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for PlotUI
 *
 * @author Meng Zhang
 */
public class PlotUtil {

    private final static Logger LOG = LoggerFactory.getLogger(PlotUtil.class);
    protected final static String R_PATH = "C:\\Program Files\\R\\R-3.3.2\\bin\\x64\\Rscript.exe";
    protected final static String R_LIB_PATH = "D:\\SSD_USER\\Documents\\R\\win-library\\3.3";
    protected final static String R_SCP_PATH = "r_lib";
    protected final static String CONFIG_FILE = "config.xml";
    protected final static String CONFIG_FILE_DEF = "config_def.xml";
    protected static HashMap<String, HashMap<String, String>> CONFIG_MAP;
    
    public enum RScps {
        StandardPlot("ria_standardplots2.r");
        
        private String rScpName;
        private RScps(String rScpName) {
            this.rScpName = rScpName;
        }
        public String getScpName() {
            return this.rScpName;
        }
    }
    
    protected static class ForceStopException extends Exception {
        
    }

    protected static void initialize(boolean isForced) throws ForceStopException {
        
        LOG.info("Initialize tool configuration...");
        
        // Deploy any invalid R script file
        Path rScpDir = Paths.get(R_SCP_PATH);
        if (!rScpDir.toFile().exists()) {
            Functions.revisePath(rScpDir.toFile().getPath());
        }
        LOG.debug(rScpDir.toFile().getAbsolutePath());
        
        for (RScps rScp : RScps.values()) {
            File rScpFile = rScpDir.resolve(rScp.getScpName()).toFile();
            if (!rScpFile.exists() || isForced) {
                LOG.info("Deploying {}", rScpFile.getName());
                InputStream rScriptIs = PlotUtil.class.getResourceAsStream("/" + rScp.getScpName());
                deployFile(rScriptIs, rScpFile);
            }
        }
        // Initialize config file if necessary
        File config = new File(CONFIG_FILE);
        if (!config.exists() || isForced) {
            LOG.info("Deploying {}", config.getName());
            InputStream configIs = PlotUtil.class.getResourceAsStream("/" + CONFIG_FILE_DEF);
            deployFile(configIs, config);
            waitForUserConfirm("Config file is set to default, please make any necessary change before starting plot tool.");
        }
        CONFIG_MAP = PlotUtil.readConfig(config);
        LOG.info("Initialization Done!");
    }

    private static void deployFile(InputStream in, File outputFile) {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                BufferedWriter wr = new BufferedWriter(new FileWriter(outputFile))) {
            char[] buf = new char[1024];
            int end;
            while ((end = br.read(buf)) > 0) {
                wr.write(buf, 0, end);
            }
            wr.flush();
        } catch (IOException ex) {
            LOG.error(Functions.getStackTrace(ex));
        }
    }
    
    public static void waitForUserConfirm(String... texts) throws ForceStopException {
        for (String txt : texts) {
            LOG.info(txt);
        }
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Are you ready to continue (Y/n)?");
        String userInput = keyboard.next();
        while (!userInput.equals("Y")) {
            if (userInput.equals("n")) {
                throw new ForceStopException();
            }
            userInput = keyboard.next();
        }
        
    }
    
    public static HashMap<String, String> readCommand(String[] args) {
        HashMap<String, String> cmds = new HashMap();
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            if (args[i].equalsIgnoreCase("-stdplot")) {
                cmds.put("rScpType", PlotUtil.RScps.StandardPlot.toString());
            }
            i++;
        }
        return cmds;
    }

    public static HashMap<String, HashMap<String, String>> readConfig(File configFile) {
        HashMap<String, HashMap<String, String>> ret = new HashMap();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(configFile);
            Element root = doc.getRootElement();
            List<Element> configurations = root.getChildren();
            for (Element config : configurations) {
                String plotName = config.getAttributeValue("name");
                LOG.debug("Find config for " + plotName);
                HashMap<String, String> configMap = new HashMap();
                ret.put(plotName, configMap);
                for (Element setting : config.getChildren()) {
                    LOG.debug(setting.getName() + " : " + setting.getText());
                    configMap.put(setting.getName(), setting.getText());
                }
            }

        } catch (JDOMException | IOException ex) {
            LOG.error(Functions.getStackTrace(ex));
        }
        return ret;
    }
}
