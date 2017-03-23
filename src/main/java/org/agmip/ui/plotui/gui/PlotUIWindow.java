package org.agmip.ui.plotui.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.agmip.common.Functions;
import org.agmip.ui.plotui.DragDropFactory;
import org.agmip.ui.plotui.PlotUtil;
import org.agmip.ui.plotui.RunPlotTask;
import org.agmip.ui.plotui.ValidationTask;
import static org.agmip.ui.plotui.PlotUtil.*;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.AlertListener;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class PlotUIWindow extends Window implements Bindable {

    private static final Logger LOG = LoggerFactory.getLogger(PlotUIWindow.class);
    private Label txtVersion = null;
    private TabPane plotuiTabs = null;
    private PushButton saveConfig = null;
    private PushButton runRScp = null;
    private ActivityIndicator runIndicator = null;
    
    private PlotUtil.RScps curTab = null;
    private BoxPane gcmLabels = null;
    private BoxPane gcmCatLabels = null;
    private BoxPane gcmCatSelectionLabels = null;
    private ActivityIndicator mappingIndicator = null;
    HashMap<String, Label> gcmCatMap = new HashMap();
    private TextInput workDirInput = null;
    private PushButton browseWorkDir = null;
    
    PlotTabBoxPane stdPlotTab = null;
    PlotTabBoxPane corPlotTab = null;
    PlotTabBoxPane ctwnPlotTab = null;
    PlotTabBoxPane hisPlotTab = null;

    private HashMap<String, String> globalConfig = getConfig(PlotUtil.GLOBAL_CONFIG);

    public PlotUIWindow() {
        Action.getNamedActions().put("fileQuit", new Action() {
            @Override
            public void perform(Component src) {
                DesktopApplicationContext.exit();
            }
        });
        final Window curWindow = this.getWindow();
        Action.getNamedActions().put("REnvConfig", new Action() {
            @Override
            public void perform(Component src) {
                BXMLSerializer bxml = new BXMLSerializer();
                Dialog dialog;
                try {
                    dialog = (Dialog) bxml.readObject(getClass().getResource("/uiscript/dialogREnvConfig.bxml"));
                    dialog.open(curWindow.getWindow());
                } catch (IOException | SerializationException ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }
            }
        });
    }

    public void setPlotUIVersion(String plotUIVersion) {
        txtVersion.setText(plotUIVersion);
    }

    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        // Initialization
        // Start Tab
        workDirInput = (TextInput) ns.get("workDir");
        browseWorkDir = (PushButton) ns.get("browseWorkDir");
        gcmLabels = (BoxPane) ns.get("gcmLabels");
        gcmCatLabels = (BoxPane) ns.get("gcmCatLabels");
        gcmCatSelectionLabels = (BoxPane) ns.get("gcmCatSelectionLabels");
        mappingIndicator = (ActivityIndicator) ns.get("mappingIndicator");
        runIndicator = (ActivityIndicator) ns.get("runIndicator");
        plotuiTabs = (TabPane) ns.get("plotuiTabs");
        saveConfig = (PushButton) ns.get("saveConfig");
        runRScp = (PushButton) ns.get("runRScp");
        txtVersion = (Label) ns.get("txtVersion");

        // Standard Plot
        stdPlotTab = ((StdPlotTabBoxPane) ns.get("stdPlotTab"));

        // Correlation Plot
        corPlotTab = ((CorPlotTabBoxPane) ns.get("corPlotTab"));

        // CTWN Plot
        ctwnPlotTab = ((CtwnPlotTabBoxPane) ns.get("ctwnPlotTab"));

        // Historical Plot
        hisPlotTab = ((HisPlotTabBoxPane) ns.get("hisPlotTab"));

        // Define listeners for buttons
        saveConfig.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                try {
                    saveAllConfig();
                    LOG.info("Done!");
                    Alert.alert(MessageType.INFO, "Config Saved", PlotUIWindow.this);
                } catch (IOException ex) {
                    LOG.error("An error occured while writing the {} file: {}", CONFIG_FILE, ex.getMessage());
                    LOG.error(Functions.getStackTrace(ex));
                    Alert.alert(MessageType.ERROR, ex.getMessage(), PlotUIWindow.this);
                }
            }
        });

        runRScp.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {

                runIndicator.setActive(true);
                try {
                    saveAllConfig();
                    LOG.info("Done!");
                } catch (IOException ex) {
                    LOG.error("An error occured while writing the {} file: {}", CONFIG_FILE, ex.getMessage());
                    LOG.error(Functions.getStackTrace(ex));
                    Alert.alert(MessageType.ERROR, ex.getMessage(), PlotUIWindow.this);
                }
                validateInput();
            }
        });

        plotuiTabs.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {

            @Override
            public void selectedIndexChanged(TabPane tp, int i) {
                String tabName = tp.getSelectedTab().getName();
                if (null != tabName) switch (tabName) {
                    case "start":
                        saveConfig.setEnabled(true);
                        runRScp.setEnabled(false);
                        curTab = null;
                        break;
                    case "help":
                        saveConfig.setEnabled(false);
                        runRScp.setEnabled(false);
                        curTab = null;
                        break;
                    default:
                        saveConfig.setEnabled(true);
                        runRScp.setEnabled(true);
                        curTab = PlotUtil.RScps.valueOf(tabName);
                        break;
                } else {
                    saveConfig.setEnabled(false);
                    runRScp.setEnabled(false);
                    curTab = null;
                }
            }
        });
        
        browseWorkDir.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;
                
                if (!new File(workDirInput.getText()).exists()) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File("").getAbsolutePath());
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(workDirInput.getText()).getAbsoluteFile().getParentFile().getPath());
                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            workDirInput.setText(dir.getPath());
                            setGcmCatMapping(PlotUtil.getGcms(dir));
                        }
                        mappingIndicator.setActive(false);
                    }
                });
                mappingIndicator.setActive(true);
            }
        });

        workDirInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {

            @Override
            public void textChanged(TextInput ti) {
                File config = Paths.get(ti.getText(), PlotUtil.CONFIG_FILE).toFile();
                if (config.isFile()) {
                    CONFIG_MAP = PlotUtil.readConfig(config, ti.getText());
                    globalConfig = getConfig(PlotUtil.GLOBAL_CONFIG);
                    stdPlotTab.updateConfig();
                    corPlotTab.updateConfig();
                    ctwnPlotTab.updateConfig();
                    hisPlotTab.updateConfig();
                    loadAllConfig();
                    LOG.info("Load config file from working directory <{}>", ti.getText());
                } else {
                    LOG.info("Not detect existing config under working directory, will create a new config file instead");
                }
                globalConfig.put("WorkDir", ti.getText());
            }
        });

        
        // Load configuration from XML into GUI
        loadAllConfig();
        // Global
        workDirInput.setText(MapUtil.getValueOr(globalConfig, "WorkDir", ""));

    }

    private void loadAllConfig() {

        // Set GCM mapping from config.xml
        setGcmCatMapping();
        // StdPlot
        stdPlotTab.loadConfig();
        // CorPlot
        corPlotTab.loadConfig();
        // CTWNPlot
        ctwnPlotTab.loadConfig();
        // HistoricalPlot
        hisPlotTab.loadConfig();
    }

    private void saveAllConfig() throws IOException {

        LOG.info("Saving {} ...", CONFIG_FILE);

        // GloBal
        globalConfig.put("WorkDir", workDirInput.getText());
        StringBuilder sbGcmMapping = new StringBuilder();
        for (String gcm : gcmCatMap.keySet()) {
            if (gcmCatMap.get(gcm).getText() != null) {
                sbGcmMapping.append(gcm).append(":")
                        .append(gcmCatMap.get(gcm).getText()).append(":")
                        .append(toColorName(gcmCatMap.get(gcm).getStyles().get("color"))).append("|");
            }
        }
        globalConfig.put("GcmMapping", sbGcmMapping.toString());

        // StdPlot
        stdPlotTab.saveConfig();

        // CorPlot
        corPlotTab.saveConfig();

        // CTWNPlot
        ctwnPlotTab.saveConfig();

        // HistoricalPlot
        hisPlotTab.saveConfig();

        deployFileByTemplate(new File(CONFIG_FILE), CONFIG_FILE_DEF_TEMPLATE, CONFIG_MAP, "config");
        deployFileByTemplate(Paths.get(MapUtil.getValueOr(globalConfig, "WorkDir", ""), CONFIG_FILE).toFile(), CONFIG_FILE_PROJECT_TEMPLATE, CONFIG_MAP, "config");
        PlotUtil.resolvePath(PlotUtil.CONFIG_MAP, workDirInput.getText());
    }

    private void validateInput() {

        LOG.info("Start validation for {}", curTab.toString());
        ArrayList<String> plotVars = PlotUtil.getValidateVars(curTab);
        String inputDir = PlotUtil.CONFIG_MAP.get(curTab.toString()).get("inputDir");
        final String outputDir = PlotUtil.CONFIG_MAP.get(curTab.toString()).get("outputPath");
        if (inputDir == null || inputDir.equals("")) {
            LOG.warn("Invalid input path for validation!");
            Alert.alert(MessageType.ERROR, "Invalid input path!", PlotUIWindow.this);
            runIndicator.setActive(false);
        } else if (outputDir == null || outputDir.equals("")) {
            LOG.warn("Invalid output path for validation!");
            Alert.alert(MessageType.ERROR, "Invalid output path!", PlotUIWindow.this);
            runIndicator.setActive(false);
        } else if (plotVars.isEmpty()) {
            LOG.warn("Invalid plot variables for validation!");
            Alert.alert(MessageType.ERROR, "Invalid plot variables!", PlotUIWindow.this);
            runIndicator.setActive(false);
        } else {
            ValidationTask task = new ValidationTask(PlotUtil.getAllInputFiles(false, new File(inputDir)), plotVars.toArray(new String[]{}));
            TaskListener lisener = new TaskListener<LinkedHashMap<File, ArrayList<HashMap<String, String>>>>() {

                @Override
                public void taskExecuted(Task<LinkedHashMap<File, ArrayList<HashMap<String, String>>>> task) {

                    LinkedHashMap<File, ArrayList<HashMap<String, String>>> result = task.getResult();
                    if (!result.isEmpty()) {

                        // Generate report
                        final File report = PlotUtil.generateReport(result, outputDir);

                        // Ask for user confirmation
                        BXMLSerializer serializer = new BXMLSerializer();
                        Component body = null;
                        try {
                            body = (Component) serializer.readObject(getClass().getResource("/validatewarning.bxml"));
                        } catch (IOException | SerializationException ex) {
                            LOG.error(Functions.getStackTrace(ex));
                        }

                        Alert warning = new Alert(MessageType.WARNING, "Detect blank line of model output in ACMO files",
                                new org.apache.pivot.collections.ArrayList("Report", "Continue", "Abort"),
                                body);
                        warning.setSelectedOption("Abort");
                        warning.getAlertListeners().add(new AlertListener.Adapter() {

                            @Override
                            public void selectedOptionChanged(Alert alert, int i) {
                                String userChoice = alert.getSelectedOption().toString();

                                switch (userChoice) {
                                    case "Report":
                                        // Open the report
                                        try {
                                            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + report.getPath() + "\"");
                                            pb.start();
                                        } catch (IOException winEx) {
                                            try {
                                                ProcessBuilder pb = new ProcessBuilder("open", "\"" + report.getPath() + "\"");
                                                pb.start();
                                            } catch (IOException macEx) {
                                                Alert.alert(MessageType.ERROR, "Your OS can not open the file by using this link", PlotUIWindow.this);
                                                LOG.error(Functions.getStackTrace(winEx));
                                                LOG.error(Functions.getStackTrace(macEx));
                                            }
                                        }
                                        break;
                                    case "Continue":
                                        runIndicator.setActive(true);
                                        runPlot();
                                        break;
                                }
                            }
                        });
                        warning.open(PlotUIWindow.this);
                        runIndicator.setActive(false);
                    } else {
                        LOG.info("Start validation for {} done!", curTab.toString());
                        runPlot();
                    }

                }

                @Override
                public void executeFailed(Task<LinkedHashMap<File, ArrayList<HashMap<String, String>>>> task) {
                    Alert.alert(MessageType.ERROR, task.getFault().toString(), PlotUIWindow.this);
                    LOG.error(Functions.getStackTrace(task.getFault()));
//                        quitCurRun(false, isBatchApplied);
                    runIndicator.setActive(false);
                }
            };
            task.execute(new TaskAdapter(lisener));
        }
    }

    private void runPlot() {

        LOG.info("Start R script for {}", curTab.toString());
        RunPlotTask task = new RunPlotTask(curTab);
        TaskListener<Integer> lisener = new TaskListener<Integer>() {

            @Override
            public void taskExecuted(Task<Integer> task) {

                if (task.getResult() != 0) {
                    Alert.alert(MessageType.ERROR, "R script failed! Please check log for detail", PlotUIWindow.this);
                    runIndicator.setActive(false);
                    return;
                }
                LOG.info("Start R script for {} done!", curTab.toString());

                BoxPane body = new BoxPane(Orientation.VERTICAL);
                final File plotFile = getPlotOutputFile(curTab);
                LOG.debug(plotFile.getAbsolutePath());
                if (plotFile.exists()) {
                    body.add(new Label("Click open to show the plot result"));
                    Alert report = new Alert(MessageType.INFO, "Job done!",
                            new org.apache.pivot.collections.ArrayList("Open", "Close"),
                            body);
                    report.setSelectedOption("Close");
                    report.getAlertListeners().add(new AlertListener.Adapter() {

                        @Override
                        public void selectedOptionChanged(Alert alert, int i) {
                            String userChoice = alert.getSelectedOption().toString();

                            switch (userChoice) {
                                case "Open":
                                    LOG.info("{} {}", userChoice, plotFile);
                                    // Open the report
                                    try {
                                        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + plotFile.getPath() + "\"");
                                        pb.start();
                                    } catch (IOException winEx) {
                                        try {
                                            ProcessBuilder pb = new ProcessBuilder("open", "\"" + plotFile.getPath() + "\"");
                                            pb.start();
                                        } catch (IOException macEx) {
                                            Alert.alert(MessageType.ERROR, "Your OS can not open the file by using this link", PlotUIWindow.this);
                                            LOG.error(Functions.getStackTrace(winEx));
                                            LOG.error(Functions.getStackTrace(macEx));
                                        }
                                    }
                                    break;
                            }
                        }
                    });
                    report.open(PlotUIWindow.this);
                } else {
                    Alert.alert(MessageType.INFO, "Job done!", PlotUIWindow.this);
                }
                runIndicator.setActive(false);
            }

            @Override
            public void executeFailed(Task<Integer> task) {
                Alert.alert(MessageType.ERROR, task.getFault().toString(), PlotUIWindow.this);
                LOG.error(Functions.getStackTrace(task.getFault()));
                runIndicator.setActive(false);
            }
        };
        task.execute(new TaskAdapter(lisener));
    }

    private void setGcmCatMapping(String... gcms) {

        resetLableGroup(gcmLabels);
        resetLableGroup(gcmCatLabels);
        resetLableGroup(gcmCatSelectionLabels);

        gcmCatMap = new HashMap();
        LinkedHashMap<String, String> gcmCatColorMap = new LinkedHashMap();
        gcmCatColorMap.put("Base", "#D3D3D3");
        gcmCatColorMap.put("Cool-Dry", "blue");
        gcmCatColorMap.put("Cool-Wet", "green");
        gcmCatColorMap.put("Hot-Dry", "red");
        gcmCatColorMap.put("Hot-Wet", "#FFD700");
        gcmCatColorMap.put("Middle", "black");
        String gcmMappingStr = MapUtil.getValueOr(globalConfig, "GcmMapping", "").trim();
        if (!gcmMappingStr.equals("")) {
            String[] gcmPairs = gcmMappingStr.split("\\|");
            for (String gcmPair : gcmPairs) {
                String[] mapping = gcmPair.split(":");
                if (mapping.length > 2) {
                    gcmCatColorMap.put(mapping[1], mapping[2]);
                }
            }
        }

        DragSource ds = DragDropFactory.createLabelDragSource();
        DropTarget dt = DragDropFactory.createLabelDropTarget();
//        DragSource ds2 = DragDropFactory.createLabelDragSource();
//        DropTarget dt2 = DragDropFactory.createLabelDropTarget();
        TreeMap<String, String> gcmCatConfig = new TreeMap();
        String[] gcmPairs = MapUtil.getValueOr(globalConfig, "GcmMapping", "").split("\\|");
        for (String gcmPair : gcmPairs) {
            String[] tmp = gcmPair.split(":");
            if (tmp.length >= 2) {
                gcmCatConfig.put(tmp[0], tmp[1]);
            }
        }

        if (gcms.length == 0) {
            gcms = gcmCatConfig.keySet().toArray(gcms);
        }

        for (String gcm : gcms) {
            if (gcm.trim().equals("")) {
                continue;
            }
            // GCM ID Label
            Label gcmLabel = new Label(gcm);
            gcmLabel.setPreferredHeight(14);
            gcmLabel.setPreferredWidth(60);
            Border gcmBorder = new Border(gcmLabel);
            gcmBorder.getStyles().put("color", "white");
            gcmLabels.add(gcmBorder);
            // GCM Category Label
            Label gcmCatLabel;
            String gcmCat = gcmCatConfig.get(gcm);

            if (gcmCat != null) {
                gcmCatLabel = new Label(gcmCat);
                gcmCatLabel.getStyles().put("color", gcmCatColorMap.get(gcmCat));
                //gcmCatColorMap.remove(gcmCat);
            } else {
                gcmCatLabel = new Label();
            }
            gcmCatLabel.setName("gcmCatLabels_" + gcm);
            gcmCatLabel.setPreferredHeight(14);
            gcmCatLabel.setPreferredWidth(100);
            gcmCatLabel.setDragSource(ds);
            gcmCatLabel.setDropTarget(dt);
            Border gcmCatBorder = new Border(gcmCatLabel);
            gcmCatBorder.getStyles().put("backgroundColor", "#F5F5F5");
            gcmCatLabels.add(gcmCatBorder);
            gcmCatMap.put(gcm, gcmCatLabel);
        }

        for (String gcmCat : gcmCatColorMap.keySet()) {

            Label gcmCatLabel = new Label(gcmCat);
            gcmCatLabel.setName("gcmCatSelectionLabels_" + gcmCat);
            gcmCatLabel.setPreferredHeight(14);
            gcmCatLabel.setPreferredWidth(80);
            gcmCatLabel.getStyles().put("color", gcmCatColorMap.get(gcmCat));
            gcmCatLabel.setDragSource(ds);
            gcmCatLabel.setDropTarget(dt);
            Border gcmBorder = new Border(gcmCatLabel);
            gcmBorder.getStyles().put("color", "white");
            gcmCatSelectionLabels.add(gcmBorder);
        }
    }

    private static void resetLableGroup(BoxPane bp) {
        Component title;
        title = bp.get(0);
        bp.removeAll();
        bp.add(title);
    }
}
