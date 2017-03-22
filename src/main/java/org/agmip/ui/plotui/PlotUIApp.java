package org.agmip.ui.plotui;

import org.agmip.ui.plotui.gui.PlotUIWindow;
import org.apache.pivot.beans.BXMLSerializer;

import java.io.IOException;
import org.agmip.common.Functions;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotUIApp extends Application.Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(PlotUIApp.class);
    private PlotUIWindow window = null;
    private static String version = "";

    @Override
    public void startup(Display display, Map<String, String> props) throws Exception {
        BXMLSerializer bxml = new BXMLSerializer();
        window = (PlotUIWindow) bxml.readObject(getClass().getResource("/uiscript/plotui.bxml"));
        window.setPlotUIVersion(version);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean opt) {
        if (window != null) {
            window.close();
        }
        return false;
    }

    public static void main(String[] args) throws TaskExecutionException {

        try {
            boolean cmdFlg = true;
            boolean initFlg = false;
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-config")) {
                    cmdFlg = false;
                } else if (arg.equalsIgnoreCase("-init")) {
                    initFlg = true;
                }
            }

            PlotUtil.initialize(initFlg);
            version = PlotUtil.getVersion();
            LOG.info("PlotUI {} lauched with JAVA {} under OS {}", version, System.getProperty("java.runtime.version"), System.getProperty("os.name"));

            if (cmdFlg) {
                PlotCmdLine cmd = new PlotCmdLine(args);
                try {
                    cmd.run();
                } catch (IOException ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }

            } else {
                DesktopApplicationContext.main(PlotUIApp.class, new String[]{});
            }
        } catch (PlotUtil.ForceStopException ex) {
            LOG.info("Process stopped.");
        }
    }
}
