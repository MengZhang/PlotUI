package org.agmip.ui.plotui.gui;

import java.util.HashMap;
import org.agmip.ui.plotui.PlotUtil;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.wtk.BoxPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public abstract class PlotTabBoxPane extends BoxPane implements Bindable {
    
    private static final Logger LOG = LoggerFactory.getLogger(PlotTabBoxPane.class);
    private static HashMap<String, String> globalConfig = PlotUtil.getConfig(PlotUtil.GLOBAL_CONFIG);
    private HashMap<String, String> config;
    private final String configKey;
    public abstract void loadConfig();
    public abstract void saveConfig();
    
    public PlotTabBoxPane(String configKey) {
        this.configKey = configKey;
        this.config = PlotUtil.getConfig(configKey);
    }
    
    public void updateConfig() {
        this.config = PlotUtil.getConfig(this.configKey);
    }
    
    public HashMap<String, String> getConfig() {
        return this.config;
    }
    
    protected String getGlobalConfigVar(String key) {
        return globalConfig.get(key);
    }
    
    protected String getGlobalConfigVar(String key, String defVal) {
        return MapUtil.getValueOr(globalConfig, key, defVal);
    }
    
    
}
