package org.agmip.ui.plotui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import org.agmip.common.Functions;
import org.agmip.util.MapUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
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
//    private static String R_EXE_PATH = detectRExePath();
//    private static String R_LIB_PATH = detectRLibPath();
    protected final static String R_SCP_PATH = "r_lib";
    protected final static String CONFIG_FILE = "config.xml";
    protected final static String CONFIG_FILE_DEF = "config_def.xml";
    protected final static String CONFIG_FILE_DEF_TEMPLATE = "config_def.template";
    protected final static String REPORT_TEMPLATE = "report.template";
    protected final static String GLOBAL_CONFIG = "GlobalConfig";
    protected static HashMap<String, HashMap<String, String>> CONFIG_MAP;

    public enum RScps {

        StandardPlot("StandardPlot.r"), CorrelationPlot("CorrelationPlot.r"), ClimAnomaly("ClimAnomaly.r");

        private final String rScpName;

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

        // Setup environment paths
        setRPath("RLibPath", detectRLibPath());
        setRPath("RExePath", detectRExePath());

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

    public static HashSet<PlotUtil.RScps> readCommand(String[] args) {
        HashSet<PlotUtil.RScps> cmds = new HashSet();
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            if (args[i].equalsIgnoreCase("-stdplot")) {
                cmds.add(PlotUtil.RScps.StandardPlot);
            } else if (args[i].equalsIgnoreCase("-corplot")) {
                cmds.add(PlotUtil.RScps.CorrelationPlot);
            } else if (args[i].equalsIgnoreCase("-climate")) {
                cmds.add(PlotUtil.RScps.ClimAnomaly);
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

    private static void setRPath(String key, String defPath) throws ForceStopException {
        HashMap<String, String> rPathConfig = CONFIG_MAP.get(GLOBAL_CONFIG);
        if (rPathConfig == null) {
            LOG.warn("Missing GlobalConfig section in config file");
            if (defPath == null) {
                waitForUserConfirm(key + " can not be detected automatically, please provide the path via config GUI or via config.xml file manually.");
            } else {
                rPathConfig = new HashMap();
                CONFIG_MAP.put(GLOBAL_CONFIG, rPathConfig);
                rPathConfig.put(key, defPath);
            }
        } else {
            String path = rPathConfig.get(key);
            if (path == null || "".equals(path.trim()) || ("default").equalsIgnoreCase(path.trim())) {
                if (defPath == null) {
                    waitForUserConfirm(key + " can not be detected automatically, please provide the path manually via config.xml file.");
                } else {
                    rPathConfig.put(key, defPath);
                }
            }
        }
    }

    public static String detectRExePath() {
        File rExePath = new File("C:\\Program Files\\R\\R-3.3.2\\bin\\Rscript.exe");
        if (rExePath.exists()) {
            return rExePath.getPath();
        } else {
            File rInstallPath = new File("C:\\Program Files\\R");
            if (rInstallPath.isDirectory()) {
                for (File dir : rInstallPath.listFiles()) {
                    File f = Paths.get(dir.getPath(), "bin", "Rscript.exe").toFile();
                    if (f.isFile()) {
                        return f.getPath();
                    }
                }
            }
            return null;
        }
    }

    public static String detectRLibPath() {
        String userDocPath = new JFileChooser().getFileSystemView().getDefaultDirectory().getPath();
        File rLibDir = new File(userDocPath + "/R/win-library/3.3");
        if (rLibDir.exists()) {
            return rLibDir.getPath();
        } else {
            rLibDir = new File(userDocPath + "/R/win-library");
            for (File dir : rLibDir.listFiles()) {
                for (File f : dir.listFiles()) {
                    if (f.getName().equalsIgnoreCase("ggplot2") && f.isDirectory()) {
                        return dir.getPath();
                    }
                }
            }
            return null;
        }
    }

    public static String getRScpPath(RScps rScpType) {
        return Paths.get(PlotUtil.R_SCP_PATH, rScpType.getScpName()).toString();
    }

    public static String getRLibPath() {
        return MapUtil.getValueOr(CONFIG_MAP.get(GLOBAL_CONFIG), "RLibPath", "");
    }

    public static String getRExePath() {
        return MapUtil.getValueOr(CONFIG_MAP.get(GLOBAL_CONFIG), "RExePath", "");
    }

    public static String getVersion() {
        try {
            Properties versionProperties = new Properties();
            try (InputStream versionFile = PlotUIApp.class.getClassLoader().getResourceAsStream("product.properties")) {
                versionProperties.load(versionFile);
            }
            StringBuilder avv = new StringBuilder();
            String buildType = versionProperties.getProperty("product.buildtype");
            avv.append("Version ");
            avv.append(versionProperties.getProperty("product.version"));
            avv.append("-").append(versionProperties.getProperty("product.buildversion"));
            avv.append("(").append(buildType).append(")");
            if (buildType.equals("dev")) {
                avv.append(" [").append(versionProperties.getProperty("product.buildts")).append("]");
            }
            return avv.toString();
        } catch (IOException ex) {
            LOG.error("Unable to load version information, version will be blank.");
            return "";
        }
    }

    public static Writer openUTF8FileForWrite(File file) throws IOException {
        return new OutputStreamWriter(
                new FileOutputStream(file),
                StandardCharsets.UTF_8);
    }

    public static List<File> getAllInputFiles(File... dirs) {
        return getAllInputFiles(true, dirs);
    }

    public static List<File> getAllInputFiles(boolean ifGoSub, File... dirs) {
        HashSet<File> files = new HashSet();
        for (File dir : dirs) {
            if (dir.isDirectory()) {
                if (ifGoSub) {
                    files.addAll(getAllInputFiles(dir.listFiles()));
                } else {
                    for (File f : dir.listFiles()) {
                        if (dir.isFile() && isCsvFile(f)) {
                            files.add(f);
                        }
                    }
                }
            } else if (isCsvFile(dir)) {
                files.add(dir);
            }
        }

        return Arrays.asList(files.toArray(new File[]{}));
    }

    public static boolean isCsvFile(File f) {
        return f.getName().toLowerCase().endsWith(".csv");
    }

    public static File generateReport(LinkedHashMap<File, ArrayList<HashMap<String, String>>> result, String outputPath) {
        LOG.info("Saving validation report ...");
        File report = Paths.get(outputPath, "report_" + System.currentTimeMillis() + ".htm").toFile();
        Functions.revisePath(outputPath);
        try {
            deployFileByTemplate(report, REPORT_TEMPLATE, result, "reports");
        } catch (IOException ex) {
            LOG.error("An error occured while writing the validation report: {}", ex.getMessage());
            LOG.error(Functions.getStackTrace(ex));
        }
        LOG.info("Done!");

        return report;
    }

    public static void deployFileByTemplate(File outputFile, String templateName, Object data, String dataName) throws IOException {
        try (Writer writer = PlotUtil.openUTF8FileForWrite(outputFile)) {
            Velocity.init();
            VelocityContext context = new VelocityContext();
            Reader R = new InputStreamReader(PlotUIWindow.class.getClassLoader().getResourceAsStream(templateName));

            context.put(dataName, data);
            Velocity.evaluate(context, writer, "Generate " + dataName, R);
            writer.close();
        }
    }
}
