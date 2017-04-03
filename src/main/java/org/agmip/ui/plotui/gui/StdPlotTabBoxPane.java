package org.agmip.ui.plotui.gui;

import java.net.URL;
import java.util.HashMap;
import org.agmip.ui.plotui.PlotUtil;
import static org.agmip.ui.plotui.gui.GuiUtil.*;
import static org.agmip.ui.plotui.PlotUtil.resolveRelPath;
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
public class StdPlotTabBoxPane extends PlotTabBoxPane implements Bindable {
    
    private static final Logger LOG = LoggerFactory.getLogger(StdPlotTabBoxPane.class);
    
    private TextInput inputDir = null;
    private PushButton inputDirBrowse = null;
    private TextInput inputDir2 = null;
    private PushButton inputDirBrowse2 = null;
    private TextInput outputDir = null;
    private PushButton outputDirBrowse = null;
    private TextInput outputGraph = null;
    private TextInput title = null;
    private ListButton plotVarLB = null;
    private ButtonGroup plotMethod = null;
    private HashMap<String, RadioButton> plotMethodRBMap = null;
    private ButtonGroup plotGrouping = null;
    private HashMap<String, RadioButton> plotGroupingRBMap = null;
    private ButtonGroup plotType = null;
    private HashMap<String, RadioButton> plotTypeRBMap = null;
    private ButtonGroup plotFormat = null;
    private HashMap<String, RadioButton> plotFormatRBMap = null;
    private Checkbox outputACMO = null;
    
    public StdPlotTabBoxPane() {
        super(PlotUtil.RScps.StandardPlot.toString());
    }
    
    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        // Initialization
        inputDir = (TextInput) ns.get("inputDir");
        inputDirBrowse = (PushButton) ns.get("inputDirBrowse");
        inputDir2 = (TextInput) ns.get("inputDir2");
        inputDirBrowse2 = (PushButton) ns.get("inputDirBrowse2");
        outputDir = (TextInput) ns.get("outputDir");
        outputDirBrowse = (PushButton) ns.get("outputDirBrowse");
        outputGraph = (TextInput) ns.get("outputGraph");
        title = (TextInput) ns.get("title");
        plotVarLB = (ListButton) ns.get("plotVarLB");
        plotMethod = (ButtonGroup) ns.get("plotMethodButtons");
        plotMethodRBMap = initRadioButtonGroup(ns, "plotMethod_abs", "plotMethod_rel");
        plotGrouping = (ButtonGroup) ns.get("plotGroupingButtons");
        plotGroupingRBMap = initRadioButtonGroup(ns, "plotGrouping_mgr", "plotGrouping_rcp");
        plotType = (ButtonGroup) ns.get("plotTypeButtons");
        plotTypeRBMap = initRadioButtonGroup(ns, "plotType_box", "plotType_cdf");
        plotFormat = (ButtonGroup) ns.get("plotFormatButtons");
        plotFormatRBMap = initRadioButtonGroup(ns, "plotFormat_pdf", "plotFormat_png");
        outputACMO = (Checkbox) ns.get("outputACMO");

        // Define listeners for buttons
        inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(inputDir));
        inputDirBrowse2.getButtonPressListeners().add(createGenericDirBPListerner(inputDir2));
        outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(outputDir));
        title.getTextInputContentListeners().add(createGenericTitleTBListerner(outputGraph));

    }

    @Override
    public void loadConfig() {

        HashMap<String, String> config = getConfig();
        inputDir.setText(MapUtil.getValueOr(config, "inputDir", ""));
        inputDir2.setText(MapUtil.getValueOr(config, "inputDir2", ""));
        title.setText(MapUtil.getValueOr(config, "title", ""));
        outputDir.setText(MapUtil.getValueOr(config, "outputPath", ""));
        outputGraph.setText(MapUtil.getValueOr(config, "outputGraph", ""));
        setSelectionList(plotVarLB, config, "plotVar");
        setRadioButtonGroup(plotMethod, plotMethodRBMap, config, "plotMethod");
        setRadioButtonGroup(plotGrouping, plotGroupingRBMap, config, "plotGrouping");
        setRadioButtonGroup(plotType, plotTypeRBMap, config, "plotType");
        setRadioButtonGroup(plotFormat, plotFormatRBMap, config, "plotFormat");
        outputACMO.setSelected(!MapUtil.getValueOr(config, "outputACMO", "").equalsIgnoreCase("false"));
        
    }

    @Override
    public void saveConfig() {
        
        String workDir = getGlobalConfigVar("WorkDir");
        HashMap<String, String> config = getConfig();
        config.put("inputDir", resolveRelPath(workDir, inputDir.getText()));
        config.put("inputDir2", resolveRelPath(workDir, inputDir2.getText()));
        config.put("title", title.getText());
        config.put("outputPath", resolveRelPath(workDir, outputDir.getText()));
        config.put("outputGraph", outputGraph.getText());
        config.put("plotFormat", plotFormat.getSelection().getButtonData().toString());
        config.put("plotMethod", plotMethod.getSelection().getButtonData().toString());
        config.put("plotGrouping", plotGrouping.getSelection().getButtonData().toString());
        config.put("plotType", plotType.getSelection().getButtonData().toString());
        config.put("plotVar", getSelectedVar(plotVarLB));
        config.put("outputACMO", outputACMO.isSelected() + "");
    }
}
