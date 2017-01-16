package org.agmip.ui.plotui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
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
    
    public static void runStandardPlot() throws IOException {
        
        HashMap<String, String> configSTPlot = PlotUtil.CONFIG_MAP.get(PlotUtil.RScps.StandardPlot.toString());
        
        String rScript = Paths.get(PlotUtil.R_SCP_PATH, RScps.StandardPlot.getScpName()).toString();
        String title = configSTPlot.get("title");
        String plotType = configSTPlot.get("plotType");
        String plotFormat = configSTPlot.get("plotFormat");
        String plotVar = configSTPlot.get("plotVar");
        String inputFile = configSTPlot.get("inputFile"); //"D:\\SSD_USER\\Documents\\NetBeansProjects\\Develop\\PlotUI\\target\\test-classes\\r_dev\\input_avg.txt";
        String outputPath = configSTPlot.get("outputPath"); //"plot_output";
        String outputACMO = configSTPlot.get("outputACMO"); //"acmo_output.csv";
        String outputGraph = configSTPlot.get("outputGraph"); //"output";

        Functions.revisePath(outputPath);
        ProcessBuilder pb = new ProcessBuilder(PlotUtil.R_PATH, rScript, PlotUtil.R_LIB_PATH, title, plotType, plotFormat, plotVar, inputFile, outputPath, outputACMO, outputGraph);
        LOG.debug(pb.command().toString());
        printRProc(pb.start(), RScps.StandardPlot);
    }

    public static void printRProc(Process p, RScps Rscp) {

        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                LOG.debug("{} : {}", Rscp.toString(), line);
            }
            while ((line = errReader.readLine()) != null) {
                LOG.info("{} : {}", Rscp.toString(), line);
            }
            reader.close();
            errReader.close();
        } catch (IOException e) {
            LOG.error("{} : {}", Rscp.toString(), "failed to read output from process");
            LOG.error(Functions.getStackTrace(e));
        }
    }
}
