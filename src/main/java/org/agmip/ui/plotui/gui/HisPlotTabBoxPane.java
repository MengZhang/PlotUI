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
public class HisPlotTabBoxPane extends PlotTabBoxPane implements Bindable {
    
    private static final Logger LOG = LoggerFactory.getLogger(HisPlotTabBoxPane.class);

    private TextInput inputDir = null;
    private PushButton inputDirBrowse = null;
    private TextInput outputDir = null;
    private PushButton outputDirBrowse = null;
    private TextInput outputGraph = null;
    private TextInput title = null;
    private ListButton plotVarLB = null;
    private ButtonGroup plotMethod = null;
    private HashMap<String, RadioButton> plotMethodRBMap = null;
    private ButtonGroup plotType = null;
    private HashMap<String, RadioButton> plotTypeRBMap = null;
    private ButtonGroup plotFormat = null;
    private HashMap<String, RadioButton> plotFormatRBMap = null;
    private Checkbox outputACMO = null;
    
    public HisPlotTabBoxPane() {
        super(PlotUtil.RScps.HistoricalPlot.toString());
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
        plotVarLB = (ListButton) ns.get("plotVarLB");
        plotMethod = (ButtonGroup) ns.get("plotMethodButtons");
        plotMethodRBMap = initRadioButtonGroup(ns, "plotMethod_abs", "plotMethod_rel");
        plotType = (ButtonGroup) ns.get("plotTypeButtons");
        plotTypeRBMap = initRadioButtonGroup(ns, "plotType_box", "plotType_cdf");
        plotFormat = (ButtonGroup) ns.get("plotFormatButtons");
        plotFormatRBMap = initRadioButtonGroup(ns, "plotFormat_pdf", "plotFormat_png");
        outputACMO = (Checkbox) ns.get("outputACMO");

        // Define listeners for buttons
        inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(inputDir));
        outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(outputDir));
        title.getTextInputContentListeners().add(createGenericTitleTBListerner(outputGraph));

    }

    @Override
    public void loadConfig() {

        HashMap<String, String> config = getConfig();
        inputDir.setText(MapUtil.getValueOr(config, "inputDir", ""));
        title.setText(MapUtil.getValueOr(config, "title", ""));
        outputDir.setText(MapUtil.getValueOr(config, "outputPath", ""));
        outputGraph.setText(MapUtil.getValueOr(config, "outputGraph", ""));
        setSelectionList(plotVarLB, config, "plotVar");
        setRadioButtonGroup(plotMethod, plotMethodRBMap, config, "plotMethod");
        setRadioButtonGroup(plotType, plotTypeRBMap, config, "plotType");
        setRadioButtonGroup(plotFormat, plotFormatRBMap, config, "plotFormat");
        outputACMO.setSelected(!MapUtil.getValueOr(config, "outputACMO", "").equalsIgnoreCase("false"));
        
    }

    @Override
    public void saveConfig() {
        
        String workDir = getGlobalConfigVar("WorkDir");
        HashMap<String, String> config = getConfig();
        config.put("inputDir", resolveRelPath(workDir, inputDir.getText()));
        config.put("title", title.getText());
        config.put("outputPath", resolveRelPath(workDir, outputDir.getText()));
        config.put("outputGraph", outputGraph.getText());
        config.put("plotFormat", plotFormat.getSelection().getButtonData().toString());
        config.put("plotMethod", plotMethod.getSelection().getButtonData().toString());
        config.put("plotType", plotType.getSelection().getButtonData().toString());
        config.put("plotVar", getSelectedVar(plotVarLB));
        config.put("outputACMO", outputACMO.isSelected() + "");
    }
}
