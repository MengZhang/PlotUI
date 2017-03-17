package org.agmip.ui.plotui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.agmip.common.Functions;
import static org.agmip.ui.plotui.PlotUtil.*;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
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
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
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
    private PlotUtil.RScps curTab = PlotUtil.RScps.StandardPlot;
    private BoxPane gcmLabels = null;
    private BoxPane gcmCatLabels = null;
    private BoxPane gcmCatSelectionLabels = null;
    private ActivityIndicator mappingIndicator = null;
    private ActivityIndicator runIndicator = null;
    HashMap<String, Label> gcmCatMap = new HashMap();

    private TextInput global_rExePath = null;
    private PushButton global_browseRExePath = null;
    private TextInput global_rLibPath = null;
    private PushButton global_browseRLibPath = null;
    private TextInput global_workDir = null;
    private PushButton global_browseWorkDir = null;

    private TextInput stdplot_inputDir = null;
    private PushButton stdplot_inputDirBrowse = null;
    private TextInput stdplot_inputDir2 = null;
    private PushButton stdplot_inputDirBrowse2 = null;
    private TextInput stdplot_outputDir = null;
    private PushButton stdplot_outputDirBrowse = null;
    private TextInput stdplot_outputGraph = null;
    private TextInput stdplot_title = null;
    private ListButton stdplot_plotVarLB = null;
    private ButtonGroup stdplot_plotMethod = null;
    private HashMap<String, RadioButton> stdplot_plotMethodRBMap = null;
    private ButtonGroup stdplot_plotType = null;
    private HashMap<String, RadioButton> stdplot_plotTypeRBMap = null;
    private ButtonGroup stdplot_plotFormat = null;
    private HashMap<String, RadioButton> stdplot_plotFormatRBMap = null;
    private Checkbox stdplot_outputACMO = null;

    private TextInput corplot_inputDir = null;
    private PushButton corplot_inputDirBrowse = null;
    private TextInput corplot_outputDir = null;
    private PushButton corplot_outputDirBrowse = null;
    private TextInput corplot_outputGraph = null;
    private TextInput corplot_title = null;
    private ListButton corplot_plotVarXLB = null;
    private ListButton corplot_plotVarYLB = null;
    private ListButton corplot_plotGroup1LB = null;
    private ListButton corplot_plotGroup2LB = null;
    private ButtonGroup corplot_plotFormat = null;
    private HashMap<String, RadioButton> corplot_plotFormatRBMap = null;

    private TextInput ctwnplot_inputDir = null;
    private PushButton ctwnplot_inputDirBrowse = null;
    private TextInput ctwnplot_outputDir = null;
    private PushButton ctwnplot_outputDirBrowse = null;
    private TextInput ctwnplot_outputSubFolder = null;
    private ListButton ctwnplot_plotVarLB = null;
    private ButtonGroup ctwnplot_plotFormat = null;
    private HashMap<String, RadioButton> ctwnplot_plotFormatRBMap = null;
    private Checkbox ctwnplot_outputACMO = null;

    private TextInput hisplot_inputDir = null;
    private PushButton hisplot_inputDirBrowse = null;
    private TextInput hisplot_outputDir = null;
    private PushButton hisplot_outputDirBrowse = null;
    private TextInput hisplot_outputGraph = null;
    private TextInput hisplot_title = null;
    private ListButton hisplot_plotVarLB = null;
    private ButtonGroup hisplot_plotMethod = null;
    private HashMap<String, RadioButton> hisplot_plotMethodRBMap = null;
    private ButtonGroup hisplot_plotType = null;
    private HashMap<String, RadioButton> hisplot_plotTypeRBMap = null;
    private ButtonGroup hisplot_plotFormat = null;
    private HashMap<String, RadioButton> hisplot_plotFormatRBMap = null;
    private Checkbox hisplot_outputACMO = null;

    private HashMap<String, String> globalConfig = getConfig(PlotUtil.GLOBAL_CONFIG);
    private HashMap<String, String> stdConfig = getConfig(PlotUtil.RScps.StandardPlot.toString());
    private HashMap<String, String> corConfig = getConfig(PlotUtil.RScps.CorrelationPlot.toString());
    private HashMap<String, String> ctwnConfig = getConfig(PlotUtil.RScps.CTWNPlot.toString());
    private HashMap<String, String> hisConfig = getConfig(PlotUtil.RScps.HistoricalPlot.toString());

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
        global_workDir = (TextInput) ns.get("global_workDir");
        global_browseWorkDir = (PushButton) ns.get("global_browseWorkDir");
        gcmLabels = (BoxPane) ns.get("global_gcmLabels");
        gcmCatLabels = (BoxPane) ns.get("global_gcmCatLabels");
        gcmCatSelectionLabels = (BoxPane) ns.get("global_gcmCatSelectionLabels");
        mappingIndicator = (ActivityIndicator) ns.get("mappingIndicator");
        runIndicator = (ActivityIndicator) ns.get("runIndicator");
        plotuiTabs = (TabPane) ns.get("plotuiTabs");
        saveConfig = (PushButton) ns.get("saveConfig");
        runRScp = (PushButton) ns.get("runRScp");
        txtVersion = (Label) ns.get("txtVersion");

        // Standard Plot
        stdplot_inputDir = (TextInput) ns.get("stdplot_inputDir");
        stdplot_inputDirBrowse = (PushButton) ns.get("stdplot_inputDirBrowse");
        stdplot_inputDir2 = (TextInput) ns.get("stdplot_inputDir2");
        stdplot_inputDirBrowse2 = (PushButton) ns.get("stdplot_inputDirBrowse2");
        stdplot_outputDir = (TextInput) ns.get("stdplot_outputDir");
        stdplot_outputDirBrowse = (PushButton) ns.get("stdplot_outputDirBrowse");
        stdplot_outputGraph = (TextInput) ns.get("stdplot_outputGraph");
        stdplot_title = (TextInput) ns.get("stdplot_title");
        stdplot_plotVarLB = (ListButton) ns.get("stdplot_plotVarLB");
        stdplot_plotMethod = (ButtonGroup) ns.get("stdplot_plotMethodButtons");
        stdplot_plotMethodRBMap = initRadioButtonGroup(ns, "stdplot_plotMethod_abs", "stdplot_plotMethod_rel");
        stdplot_plotType = (ButtonGroup) ns.get("stdplot_plotTypeButtons");
        stdplot_plotTypeRBMap = initRadioButtonGroup(ns, "stdplot_plotType_box", "stdplot_plotType_cdf");
        stdplot_plotFormat = (ButtonGroup) ns.get("stdplot_plotFormatButtons");
        stdplot_plotFormatRBMap = initRadioButtonGroup(ns, "stdplot_plotFormat_pdf", "stdplot_plotFormat_png");
        stdplot_outputACMO = (Checkbox) ns.get("stdplot_outputACMO");

        // Correlation Plot
        corplot_inputDir = (TextInput) ns.get("corplot_inputDir");
        corplot_inputDirBrowse = (PushButton) ns.get("corplot_inputDirBrowse");
        corplot_outputDir = (TextInput) ns.get("corplot_outputDir");
        corplot_outputDirBrowse = (PushButton) ns.get("corplot_outputDirBrowse");
        corplot_outputGraph = (TextInput) ns.get("corplot_outputGraph");
        corplot_title = (TextInput) ns.get("corplot_title");
        corplot_plotVarXLB = (ListButton) ns.get("corplot_plotVarXLB");
        corplot_plotVarYLB = (ListButton) ns.get("corplot_plotVarYLB");
        corplot_plotGroup1LB = (ListButton) ns.get("corplot_plotGroup1LB");
        corplot_plotGroup2LB = (ListButton) ns.get("corplot_plotGroup2LB");
        corplot_plotFormat = (ButtonGroup) ns.get("corplot_plotFormatButtons");
        corplot_plotFormatRBMap = initRadioButtonGroup(ns, "corplot_plotFormat_pdf", "corplot_plotFormat_png");

        // CTWN Plot
        ctwnplot_inputDir = (TextInput) ns.get("ctwnplot_inputDir");
        ctwnplot_inputDirBrowse = (PushButton) ns.get("ctwnplot_inputDirBrowse");
        ctwnplot_outputDir = (TextInput) ns.get("ctwnplot_outputDir");
        ctwnplot_outputDirBrowse = (PushButton) ns.get("ctwnplot_outputDirBrowse");
        ctwnplot_outputSubFolder = (TextInput) ns.get("ctwnplot_outputSubFolder");
        ctwnplot_plotVarLB = (ListButton) ns.get("ctwnplot_plotVarLB");
        ctwnplot_plotFormat = (ButtonGroup) ns.get("ctwnplot_plotFormatButtons");
        ctwnplot_plotFormatRBMap = initRadioButtonGroup(ns, "ctwnplot_plotFormat_pdf", "ctwnplot_plotFormat_png");
        ctwnplot_outputACMO = (Checkbox) ns.get("ctwnplot_outputACMO");

        // Historical Plot
        hisplot_inputDir = (TextInput) ns.get("hisplot_inputDir");
        hisplot_inputDirBrowse = (PushButton) ns.get("hisplot_inputDirBrowse");
        hisplot_outputDir = (TextInput) ns.get("hisplot_outputDir");
        hisplot_outputDirBrowse = (PushButton) ns.get("hisplot_outputDirBrowse");
        hisplot_outputGraph = (TextInput) ns.get("hisplot_outputGraph");
        hisplot_title = (TextInput) ns.get("hisplot_title");
        hisplot_plotVarLB = (ListButton) ns.get("hisplot_plotVarLB");
        hisplot_plotMethod = (ButtonGroup) ns.get("hisplot_plotMethodButtons");
        hisplot_plotMethodRBMap = initRadioButtonGroup(ns, "hisplot_plotMethod_abs", "hisplot_plotMethod_rel");
        hisplot_plotType = (ButtonGroup) ns.get("hisplot_plotTypeButtons");
        hisplot_plotTypeRBMap = initRadioButtonGroup(ns, "hisplot_plotType_box", "hisplot_plotType_cdf");
        hisplot_plotFormat = (ButtonGroup) ns.get("hisplot_plotFormatButtons");
        hisplot_plotFormatRBMap = initRadioButtonGroup(ns, "hisplot_plotFormat_pdf", "hisplot_plotFormat_png");
        hisplot_outputACMO = (Checkbox) ns.get("hisplot_outputACMO");

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
                        curTab = PlotUtil.RScps.CTWNPlot;
                    } else if (curIdx == 4) {
                        curTab = PlotUtil.RScps.HistoricalPlot;
                    } else if (curIdx == 5) {
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

                if (!new File(global_rExePath.getText()).exists()) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File("").getAbsolutePath());
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File(global_rExePath.getText()).getAbsoluteFile().getParentFile().getPath());
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
        global_browseWorkDir.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

//                if (global_workDir.getText().equals("")) {
//                    if (!new File(stdplot_inputDir.getText()).exists()) {
//                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File("").getAbsolutePath());
//                    } else {
//                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(stdplot_inputDir.getText()).getAbsoluteFile().getParentFile().getPath());
//                    }
//                } else {
                if (!new File(global_workDir.getText()).exists()) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File("").getAbsolutePath());
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(global_workDir.getText()).getAbsoluteFile().getParentFile().getPath());
                }
//                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            global_workDir.setText(dir.getPath());
                            setGcmCatMapping(PlotUtil.getGcms(dir));
                        }
                        mappingIndicator.setActive(false);
                    }
                });
                mappingIndicator.setActive(true);
            }
        });

        global_workDir.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {

            @Override
            public void textChanged(TextInput ti) {
                File config = Paths.get(ti.getText(), PlotUtil.CONFIG_FILE).toFile();
                if (config.isFile()) {
                    CONFIG_MAP = PlotUtil.readConfig(config, ti.getText());
                    globalConfig = getConfig(PlotUtil.GLOBAL_CONFIG);
                    stdConfig = getConfig(PlotUtil.RScps.StandardPlot.toString());
                    corConfig = getConfig(PlotUtil.RScps.CorrelationPlot.toString());
                    ctwnConfig = getConfig(PlotUtil.RScps.CTWNPlot.toString());
                    hisConfig = getConfig(PlotUtil.RScps.HistoricalPlot.toString());
                    loadAllConfig();
                    LOG.info("Load config file from working directory <{}>", ti.getText());
                } else {
                    LOG.info("Not detect existing config under working directory, will create a new config file instead");
                }
                globalConfig.put("WorkDir", ti.getText());
            }
        });

        stdplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(stdplot_inputDir));
        stdplot_inputDirBrowse2.getButtonPressListeners().add(createGenericDirBPListerner(stdplot_inputDir2));
        stdplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(stdplot_outputDir));
        stdplot_title.getTextInputContentListeners().add(createGenericTitleTBListerner(stdplot_outputGraph));

        corplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(corplot_inputDir));
        corplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(corplot_outputDir));
        corplot_title.getTextInputContentListeners().add(createGenericTitleTBListerner(corplot_outputGraph));
        setNonRepeatedSelection(corplot_plotVarXLB, corplot_plotVarYLB);
        setNonRepeatedSelection(corplot_plotGroup1LB, corplot_plotGroup2LB);

        ctwnplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(ctwnplot_inputDir));
        ctwnplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(ctwnplot_outputDir));
//        ctwnplot_outputSubFolder.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
//
//            
//            @Override
//            public void textChanged(TextInput ti) {
//                String var = getSelectedVar(ctwnplot_plotVarLB);
//                String text = ti.getText();
//                if (!text.equals("") && !var.equals("") && !text.contains(var)) {
//                    text = text.replaceAll("-\\w+_S$", "");
//                    ti.setText(text + "-" + var);
//                } else if (!text.endsWith(var) && text.contains(var)) {
//                    text = text.replaceAll("-\\w+_S", "");
//                    ti.setText(text + "-" + var);
//                }
//            }
//        });
        ctwnplot_plotVarLB.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                String var = getSelectedVar(ctwnplot_plotVarLB);
                String text = ctwnplot_outputSubFolder.getText();
                if (!text.equals("") && !var.equals("") && !text.endsWith(var)) {
                    text = text.replaceAll("-\\w+_S$", "");
                    ctwnplot_outputSubFolder.setText(text + "-" + var);
                }
            }
        });
        
        hisplot_inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(hisplot_inputDir));
        hisplot_outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(hisplot_outputDir));
        hisplot_title.getTextInputContentListeners().add(createGenericTitleTBListerner(hisplot_outputGraph));

        
        // Load configuration from XML into GUI
        loadAllConfig();
        // Global
        global_rExePath.setText(MapUtil.getValueOr(globalConfig, "RExePath", ""));
        global_rLibPath.setText(MapUtil.getValueOr(globalConfig, "RLibPath", ""));
        global_workDir.setText(MapUtil.getValueOr(globalConfig, "WorkDir", ""));

    }

    private void loadAllConfig() {

        // Set GCM mapping from config.xml
        setGcmCatMapping();
        // StdPlot
        stdplot_inputDir.setText(MapUtil.getValueOr(stdConfig, "inputDir", ""));
        stdplot_inputDir2.setText(MapUtil.getValueOr(stdConfig, "inputDir2", ""));
        stdplot_title.setText(MapUtil.getValueOr(stdConfig, "title", ""));
        stdplot_outputDir.setText(MapUtil.getValueOr(stdConfig, "outputPath", ""));
        stdplot_outputGraph.setText(MapUtil.getValueOr(stdConfig, "outputGraph", ""));
        setSelectionList(stdplot_plotVarLB, stdConfig, "plotVar");
        setRadioButtonGroup(stdplot_plotMethod, stdplot_plotMethodRBMap, stdConfig, "plotMethod");
        setRadioButtonGroup(stdplot_plotType, stdplot_plotTypeRBMap, stdConfig, "plotType");
        setRadioButtonGroup(stdplot_plotFormat, stdplot_plotFormatRBMap, stdConfig, "plotFormat");
        stdplot_outputACMO.setSelected(!MapUtil.getValueOr(stdConfig, "outputACMO", "").equalsIgnoreCase("false"));
        // CorPlot
        corplot_inputDir.setText(MapUtil.getValueOr(corConfig, "inputDir", ""));
        corplot_outputDir.setText(MapUtil.getValueOr(corConfig, "outputPath", ""));
        corplot_title.setText(MapUtil.getValueOr(corConfig, "title", ""));
        corplot_outputGraph.setText(MapUtil.getValueOr(corConfig, "outputGraph", ""));
        setSelectionList(corplot_plotVarXLB, corConfig, "plotVarX");
        setSelectionList(corplot_plotVarYLB, corConfig, "plotVarY");
        setSelectionList(corplot_plotGroup1LB, corConfig, "group1");
        setSelectionList(corplot_plotGroup2LB, corConfig, "group2");
        corplot_plotFormat.setSelection(corplot_plotFormatRBMap.get(MapUtil.getValueOr(corConfig, "plotFormat", "")));
        setRadioButtonGroup(corplot_plotFormat, corplot_plotFormatRBMap, corConfig, "plotFormat");
        // CTWNPlot
        ctwnplot_inputDir.setText(MapUtil.getValueOr(ctwnConfig, "inputDir", ""));
        String ctwnOutput = MapUtil.getValueOr(ctwnConfig, "outputPath", "");
        if (!ctwnOutput.equals("")) {
            ctwnplot_outputDir.setText(new File(ctwnOutput).getParentFile().getPath());
            ctwnplot_outputSubFolder.setText(new File(ctwnOutput).getName());
        }
        setSelectionList(ctwnplot_plotVarLB, ctwnConfig, "plotVar");
        setRadioButtonGroup(ctwnplot_plotFormat, ctwnplot_plotFormatRBMap, ctwnConfig, "plotFormat");
        String ctwnFlag = MapUtil.getValueOr(ctwnConfig, "outputACMO", "");
        ctwnplot_outputACMO.setSelected(!ctwnFlag.equalsIgnoreCase("false"));
        // HistoricalPlot
        hisplot_inputDir.setText(MapUtil.getValueOr(hisConfig, "inputDir", ""));
        hisplot_title.setText(MapUtil.getValueOr(hisConfig, "title", ""));
        hisplot_outputDir.setText(MapUtil.getValueOr(hisConfig, "outputPath", ""));
        hisplot_outputGraph.setText(MapUtil.getValueOr(hisConfig, "outputGraph", ""));
        setSelectionList(hisplot_plotVarLB, hisConfig, "plotVar");
        setRadioButtonGroup(hisplot_plotMethod, hisplot_plotMethodRBMap, hisConfig, "plotMethod");
        setRadioButtonGroup(hisplot_plotType, hisplot_plotTypeRBMap, hisConfig, "plotType");
        setRadioButtonGroup(hisplot_plotFormat, hisplot_plotFormatRBMap, hisConfig, "plotFormat");
        hisplot_outputACMO.setSelected(!MapUtil.getValueOr(hisConfig, "outputACMO", "").equalsIgnoreCase("false"));
    }

    private void saveAllConfig() throws IOException {

        LOG.info("Saving {} ...", CONFIG_FILE);

        // GloBal
        globalConfig.put("RExePath", global_rExePath.getText());
        globalConfig.put("RLibPath", global_rLibPath.getText());
        globalConfig.put("WorkDir", global_workDir.getText());
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
        stdConfig.put("inputDir", resolveRelPath(global_workDir.getText(), stdplot_inputDir.getText()));
        stdConfig.put("inputDir2", resolveRelPath(global_workDir.getText(), stdplot_inputDir2.getText()));
        stdConfig.put("title", stdplot_title.getText());
        stdConfig.put("outputPath", resolveRelPath(global_workDir.getText(), stdplot_outputDir.getText()));
        stdConfig.put("outputGraph", stdplot_outputGraph.getText());
        stdConfig.put("plotFormat", stdplot_plotFormat.getSelection().getButtonData().toString());
        stdConfig.put("plotMethod", stdplot_plotMethod.getSelection().getButtonData().toString());
        stdConfig.put("plotType", stdplot_plotType.getSelection().getButtonData().toString());
        stdConfig.put("plotVar", getSelectedVar(stdplot_plotVarLB));
        stdConfig.put("outputACMO", stdplot_outputACMO.isSelected() + "");

        // CorPlot
        corConfig.put("inputDir", resolveRelPath(global_workDir.getText(), corplot_inputDir.getText()));
        corConfig.put("outputPath", resolveRelPath(global_workDir.getText(), corplot_outputDir.getText()));
        corConfig.put("outputGraph", corplot_outputGraph.getText());
        corConfig.put("title", corplot_title.getText());
        corConfig.put("plotFormat", corplot_plotFormat.getSelection().getButtonData().toString());
        corConfig.put("plotVarX", getSelectedVar(corplot_plotVarXLB));
        corConfig.put("plotVarY", getSelectedVar(corplot_plotVarYLB));
        corConfig.put("group1", getSelectedVar(corplot_plotGroup1LB));
        corConfig.put("group2", getSelectedVar(corplot_plotGroup2LB));

        // CTWNPlot
        ctwnConfig.put("inputDir", resolveRelPath(global_workDir.getText(), ctwnplot_inputDir.getText()));
        ctwnConfig.put("outputPath", resolveRelPath(global_workDir.getText(), Paths.get(ctwnplot_outputDir.getText(), ctwnplot_outputSubFolder.getText()).toString()));
        ctwnConfig.put("plotFormat", ctwnplot_plotFormat.getSelection().getButtonData().toString());
        ctwnConfig.put("plotVar", getSelectedVar(ctwnplot_plotVarLB));
        ctwnConfig.put("outputACMO", ctwnplot_outputACMO.isSelected() + "");

        // HistoricalPlot
        hisConfig.put("inputDir", resolveRelPath(global_workDir.getText(), hisplot_inputDir.getText()));
        hisConfig.put("title", hisplot_title.getText());
        hisConfig.put("outputPath", resolveRelPath(global_workDir.getText(), hisplot_outputDir.getText()));
        hisConfig.put("outputGraph", hisplot_outputGraph.getText());
        hisConfig.put("plotFormat", hisplot_plotFormat.getSelection().getButtonData().toString());
        hisConfig.put("plotMethod", hisplot_plotMethod.getSelection().getButtonData().toString());
        hisConfig.put("plotType", hisplot_plotType.getSelection().getButtonData().toString());
        hisConfig.put("plotVar", getSelectedVar(hisplot_plotVarLB));
        hisConfig.put("outputACMO", hisplot_outputACMO.isSelected() + "");

        deployFileByTemplate(new File(CONFIG_FILE), CONFIG_FILE_DEF_TEMPLATE, CONFIG_MAP, "config");
        deployFileByTemplate(Paths.get(MapUtil.getValueOr(globalConfig, "WorkDir", ""), CONFIG_FILE).toFile(), CONFIG_FILE_PROJECT_TEMPLATE, CONFIG_MAP, "config");
        PlotUtil.resolvePath(PlotUtil.CONFIG_MAP, global_workDir.getText());
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
            gcmCatLabel.setName("global_gcmCatLabels_" + gcm);
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
            gcmCatLabel.setName("global_gcmCatSelectionLabels_" + gcmCat);
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
                if (!new File(input.getText()).exists()) {
                    File workDir = new File(MapUtil.getValueOr(globalConfig, "WorkDir", ""));
                    if (workDir.exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, workDir.getAbsolutePath());
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File("").getAbsolutePath());
                    }
                    
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(input.getText()).getAbsoluteFile().getParentFile().getPath());
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

    private TextInputContentListener createGenericTitleTBListerner(final TextInput corInput) {
        return new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput ti) {
                corInput.setText(ti.getText());
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
