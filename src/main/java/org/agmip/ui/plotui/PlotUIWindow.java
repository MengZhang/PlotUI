package org.agmip.ui.plotui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.common.Functions;
import static org.agmip.ui.plotui.PlotUtil.*;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.AlertListener;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
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
    private PlotUtil.RScps curTab = PlotUtil.RScps.StandardPlot;
    private BoxPane gcmLabels = null;
    private BoxPane gcmCatLabels = null;
    private BoxPane gcmCatSelectionLabels = null;
    HashMap<String, Label> gcmCatMap = new HashMap();

    private TextInput global_rExePath = null;
    private PushButton global_browseRExePath = null;
    private TextInput global_rLibPath = null;
    private PushButton global_browseRLibPath = null;
    private TextInput global_acmoDir = null;
    private PushButton global_browseAcmoDir = null;

    private TextInput stdplot_inputDir = null;
    private PushButton stdplot_inputDirBrowse = null;
    private TextInput stdplot_outputDir = null;
    private PushButton stdplot_outputDirBrowse = null;
    private TextInput stdplot_outputGraph = null;
    private TextInput stdplot_title = null;
    private ListButton stdplot_plotVarLB = null;
    private ButtonGroup stdplot_plotType = null;
    private HashMap<String, RadioButton> stdplot_plotTypeRBMap = null;
    private ButtonGroup stdplot_plotFormat = null;
    private HashMap<String, RadioButton> stdplot_plotFormatRBMap = null;

    private TextInput corplot_inputDir = null;
    private PushButton corplot_inputDirBrowse = null;
    private TextInput corplot_outputDir = null;
    private PushButton corplot_outputDirBrowse = null;
    private TextInput corplot_outputGraph = null;
    private ListButton corplot_plotVarXLB = null;
    private ListButton corplot_plotVarYLB = null;
    private ListButton corplot_plotGroup1LB = null;
    private ListButton corplot_plotGroup2LB = null;
    private ButtonGroup corplot_plotFormat = null;
    private HashMap<String, RadioButton> corplot_plotFormatRBMap = null;

    private final HashMap<String, String> globalConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.GLOBAL_CONFIG);
    private final HashMap<String, String> stdConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.StandardPlot.toString());
    private final HashMap<String, String> corConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.CorrelationPlot.toString());

    public PlotUIWindow() {
        Action.getNamedActions().put("fileQuit", new Action() {
            @Override
            public void perform(Component src) {
                DesktopApplicationContext.exit();
            }
        });
    }

    public void setPlotUIVersion(String plotUIVersion) {
        txtVersion.setText(plotUIVersion);
    }

    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        // Initialization
        // Global
        global_rExePath = (TextInput) ns.get("global_rExePath");
        global_browseRExePath = (PushButton) ns.get("global_browseRExePath");
        global_rLibPath = (TextInput) ns.get("global_rLibPath");
        global_browseRLibPath = (PushButton) ns.get("global_browseRLibPath");
        global_acmoDir = (TextInput) ns.get("global_acmoDir");
        global_browseAcmoDir = (PushButton) ns.get("global_browseAcmoDir");
        gcmLabels = (BoxPane) ns.get("global_gcmLabels");
        gcmCatLabels = (BoxPane) ns.get("global_gcmCatLabels");
        gcmCatSelectionLabels = (BoxPane) ns.get("global_gcmCatSelectionLabels");
        plotuiTabs = (TabPane) ns.get("plotuiTabs");
        saveConfig = (PushButton) ns.get("saveConfig");
        runRScp = (PushButton) ns.get("runRScp");
        txtVersion = (Label) ns.get("txtVersion");

        // Standard Plot
        stdplot_inputDir = (TextInput) ns.get("stdplot_inputDir");
        stdplot_inputDirBrowse = (PushButton) ns.get("stdplot_inputDirBrowse");
        stdplot_outputDir = (TextInput) ns.get("stdplot_outputDir");
        stdplot_outputDirBrowse = (PushButton) ns.get("stdplot_outputDirBrowse");
        stdplot_outputGraph = (TextInput) ns.get("stdplot_outputGraph");
        stdplot_title = (TextInput) ns.get("stdplot_title");
        stdplot_plotVarLB = (ListButton) ns.get("stdplot_plotVarLB");
        stdplot_plotType = (ButtonGroup) ns.get("stdplot_plotTypeButtons");
        stdplot_plotTypeRBMap = initRadioButtonGroup(ns, "stdplot_plotType_box", "stdplot_plotType_cdf");
        stdplot_plotFormat = (ButtonGroup) ns.get("stdplot_plotFormatButtons");
        stdplot_plotFormatRBMap = initRadioButtonGroup(ns, "stdplot_plotFormat_pdf", "stdplot_plotFormat_png");

        // Correlation Plot
        corplot_inputDir = (TextInput) ns.get("corplot_inputDir");
        corplot_inputDirBrowse = (PushButton) ns.get("corplot_inputDirBrowse");
        corplot_outputDir = (TextInput) ns.get("corplot_outputDir");
        corplot_outputDirBrowse = (PushButton) ns.get("corplot_outputDirBrowse");
        corplot_outputGraph = (TextInput) ns.get("corplot_outputGraph");
        corplot_plotVarXLB = (ListButton) ns.get("corplot_plotVarXLB");
        corplot_plotVarYLB = (ListButton) ns.get("corplot_plotVarYLB");
        corplot_plotGroup1LB = (ListButton) ns.get("corplot_plotGroup1LB");
        corplot_plotGroup2LB = (ListButton) ns.get("corplot_plotGroup2LB");
        corplot_plotFormat = (ButtonGroup) ns.get("corplot_plotFormatButtons");
        corplot_plotFormatRBMap = initRadioButtonGroup(ns, "corplot_plotFormat_pdf", "corplot_plotFormat_png");

        // Load configuration from XML into GUI
        // Global
        global_rExePath.setText(MapUtil.getValueOr(globalConfig, "RExePath", ""));
        global_rLibPath.setText(MapUtil.getValueOr(globalConfig, "RLibPath", ""));
        // StdPlot
        stdplot_inputDir.setText(MapUtil.getValueOr(stdConfig, "inputDir", ""));
        stdplot_title.setText(MapUtil.getValueOr(stdConfig, "title", ""));
        stdplot_outputDir.setText(MapUtil.getValueOr(stdConfig, "outputPath", ""));
        stdplot_outputGraph.setText(MapUtil.getValueOr(stdConfig, "outputGraph", ""));
        setSelectionList(stdplot_plotVarLB, stdConfig, "plotVar");
        setRadioButtonGroup(stdplot_plotType, stdplot_plotTypeRBMap, stdConfig, "plotType");
        setRadioButtonGroup(stdplot_plotFormat, stdplot_plotFormatRBMap, stdConfig, "plotFormat");
        // CorPlot
        corplot_inputDir.setText(MapUtil.getValueOr(corConfig, "inputDir", ""));
        corplot_outputDir.setText(MapUtil.getValueOr(corConfig, "outputPath", ""));
        corplot_outputGraph.setText(MapUtil.getValueOr(corConfig, "outputGraph", ""));
        setSelectionList(corplot_plotVarXLB, corConfig, "plotVarX");
        setSelectionList(corplot_plotVarYLB, corConfig, "plotVarY");
        setSelectionList(corplot_plotGroup1LB, corConfig, "group1");
        setSelectionList(corplot_plotGroup2LB, corConfig, "group2");
        corplot_plotFormat.setSelection(corplot_plotFormatRBMap.get(MapUtil.getValueOr(corConfig, "plotFormat", "")));
        setRadioButtonGroup(corplot_plotFormat, corplot_plotFormatRBMap, corConfig, "plotFormat");

        // Set GCM mapping from config.xml
        setGcmCatMapping();

        saveConfig.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                saveAllConfig();
            }
        });

        runRScp.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {

                saveAllConfig();
                validateInput();
            }
        });

        plotuiTabs.getTabPaneSelectionListeners().add(new TabPaneSelectionListener.Adapter() {

            @Override
            public void selectedIndexChanged(TabPane tp, int i) {
                int maxIdx = tp.getTabs().getLength() - 1;
                int curIdx = tp.getSelectedIndex();
                if (curIdx == 0) {
                    saveConfig.setEnabled(true);
                    runRScp.setEnabled(false);
                } else if (curIdx == maxIdx) {
                    saveConfig.setEnabled(false);
                    runRScp.setEnabled(false);
                } else {
                    saveConfig.setEnabled(true);
                    runRScp.setEnabled(true);
                    if (curIdx == 1) {
                        curTab = PlotUtil.RScps.StandardPlot;
                    } else if (curIdx == 2) {
                        curTab = PlotUtil.RScps.CorrelationPlot;
                    } else if (curIdx == 3) {
                        curTab = PlotUtil.RScps.ClimAnomaly;
                    } else {
                        curTab = null;
                    }
                }
            }
        });

        global_browseRExePath.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (global_rExePath.getText().equals("")) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
                } else {
                    if (!new File(global_rExePath.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File(global_rExePath.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                }
                browse.setDisabledFileFilter(new Filter<File>() {

                    @Override
                    public boolean include(File file) {
                        return (file.isFile() && (!file.getName().equalsIgnoreCase("Rscript.exe")));
                    }
                });
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            global_rExePath.setText(dir.getPath());
                        }
                    }
                });
            }
        });

        global_browseRLibPath.getButtonPressListeners().add(createGenericDirBPListerner(global_rLibPath));
        global_browseAcmoDir.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (global_acmoDir.getText().equals("")) {
                    if (!new File(stdplot_inputDir.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(stdplot_inputDir.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                } else {
                    if (!new File(global_acmoDir.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(global_acmoDir.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            global_acmoDir.setText(dir.getPath());
                            setGcmCatMapping(PlotUtil.getGcms(dir));
                        }
                    }
                });
            }
        });

        stdplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(stdplot_inputDir));
        stdplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(stdplot_outputDir));

        corplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(corplot_inputDir));
        corplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(corplot_outputDir));
        setNonRepeatedSelection(corplot_plotVarXLB, corplot_plotVarYLB);
        setNonRepeatedSelection(corplot_plotGroup1LB, corplot_plotGroup2LB);
    }

    private void saveAllConfig() {

        LOG.info("Saving {} ...", CONFIG_FILE);

        // GloBal
        globalConfig.put("RExePath", global_rExePath.getText());
        globalConfig.put("RLibPath", global_rLibPath.getText());
        StringBuilder sbGcmMapping = new StringBuilder();
        for (String gcm : gcmCatMap.keySet()) {
            if (gcmCatMap.get(gcm).getText() != null) {
                sbGcmMapping.append(gcm).append(":").append(gcmCatMap.get(gcm).getText()).append("|");
            }
        }
        globalConfig.put("GcmMapping", sbGcmMapping.toString());

        // StdPlot
        stdConfig.put("inputDir", stdplot_inputDir.getText());
        stdConfig.put("title", stdplot_title.getText());
        stdConfig.put("outputPath", stdplot_outputDir.getText());
        stdConfig.put("outputGraph", stdplot_outputGraph.getText());
        stdConfig.put("plotFormat", stdplot_plotFormat.getSelection().getButtonData().toString());
        stdConfig.put("plotType", stdplot_plotType.getSelection().getButtonData().toString());
        stdConfig.put("plotVar", getSelectedVar(stdplot_plotVarLB));

        // CorPlot
        corConfig.put("inputDir", corplot_inputDir.getText());
        corConfig.put("outputPath", corplot_outputDir.getText());
        corConfig.put("outputGraph", corplot_outputGraph.getText());
        corConfig.put("plotFormat", corplot_plotFormat.getSelection().getButtonData().toString());
        corConfig.put("plotVarX", getSelectedVar(corplot_plotVarXLB));
        corConfig.put("plotVarY", getSelectedVar(corplot_plotVarYLB));
        corConfig.put("group1", getSelectedVar(corplot_plotGroup1LB));
        corConfig.put("group2", getSelectedVar(corplot_plotGroup2LB));

        try {
            deployFileByTemplate(new File(CONFIG_FILE), CONFIG_FILE_DEF_TEMPLATE, CONFIG_MAP, "config");
            LOG.info("Done!");
        } catch (IOException ex) {
            LOG.error("An error occured while writing the {} file: {}", CONFIG_FILE, ex.getMessage());
            LOG.error(Functions.getStackTrace(ex));
            Alert.alert(MessageType.ERROR, ex.getMessage(), PlotUIWindow.this);
        }
    }

    private void validateInput() {

        LOG.info("Start validation for {}", curTab.toString());
        ArrayList<String> plotVars = PlotUtil.getValidateVars(curTab);
        String inputDir = PlotUtil.CONFIG_MAP.get(curTab.toString()).get("inputDir");
        final String outputDir = PlotUtil.CONFIG_MAP.get(curTab.toString()).get("outputPath");
        if (inputDir == null || inputDir.equals("")) {
            LOG.warn("Invalid input path for validation!");
        } else if (outputDir == null || outputDir.equals("")) {
            LOG.warn("Invalid output path for validation!");
        } else if (plotVars.isEmpty()) {
            LOG.warn("Invalid plot variables for validation!");
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
                                        runPlot();
                                        break;
                                }
                            }
                        });
                        warning.open(PlotUIWindow.this);
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
                    report.getAlertListeners().add(new AlertListener() {

                        @Override
                        public void messageTypeChanged(Alert alert, MessageType mt) {
                        }

                        @Override
                        public void messageChanged(Alert alert, String string) {
                        }

                        @Override
                        public void bodyChanged(Alert alert, Component cmpnt) {
                        }

                        @Override
                        public void optionInserted(Alert alert, int i) {
                        }

                        @Override
                        public void optionsRemoved(Alert alert, int i, Sequence<?> sqnc) {
                        }

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

            }

            @Override
            public void executeFailed(Task<Integer> task) {
                Alert.alert(MessageType.ERROR, task.getFault().toString(), PlotUIWindow.this);
                LOG.error(Functions.getStackTrace(task.getFault()));
            }
        };
        task.execute(new TaskAdapter(lisener));
    }

    private void setGcmCatMapping(String... gcms) {

        Component title;
        title = gcmLabels.get(0);
        gcmLabels.removeAll();
        gcmLabels.add(title);
        title = gcmCatLabels.get(0);
        gcmCatLabels.removeAll();
        gcmCatLabels.add(title);
        title = gcmCatSelectionLabels.get(0);
        gcmCatSelectionLabels.removeAll();
        gcmCatSelectionLabels.add(title);

        gcmCatMap = new HashMap();
        LinkedHashMap<String, String> gcmCatColorMap = new LinkedHashMap();
        gcmCatColorMap.put("Base", "#D3D3D3");
        gcmCatColorMap.put("Cool-Dry", "blue");
        gcmCatColorMap.put("Cool-Wet", "green");
        gcmCatColorMap.put("Hot-Dry", "red");
        gcmCatColorMap.put("Hot-Wet", "#FFD700");
        gcmCatColorMap.put("Middle", "black");

        DragSource ds = DragDropFactory.createLabelDragSource();
        DropTarget dt = DragDropFactory.createLabelDropTarget();
        HashMap<String, String> gcmCatConfig = new LinkedHashMap();
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
                gcmCatColorMap.remove(gcmCat);
            } else {
                gcmCatLabel = new Label();
            }
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

    private static HashMap<String, RadioButton> initRadioButtonGroup(Map<String, Object> ns, String... ids) {

        HashMap<String, RadioButton> ret = new HashMap();
        for (String id : ids) {
            RadioButton rb = (RadioButton) ns.get(id);
            ret.put(rb.getButtonData().toString(), rb);
        }

        return ret;
    }

    private static void setRadioButtonGroup(ButtonGroup bg, HashMap<String, RadioButton> rbMap, HashMap<String, String> config, String var) {

        RadioButton rb = rbMap.get(MapUtil.getValueOr(config, var, ""));
        if (rb != null) {
            bg.setSelection(rb);
        }
    }

    private static void setSelectionList(ListButton lb, HashMap<String, String> config, String var) { //, String defVal) {
        String configPlotVar = MapUtil.getValueOr(config, var, "");
        if (!configPlotVar.equals("")) {
            for (int i = 0; i < lb.getListData().getLength(); i++) {
                String plotVar = lb.getListData().get(i).toString();
                if (plotVar.contains(configPlotVar)) {
                    lb.setSelectedIndex(i);
                }
            }
        }
    }

    private ButtonPressListener createGenericDirBPListerner(final TextInput input) {
        return new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (input.getText().equals("")) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                } else {
                    if (!new File(input.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(input.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            input.setText(dir.getPath());
                        }
                    }
                });
            }
        };
    }

    private String getSelectedVar(ListButton lb) {
        String selected = (String) lb.getSelectedItem();
        if (selected == null) {
            return "";
        } else if (lb.getName().equalsIgnoreCase("corplot_plotGroup2LB") && selected.startsWith("No")) {
            return "No";
        } else {
            return selected.substring(selected.lastIndexOf("(") + 1).replaceAll("\\)", "");
        }

    }

    private void setNonRepeatedSelection(final ListButton lb1, final ListButton lb2) {
        String lb1OriSelection = (String) lb1.getSelectedItem();
        String lb2OriSelection = (String) lb2.getSelectedItem();
        if (lb1OriSelection != null && lb1OriSelection.equals(lb2OriSelection)) {
            lb2.setSelectedItem(null);
        }
        lb1.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                Object newSelected1 = lb.getSelectedItem();
                Object curSelected = lb2.getSelectedItem();
                if (newSelected1 != null && curSelected != null && newSelected1.equals(curSelected)) {
                    if (o == null && lb2.getName().equalsIgnoreCase("corplot_plotGroup2LB")) {
                        lb2.setSelectedIndex(0);
                    } else {
                        lb2.setSelectedItem(o);
                    }
                }
            }
        });
        lb2.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                Object newSelected1 = lb.getSelectedItem();
                Object curSelected = lb1.getSelectedItem();
                if (newSelected1 != null && curSelected != null && newSelected1.equals(curSelected)) {
                    if (o != null && o.toString().startsWith("No")) {
                        int maxIdx = lb1.getListData().getLength() - 1;
                        int curIdx = lb1.getSelectedIndex();
                        if (curIdx == maxIdx) {
                            lb1.setSelectedIndex(0);
                        } else {
                            lb1.setSelectedIndex(curIdx + 1);
                        }
                    } else {
                        lb1.setSelectedItem(o);
                    }

                }
            }
        });
    }
}
