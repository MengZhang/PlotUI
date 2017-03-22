package org.agmip.ui.plotui.gui;

import java.io.File;
import java.util.HashMap;
import org.agmip.ui.plotui.PlotUtil;
import org.agmip.util.MapUtil;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;

/**
 *
 * @author Meng Zhang
 */
public class GuiUtil {
    
    public static HashMap<String, RadioButton> initRadioButtonGroup(Map<String, Object> ns, String... ids) {

        HashMap<String, RadioButton> ret = new HashMap();
        for (String id : ids) {
            RadioButton rb = (RadioButton) ns.get(id);
            ret.put(rb.getButtonData().toString(), rb);
        }

        return ret;
    }

    public static void setRadioButtonGroup(ButtonGroup bg, HashMap<String, RadioButton> rbMap, HashMap<String, String> config, String var) {

        RadioButton rb = rbMap.get(MapUtil.getValueOr(config, var, ""));
        if (rb != null) {
            bg.setSelection(rb);
        }
    }

    public static void setSelectionList(ListButton lb, HashMap<String, String> config, String var) { //, String defVal) {
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

    public static ButtonPressListener createGenericDirBPListerner(final TextInput input) {
        return createGenericDirBPListerner(input, new File(MapUtil.getValueOr(PlotUtil.getConfig(PlotUtil.GLOBAL_CONFIG), "WorkDir", "")), FileBrowserSheet.Mode.SAVE_TO);
    }

    public static ButtonPressListener createGenericDirBPListerner(final TextInput input, final File workDir, final FileBrowserSheet.Mode mode) {
        return new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;
                if (!new File(input.getText()).exists()) {
                    if (workDir != null && workDir.exists()) {
                        browse = new FileBrowserSheet(mode, workDir.getAbsolutePath());
                    } else {
                        browse = new FileBrowserSheet(mode, new File("").getAbsolutePath());
                    }
                    
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(input.getText()).getAbsoluteFile().getParentFile().getPath());
                }
                browse.open(button.getWindow(), new SheetCloseListener() {
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

    public static TextInputContentListener createGenericTitleTBListerner(final TextInput corInput) {
        return new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput ti) {
                corInput.setText(ti.getText());
            }
        };
    }
    
    public static String getSelectedVar(ListButton lb) {
        String selected = (String) lb.getSelectedItem();
        if (selected == null) {
            return "";
        } else if (lb.getName().equalsIgnoreCase("plotGroup2LB") && selected.startsWith("No")) {
            return "No";
        } else {
            return selected.substring(selected.lastIndexOf("(") + 1).replaceAll("\\)", "");
        }

    }

    public static void setNonRepeatedSelection(final ListButton lb1, final ListButton lb2) {
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
                    if (o == null && lb2.getName().equalsIgnoreCase("plotGroup2LB")) {
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
