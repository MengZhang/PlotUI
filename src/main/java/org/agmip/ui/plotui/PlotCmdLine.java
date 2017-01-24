package org.agmip.ui.plotui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import org.apache.pivot.util.concurrent.TaskExecutionException;
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
    private final HashMap<String, String> globalConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.GLOBAL_CONFIG);
    private final HashMap<String, String> stdConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.StandardPlot.toString());

    public PlotCmdLine(String[] args) {
        this.cmds = PlotUtil.readCommand(args);
    }

    public void run() throws IOException, TaskExecutionException, PlotUtil.ForceStopException {

        String plotVar = stdConfig.get("plotVar");
        ValidationTask task = new ValidationTask(PlotUtil.getAllInputFiles(new File(stdConfig.get("inputDir"))), plotVar);
        LinkedHashMap<File, ArrayList<HashMap<String, String>>> result = task.execute();
        if (!result.isEmpty()) {

            // Generate report
            File report = PlotUtil.generateReport(result, stdConfig.get("outputPath"));

            // Ask for user confirmation
            PlotUtil.waitForUserConfirm(
                "Detect blank records for " + plotVar,
                "Please check report file for detail [" + report.getPath() + "]");
            
        }
        runPlot();

    }

    private void runPlot() throws IOException {
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
