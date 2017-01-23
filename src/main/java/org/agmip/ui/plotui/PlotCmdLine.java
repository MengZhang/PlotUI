package org.agmip.ui.plotui;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line UI for AgMIP R plotting tools
 *
 * @author Meng Zhang
 */
public class PlotCmdLine {
    
    private final static Logger LOG = LoggerFactory.getLogger(PlotCmdLine.class);
    private final HashSet<PlotUtil.RScps> cmds;
    
    public PlotCmdLine(String[] args) {
        this.cmds = PlotUtil.readCommand(args);
    }
    
    public void run () throws IOException {
        
        for (PlotUtil.RScps rScpType : cmds) {
            if (rScpType.equals(PlotUtil.RScps.StandardPlot)) {
                PlotRunner.runStandardPlot();
            } else if (rScpType.equals(PlotUtil.RScps.CorrelationPlot)) {
                PlotRunner.runCorrelationPlot();
            } else if (rScpType.equals(PlotUtil.RScps.ClimAnomaly)) {
                PlotRunner.runClimAnomaly();
            }
        }
    }
}
