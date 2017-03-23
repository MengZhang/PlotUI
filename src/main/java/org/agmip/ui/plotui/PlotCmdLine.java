package org.agmip.ui.plotui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import static org.agmip.ui.plotui.PlotUtil.getConfig;
import static org.agmip.ui.plotui.PlotUtil.getPlotOutputFile;
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
//    private final HashMap<String, String> globalConfig = getConfig(PlotUtil.GLOBAL_CONFIG);
    private final HashMap<String, String> stdConfig = getConfig(PlotUtil.RScps.StandardPlot.toString());
    private final HashMap<String, String> corConfig = getConfig(PlotUtil.RScps.CorrelationPlot.toString());
    private final HashMap<String, String> ctwnConfig = getConfig(PlotUtil.RScps.CTWNPlot.toString());

    public PlotCmdLine(String[] args) {
        this.cmds = PlotUtil.readCommand(args);
    }

    public void run() throws IOException, TaskExecutionException, PlotUtil.ForceStopException {

        for (PlotUtil.RScps rScpType : cmds) {
            validateInput(rScpType);
            LOG.info("Start R script for {}", rScpType.toString());
            if (rScpType.equals(PlotUtil.RScps.StandardPlot)) {
                PlotRunner.runStandardPlot();
            } else if (rScpType.equals(PlotUtil.RScps.CorrelationPlot)) {
                PlotRunner.runCorrelationPlot();
            } else if (rScpType.equals(PlotUtil.RScps.CTWNPlot)) {
                PlotRunner.runCTWNPlot();
            } else if (rScpType.equals(PlotUtil.RScps.HistoricalPlot)) {
                PlotRunner.runHistoricalPlot();
            } else if (rScpType.equals(PlotUtil.RScps.ClimAnomaly)) {
                PlotRunner.runClimAnomaly();
            } else {
                LOG.warn("Unsupported plot type : {}", rScpType.toString());
            }
            LOG.info("Start R script for {} done!", rScpType.toString());
            File plotFile = getPlotOutputFile(rScpType);
            if (plotFile.exists()) {
                LOG.info("Generate {}", plotFile);
            }
        }

    }

    private void validateInput(PlotUtil.RScps rScpType) throws IOException, PlotUtil.ForceStopException, TaskExecutionException {

        LOG.info("Start validation for {}", rScpType.toString());
        ArrayList<String> plotVars = PlotUtil.getValidateVars(rScpType);
        String inputDir = PlotUtil.CONFIG_MAP.get(rScpType.toString()).get("inputDir");
        String outputDir = PlotUtil.CONFIG_MAP.get(rScpType.toString()).get("outputPath");
        if (inputDir == null || inputDir.equals("")) {
            LOG.warn("Invalid input path for validation!");
        } else if (outputDir == null || outputDir.equals("")) {
            LOG.warn("Invalid output path for validation!");
        } else if (plotVars.isEmpty()) {
            LOG.warn("Invalid plot variables for validation!");
        } else {
            ValidationTask task = new ValidationTask(PlotUtil.getAllInputFiles(new File(inputDir)), plotVars.toArray(new String[]{}));
            LinkedHashMap<File, ArrayList<HashMap<String, String>>> result = task.execute();
            if (!result.isEmpty()) {

                // Generate report
                File report = PlotUtil.generateReport(result, rScpType);

                // Ask for user confirmation
                PlotUtil.waitForUserConfirm(
                        "Detect blank records for plot variables",
                        "Please check report file for detail [" + report.getPath() + "]");
                
            } else {
                LOG.info("Start validation for {} done!", rScpType.toString());
            }
        }
    }
}
