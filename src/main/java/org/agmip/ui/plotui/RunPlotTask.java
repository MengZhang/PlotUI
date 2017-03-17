package org.agmip.ui.plotui;

import java.io.IOException;
import org.agmip.common.Functions;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class RunPlotTask extends Task<Integer> {

    private final PlotUtil.RScps rScpType;
    private final static Logger LOG = LoggerFactory.getLogger(RunPlotTask.class);

    public RunPlotTask(PlotUtil.RScps rScpType) {
        this.rScpType = rScpType;
    }

    @Override
    public Integer execute() throws TaskExecutionException {
        try {
            if (rScpType.equals(PlotUtil.RScps.StandardPlot)) {
                return PlotRunner.runStandardPlot();
            } else if (rScpType.equals(PlotUtil.RScps.CorrelationPlot)) {
                return PlotRunner.runCorrelationPlot();
            } else if (rScpType.equals(PlotUtil.RScps.CTWNPlot)) {
                return PlotRunner.runCTWNPlot();
            } else if (rScpType.equals(PlotUtil.RScps.HistoricalPlot)) {
                return PlotRunner.runHistoricalPlot();
            } else if (rScpType.equals(PlotUtil.RScps.ClimAnomaly)) {
                return PlotRunner.runClimAnomaly();
            } else {
                LOG.error("Invalid value for R script type");
                return -99;
            }
        } catch (IOException ex) {
            LOG.error(Functions.getStackTrace(ex));
            return -99;
        }
    }

}
