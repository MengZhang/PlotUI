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
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.AlertListener;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
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
    private PushButton saveConfig = null;
    private PushButton runRScp = null;
    private PlotUtil.RScps curTab = PlotUtil.RScps.StandardPlot;
    private TextInput curInputDir = null;
    private TextInput curOutputDir = null;
    private TextInput curoutputGraph = null;

    private TextInput stdplot_inputDir = null;
    private PushButton stdplot_inputDirBrowse = null;
    private TextInput stdplot_outputDir = null;
    private PushButton stdplot_outputDirBrowse = null;
    private TextInput stdplot_outputGraph = null;
    private TextInput stdplot_title = null;
    private ListButton stdplot_plotVarLB = null;
    private ButtonGroup stdplot_plotType = null;
    private RadioButton stdplot_plotType_box = null;
    private RadioButton stdplot_plotType_cdf = null;
    private ButtonGroup stdplot_plotFormat = null;
    private RadioButton stdplot_plotFormat_pdf = null;
    private RadioButton stdplot_plotFormat_png = null;

    private final HashMap<String, String> stdConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.StandardPlot.toString());
    private String stdplot_selected_plotVar = "";

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
        saveConfig = (PushButton) ns.get("saveConfig");
        runRScp = (PushButton) ns.get("runRScp");
//        acebText = (TextInput) ns.get("acebText");
//        dataListBd = (Border) ns.get("dataList");
//        dataDetailBd = (Border) ns.get("dataDetail");
        txtVersion = (Label) ns.get("txtVersion");
        stdplot_inputDir = (TextInput) ns.get("stdplot_inputDir");
        stdplot_inputDirBrowse = (PushButton) ns.get("stdplot_inputDirBrowse");
        stdplot_outputDir = (TextInput) ns.get("stdplot_outputDir");
        stdplot_outputDirBrowse = (PushButton) ns.get("stdplot_outputDirBrowse");
        stdplot_outputGraph = (TextInput) ns.get("stdplot_outputGraph");
        stdplot_title = (TextInput) ns.get("stdplot_title");
        stdplot_plotVarLB = (ListButton) ns.get("stdplot_plotVarLB");
        stdplot_plotType = (ButtonGroup) ns.get("stdplot_plotTypeButtons");
        stdplot_plotType_box = (RadioButton) ns.get("stdplot_plotType_box");
        stdplot_plotType_cdf = (RadioButton) ns.get("stdplot_plotType_cdf");
        stdplot_plotFormat = (ButtonGroup) ns.get("stdplot_plotFormatButtons");
        stdplot_plotFormat_pdf = (RadioButton) ns.get("stdplot_plotFormat_pdf");
        stdplot_plotFormat_png = (RadioButton) ns.get("stdplot_plotFormat_png");

        // Load configuration from XML into GUI
        stdplot_inputDir.setText(MapUtil.getValueOr(stdConfig, "inputDir", ""));
        stdplot_title.setText(MapUtil.getValueOr(stdConfig, "title", ""));
        stdplot_outputDir.setText(MapUtil.getValueOr(stdConfig, "outputPath", ""));
        stdplot_outputGraph.setText(MapUtil.getValueOr(stdConfig, "outputGraph", ""));
        String configPlotVar = MapUtil.getValueOr(stdConfig, "plotVar", "");
        if (!configPlotVar.equals("")) {
            for (int i = 0; i < stdplot_plotVarLB.getListData().getLength(); i++) {
                String plotVar = stdplot_plotVarLB.getListData().get(i).toString();
                if (plotVar.contains(configPlotVar)) {
                    stdplot_plotVarLB.setSelectedIndex(i);
                    stdplot_selected_plotVar = configPlotVar;
                }
            }
        }
        String configPlotType = MapUtil.getValueOr(stdConfig, "plotType", "");
        if (configPlotType.equalsIgnoreCase("BoxPlot")) {
            stdplot_plotType.setSelection(stdplot_plotType_box);
        } else if (configPlotType.equalsIgnoreCase("CDF")) {
            stdplot_plotType.setSelection(stdplot_plotType_cdf);
        }
        String configPlotFormat = MapUtil.getValueOr(stdConfig, "plotFormat", "");
        if (configPlotFormat.equals("pdf")) {
            stdplot_plotFormat.setSelection(stdplot_plotFormat_pdf);
        } else if (configPlotFormat.equals("png")) {
            stdplot_plotFormat.setSelection(stdplot_plotFormat_png);
        }

        // set default tab
        switchCurTab();

        saveConfig.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                saveStdPlotConfigToMap();
                LOG.info("Saving {} ...", CONFIG_FILE);
                try {
                    deployFileByTemplate(new File(CONFIG_FILE), CONFIG_FILE_DEF_TEMPLATE, CONFIG_MAP, "config");
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

                saveStdPlotConfigToMap();
                try {
                    deployFileByTemplate(new File(CONFIG_FILE), CONFIG_FILE_DEF_TEMPLATE, CONFIG_MAP, "config");
                    LOG.info("Done!");
                } catch (IOException ex) {
                    LOG.error("An error occured while writing the {} file: {}", CONFIG_FILE, ex.getMessage());
                    LOG.error(Functions.getStackTrace(ex));
                    Alert.alert(MessageType.ERROR, ex.getMessage(), PlotUIWindow.this);
                }
                ValidationTask task = new ValidationTask(PlotUtil.getAllInputFiles(new File(stdplot_inputDir.getText())), stdplot_selected_plotVar);
                TaskListener lisener = new TaskListener<LinkedHashMap<File, ArrayList<HashMap<String, String>>>>() {

                    @Override
                    public void taskExecuted(Task<LinkedHashMap<File, ArrayList<HashMap<String, String>>>> task) {

                        LinkedHashMap<File, ArrayList<HashMap<String, String>>> result = task.getResult();
                        if (!result.isEmpty()) {

                            // Generate report
                            final File report = PlotUtil.generateReport(result, curOutputDir.getText());

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
                            warning.getAlertListeners().add(new AlertListener() {

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
                                        case "Report":
                                            LOG.info(userChoice);
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
                                            LOG.info(userChoice);
                                            runPlot();
                                            break;
                                    }
                                }
                            });
                            warning.open(PlotUIWindow.this);
                        } else {
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
        });

        stdplot_inputDirBrowse.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (stdplot_inputDir.getText().equals("")) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                } else {
                    if (!new File(stdplot_inputDir.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(stdplot_inputDir.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            stdplot_inputDir.setText(dir.getPath());
//                            if (stdConfig != null) {
//                                stdConfig.put("inputDir", dir.getPath());
//                            }
                        }
                    }
                });
            }
        });

        stdplot_outputDirBrowse.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (stdplot_outputDir.getText().equals("")) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                } else {
                    if (!new File(stdplot_outputDir.getText()).exists()) {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
                    } else {
                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(stdplot_outputDir.getText()).getAbsoluteFile().getParentFile().getPath());
                    }
                }
                browse.open(PlotUIWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            stdplot_outputDir.setText(dir.getPath());
//                            if (stdConfig != null) {
//                                stdConfig.put("outputDir", dir.getPath());
//                            }
                        }
                    }
                });
            }
        });

        stdplot_plotVarLB.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                String selected = stdplot_plotVarLB.getSelectedItem().toString();
                LOG.debug(selected);
                stdplot_selected_plotVar = selected.substring(selected.lastIndexOf("(") + 1).replaceAll("\\)", "");
                LOG.debug(selected);

            }
        });

    }

    private void runPlot() {

        RunPlotTask task = new RunPlotTask(curTab);
        TaskListener<Integer> lisener = new TaskListener<Integer>() {

            @Override
            public void taskExecuted(Task<Integer> task) {
                BoxPane body = new BoxPane(Orientation.VERTICAL);
                StringBuilder stdPlotFileName = new StringBuilder();
                stdPlotFileName.append(stdConfig.get("outputGraph"));
                stdPlotFileName.append("-").append(stdConfig.get("plotType"));
                stdPlotFileName.append("-ABSOLUTE");
                stdPlotFileName.append("-").append(stdConfig.get("plotVar"));
                stdPlotFileName.append(".").append(stdConfig.get("plotFormat"));
                final File plotFile = Paths.get(stdplot_outputDir.getText(), stdPlotFileName.toString()).toFile();
                LOG.debug(plotFile.getPath());
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
                Alert.alert(MessageType.ERROR, task.getFault().getMessage(), PlotUIWindow.this);
            }
        };
        task.execute(new TaskAdapter(lisener));
    }

    private void saveStdPlotConfigToMap() {
        stdConfig.put("inputDir", stdplot_inputDir.getText());
        stdConfig.put("title", stdplot_title.getText());
        stdConfig.put("outputPath", stdplot_outputDir.getText());
        stdConfig.put("outputGraph", stdplot_outputGraph.getText());
        stdConfig.put("plotFormat", stdplot_plotFormat.getSelection().getButtonData().toString().toLowerCase());
        stdConfig.put("plotType", stdplot_plotType.getSelection().getButtonData().toString());
        stdConfig.put("plotVar", stdplot_selected_plotVar);
    }

    private void switchCurTab() {
        if (curTab.equals(PlotUtil.RScps.StandardPlot)) {
            curInputDir = stdplot_inputDir;
            curOutputDir = stdplot_outputDir;
            curoutputGraph = stdplot_outputGraph;
        } else if (curTab.equals(PlotUtil.RScps.CorrelationPlot)) {

        } else if (curTab.equals(PlotUtil.RScps.ClimAnomaly)) {

        }
    }
}
