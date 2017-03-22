package org.agmip.ui.plotui.gui;

import java.io.File;
import java.net.URL;
import org.agmip.ui.plotui.PlotUtil;
import static org.agmip.ui.plotui.PlotUtil.getConfig;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.LoggerFactory;

/**
 * The dialog activated from menu bar to change the path for R environment.
 *
 * @author Meng Zhang
 */
public class REnvConfigDialog extends Dialog implements Bindable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(REnvConfigDialog.class);
    
    private TextInput start_rExePath = null;
    private PushButton start_browseRExePath = null;
    private TextInput start_rLibPath = null;
    private PushButton start_browseRLibPath = null;
    
    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        start_rExePath = (TextInput) ns.get("start_rExePath");
        start_browseRExePath = (PushButton) ns.get("start_browseRExePath");
        start_rLibPath = (TextInput) ns.get("start_rLibPath");
        start_browseRLibPath = (PushButton) ns.get("start_browseRLibPath");
        
        start_rExePath.setText(MapUtil.getValueOr(getConfig(PlotUtil.GLOBAL_CONFIG), "RExePath", ""));
        start_rLibPath.setText(MapUtil.getValueOr(getConfig(PlotUtil.GLOBAL_CONFIG), "RLibPath", ""));
        
        start_browseRExePath.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (!new File(start_rExePath.getText()).exists()) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File("").getAbsolutePath());
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File(start_rExePath.getText()).getAbsoluteFile().getParentFile().getPath());
                }
                browse.setDisabledFileFilter(new Filter<File>() {

                    @Override
                    public boolean include(File file) {
                        return (file.isFile() && (!file.getName().equalsIgnoreCase("Rscript.exe")));
                    }
                });
                browse.open(button.getWindow(), new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            start_rExePath.setText(dir.getPath());
                            getConfig(PlotUtil.GLOBAL_CONFIG).put("RExePath", dir.getPath());
                        }
                    }
                });
            }
        });
        
        start_browseRLibPath.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;

                if (!new File(start_rLibPath.getText()).exists()) {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File("").getAbsolutePath());
                } else {
                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, new File(start_rLibPath.getText()).getAbsoluteFile().getParentFile().getPath());
                }
                browse.open(button.getWindow(), new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File dir = browse.getSelectedFile();
                            start_rLibPath.setText(dir.getPath());
                            getConfig(PlotUtil.GLOBAL_CONFIG).put("RLibPath", dir.getPath());
                        }
                    }
                });
            }
        });
    }
}
