package org.agmip.ui.plotui.gui;

import java.net.URL;
import java.util.HashMap;
import org.agmip.ui.plotui.PlotUtil;
import static org.agmip.ui.plotui.PlotUtil.resolveRelPath;
import static org.agmip.ui.plotui.gui.GuiUtil.*;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class CorPlotTabBoxPane extends PlotTabBoxPane implements Bindable {
    
    private static final Logger LOG = LoggerFactory.getLogger(CorPlotTabBoxPane.class);
    
    private TextInput inputDir = null;
    private PushButton inputDirBrowse = null;
    private TextInput outputDir = null;
    private PushButton outputDirBrowse = null;
    private TextInput outputGraph = null;
    private TextInput title = null;
    private ListButton plotVarXLB = null;
    private ListButton plotVarYLB = null;
    private ListButton plotGroup1LB = null;
    private ListButton plotGroup2LB = null;
    private ButtonGroup plotFormat = null;
    private HashMap<String, RadioButton> plotFormatRBMap = null;
    private Checkbox outputACMO = null;
    
    public CorPlotTabBoxPane() {
        super(PlotUtil.RScps.CorrelationPlot.toString());
    }
    
    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        // Initialization
        inputDir = (TextInput) ns.get("inputDir");
        inputDirBrowse = (PushButton) ns.get("inputDirBrowse");
        outputDir = (TextInput) ns.get("outputDir");
        outputDirBrowse = (PushButton) ns.get("outputDirBrowse");
        outputGraph = (TextInput) ns.get("outputGraph");
        title = (TextInput) ns.get("title");
        plotVarXLB = (ListButton) ns.get("plotVarXLB");
        plotVarYLB = (ListButton) ns.get("plotVarYLB");
        plotGroup1LB = (ListButton) ns.get("plotGroup1LB");
        plotGroup2LB = (ListButton) ns.get("plotGroup2LB");
        plotFormat = (ButtonGroup) ns.get("plotFormatButtons");
        plotFormatRBMap = initRadioButtonGroup(ns, "plotFormat_pdf", "plotFormat_png");
        outputACMO = (Checkbox) ns.get("outputACMO");

        // Define listeners for buttons
        inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(inputDir));
        outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(outputDir));
        title.getTextInputContentListeners().add(createGenericTitleTBListerner(outputGraph));
        setNonRepeatedSelection(plotVarXLB, plotVarYLB);
        setNonRepeatedSelection(plotGroup1LB, plotGroup2LB);

    }

    @Override
    public void loadConfig() {

        HashMap<String, String> config = getConfig();
        inputDir.setText(MapUtil.getValueOr(config, "inputDir", ""));
        outputDir.setText(MapUtil.getValueOr(config, "outputPath", ""));
        title.setText(MapUtil.getValueOr(config, "title", ""));
        outputGraph.setText(MapUtil.getValueOr(config, "outputGraph", ""));
        setSelectionList(plotVarXLB, config, "plotVarX");
        setSelectionList(plotVarYLB, config, "plotVarY");
        setSelectionList(plotGroup1LB, config, "group1");
        setSelectionList(plotGroup2LB, config, "group2");
        plotFormat.setSelection(plotFormatRBMap.get(MapUtil.getValueOr(config, "plotFormat", "")));
        setRadioButtonGroup(plotFormat, plotFormatRBMap, config, "plotFormat");
        outputACMO.setSelected(!MapUtil.getValueOr(config, "outputACMO", "").equalsIgnoreCase("false"));
        
    }

    @Override
    public void saveConfig() {
        
        String workDir = getGlobalConfigVar("WorkDir");
        HashMap<String, String> config = getConfig();
        config.put("inputDir", resolveRelPath(workDir, inputDir.getText()));
        config.put("outputPath", resolveRelPath(workDir, outputDir.getText()));
        config.put("outputGraph", outputGraph.getText());
        config.put("title", title.getText());
        config.put("plotFormat", plotFormat.getSelection().getButtonData().toString());
        config.put("plotVarX", getSelectedVar(plotVarXLB));
        config.put("plotVarY", getSelectedVar(plotVarYLB));
        config.put("group1", getSelectedVar(plotGroup1LB));
        config.put("group2", getSelectedVar(plotGroup2LB));
        config.put("outputACMO", outputACMO.isSelected() + "");
    }
}
