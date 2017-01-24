package org.agmip.ui.plotui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.agmip.common.Functions;
import org.agmip.ui.plotui.PlotUtil.RScps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods provided here for running each R script
 *
 * @author Meng Zhang
 */
public class PlotRunner {

    private final static Logger LOG = LoggerFactory.getLogger(PlotRunner.class);

    public static int runStandardPlot() throws IOException {

        HashMap<String, String> config = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.StandardPlot.toString());
        HashMap<String, String> globalConfig = PlotUtil.CONFIG_MAP.get(PlotUtil.GLOBAL_CONFIG);

        String title = config.get("title");
        String plotType = config.get("plotType");
        String plotFormat = config.get("plotFormat");
        String plotVar = config.get("plotVar");
        String inputDir = config.get("inputDir");
        String outputPath = config.get("outputPath");
        String outputACMO = config.get("outputACMO");
        String outputGraph = config.get("outputGraph");
        String gcmMapping = globalConfig.get("GcmMapping");

        Functions.revisePath(outputPath);
        ProcessBuilder pb = new ProcessBuilder(
                PlotUtil.getRExePath(),
                PlotUtil.getRScpPath(RScps.StandardPlot),
                PlotUtil.getRLibPath(),
                title, plotType, plotFormat, plotVar, inputDir, outputPath, outputACMO, outputGraph, gcmMapping.replaceAll("\\|", "_"));
        LOG.debug(pb.command().toString());
        return printRProc(pb.start(), RScps.StandardPlot);
    }

    public static int runCorrelationPlot() throws IOException {

        HashMap<String, String> config = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.CorrelationPlot.toString());

        String plotFormat = config.get("plotFormat");
        String plotVarX = config.get("plotVarX");
        String plotVarY = config.get("plotVarY");
        String group1 = config.get("group1");
        String group2 = config.get("group2");
        String inputFile = config.get("inputFile");
        String outputPath = config.get("outputPath");
        String outputGraph = config.get("outputGraph");

        Functions.revisePath(outputPath);
        ProcessBuilder pb = new ProcessBuilder(
                PlotUtil.getRExePath(),
                PlotUtil.getRScpPath(RScps.CorrelationPlot),
                PlotUtil.getRLibPath(),
                inputFile, plotFormat, plotVarX, plotVarY, group1, group2, outputPath, outputGraph);
        LOG.debug(pb.command().toString());
        return printRProc(pb.start(), RScps.CorrelationPlot);
    }

    public static int runClimAnomaly() throws IOException {

        HashMap<String, String> config = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.ClimAnomaly.toString());

        String plotType = config.get("plotType");
        String plotFormat = config.get("plotFormat");
        String plotVar = config.get("plotVar");
        String inputDir = config.get("inputDir");
        String outputPath = config.get("outputPath");
        String outputGraph = config.get("outputGraph");

        Functions.revisePath(outputPath);
        ProcessBuilder pb = new ProcessBuilder(
                PlotUtil.getRExePath(),
                PlotUtil.getRScpPath(RScps.ClimAnomaly),
                PlotUtil.getRLibPath(),
                inputDir, plotVar, plotType, plotFormat, outputPath, outputGraph);
        LOG.debug(pb.command().toString());
        return printRProc(pb.start(), RScps.ClimAnomaly);
    }

    public static int runGcmDetect(String plotVar, String inputDir, String output) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(
                PlotUtil.getRExePath(),
                PlotUtil.getRScpPath(RScps.VarDetect),
                PlotUtil.getRLibPath(),
                plotVar, inputDir, output);
        LOG.debug(pb.command().toString());
        return printRProc(pb.start(), RScps.VarDetect);
    }

    public static int printRProc(Process p, RScps Rscp) {

        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        try {
            String line;
            boolean isErrExist = false;
            while ((line = reader.readLine()) != null) {
                LOG.debug("{} : {}", Rscp.toString(), line);
            }
            while ((line = errReader.readLine()) != null) {
                isErrExist = true;
                LOG.info("{} : {}", Rscp.toString(), line);
            }
            if (!isErrExist) {
                LOG.info("{} : Job done!", Rscp.toString());
            }
            reader.close();
            errReader.close();
        } catch (IOException e) {
            LOG.error("{} : {}", Rscp.toString(), "failed to read output from process");
            LOG.error(Functions.getStackTrace(e));
        }
        return p.exitValue();
    }
}
