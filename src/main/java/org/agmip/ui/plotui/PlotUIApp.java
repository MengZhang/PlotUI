package org.agmip.ui.plotui;

//import org.apache.pivot.beans.BXMLSerializer;

import java.io.IOException;
import java.util.logging.Level;
import org.agmip.common.Functions;

//import org.apache.pivot.collections.Map;
//import org.apache.pivot.wtk.Application;
//import org.apache.pivot.wtk.DesktopApplicationContext;
//import org.apache.pivot.wtk.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotUIApp { //extends Application.Adapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(PlotUIApp.class);
//    private PlotUIWindow window = null;

//    @Override
//    public void startup(Display display, Map<String, String> props) throws Exception {
//        BXMLSerializer bxml = new BXMLSerializer();
//        window = (QuadUIWindow) bxml.readObject(getClass().getResource("/quadui.bxml"));
//        window.open(display);
//    }
//
//    @Override
//    public boolean shutdown(boolean opt) {
//        if (window != null) {
//            window.close();
//        }
//        return false;
//    }

    public static void main(String[] args) {
        
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
            
            if (cmdFlg) {
                PlotCmdLine cmd = new PlotCmdLine(args);
                try {
                    cmd.run();
                } catch (IOException ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }
                
            } else {
//            DesktopApplicationContext.main(PlotUIApp.class, args);
            }
        } catch (PlotUtil.ForceStopException ex) {
            LOG.info("Process stopped.");
        }
    }
}
