package org.agmip.ui.plotui;

import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line UI for AgMIP R plotting tools
 *
 * @author Meng Zhang
 */
public class PlotCmdLine {
    
    private final static Logger LOG = LoggerFactory.getLogger(PlotCmdLine.class);
    private final HashMap<String, String> cmds;
    
    public PlotCmdLine(String[] args) {
        this.cmds = PlotUtil.readCommand(args);
    }
    
    public void run () throws IOException {
        
        String rScpType = cmds.get("rScpType");
        if (rScpType == null) {
            LOG.warn("Require R plot type in command line arguments!");
        } else if (rScpType.equals(PlotUtil.RScps.StandardPlot.toString())) {
            PlotRunner.runStandardPlot();
        }
    }
}
