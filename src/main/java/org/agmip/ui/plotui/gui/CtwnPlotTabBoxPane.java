package org.agmip.ui.plotui.gui;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
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
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class CtwnPlotTabBoxPane extends PlotTabBoxPane implements Bindable {
    
    private static final Logger LOG = LoggerFactory.getLogger(CtwnPlotTabBoxPane.class);
    
    private TextInput inputDir = null;
    private PushButton inputDirBrowse = null;
    private TextInput outputDir = null;
    private PushButton outputDirBrowse = null;
    private TextInput outputSubFolder = null;
    private ListButton plotVarLB = null;
    private ButtonGroup plotFormat = null;
    private HashMap<String, RadioButton> plotFormatRBMap = null;
    private Checkbox outputACMO = null;
    
    public CtwnPlotTabBoxPane() {
        super(PlotUtil.RScps.CTWNPlot.toString());
    }
    
    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        // Initialization
        inputDir = (TextInput) ns.get("inputDir");
        inputDirBrowse = (PushButton) ns.get("inputDirBrowse");
        outputDir = (TextInput) ns.get("outputDir");
        outputDirBrowse = (PushButton) ns.get("outputDirBrowse");
        outputSubFolder = (TextInput) ns.get("outputSubFolder");
        plotVarLB = (ListButton) ns.get("plotVarLB");
        plotFormat = (ButtonGroup) ns.get("plotFormatButtons");
        plotFormatRBMap = initRadioButtonGroup(ns, "plotFormat_pdf", "plotFormat_png");
        outputACMO = (Checkbox) ns.get("outputACMO");

        // Define listeners for buttons
        inputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(inputDir));
        outputDirBrowse.getButtonPressListeners().add(createGenericDirBPListerner(outputDir));
//        outputSubFolder.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
//
//            
//            @Override
//            public void textChanged(TextInput ti) {
//                String var = getSelectedVar(plotVarLB);
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
        plotVarLB.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                String var = getSelectedVar(plotVarLB);
                String text = outputSubFolder.getText();
                if (!text.equals("") && !var.equals("") && !text.endsWith(var)) {
                    text = text.replaceAll("-\\w+_S$", "");
                    outputSubFolder.setText(text + "-" + var);
                }
            }
        });

    }

    @Override
    public void loadConfig() {

        HashMap<String, String> config = getConfig();
        inputDir.setText(MapUtil.getValueOr(config, "inputDir", ""));
        String ctwnOutput = MapUtil.getValueOr(config, "outputPath", "");
        if (!ctwnOutput.equals("")) {
            outputDir.setText(new File(ctwnOutput).getParentFile().getPath());
            outputSubFolder.setText(new File(ctwnOutput).getName());
        }
        setSelectionList(plotVarLB, config, "plotVar");
        setRadioButtonGroup(plotFormat, plotFormatRBMap, config, "plotFormat");
        String ctwnFlag = MapUtil.getValueOr(config, "outputACMO", "");
        outputACMO.setSelected(!ctwnFlag.equalsIgnoreCase("false"));
        
    }

    @Override
    public void saveConfig() {
        
        String workDir = getGlobalConfigVar("WorkDir");
        HashMap<String, String> config = getConfig();
        config.put("inputDir", resolveRelPath(workDir, inputDir.getText()));
        config.put("outputPath", resolveRelPath(workDir, Paths.get(outputDir.getText(), outputSubFolder.getText()).toString()));
        config.put("plotFormat", plotFormat.getSelection().getButtonData().toString());
        config.put("plotVar", getSelectedVar(plotVarLB));
        config.put("outputACMO", outputACMO.isSelected() + "");
    }
}
