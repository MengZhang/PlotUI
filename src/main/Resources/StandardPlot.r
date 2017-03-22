#################################################################################################
#
#  This script generates a boxplot/CDF for a simulated variables with GCMs and models
#
#  Author: Meng Zhang
#  Created: 01/12/2017
#################################################################################################

options(echo = F)

args <- commandArgs(trailingOnly = TRUE)
utilScpPath <- "r_lib/PlotUtil.r"
if (length(args) == 0) {
  # for debug purpose and sample for commend line arguments
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "Result",
      "CDF",
      "PNG",
      "HWAH_S",
      "ABSOLUTE",
      # "RELATIVE",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM3",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM1",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM3",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM4",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM2\\RCP4.5",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM5\\RCP4.5",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output_ui",
      "true",
      "CM2CM5",
      "GWXF:Middle_GIXF:Cool-Wet_GEXF:Cool-Dry_0XXX:Base_GMXF:Hot-Wet_GJXF:Hot-Dry_"
      # "GWXF:Middle:#333333_GIXF:Cool-Wet:green_GEXF:Cool-Dry:blue_0XXX:Base:#D3D3D3_GMXF:Hot-Wet:yellow_GJXF:Hot-Dry:red_"
    )
  utilScpPath <- "PlotUtil.r"
}
print(args)
source(utilScpPath)
setLibPath(args[1])
library(ggplot2)
library(plyr)
title <- args[2]
plotType <- args[3]
plotFormat <- tolower(args[4])
plotVarID <- args[5]
plotMethod <- args[6]
inputFolder <- args[7]
inputFolder2 <- args[8]
outputPath <- args[9]
outputCsvFlag <- args[10]
outputAcmo <-
  paste(paste(args[11], plotType, plotMethod, plotVarID, sep = "-"),
        "csv",
        sep = ".")
outputAcmo <- paste(outputPath, outputAcmo, sep = "/")
outputPlot <-
  paste(paste(args[11], plotType, plotMethod, plotVarID, sep = "-"),
        plotFormat,
        sep = ".")
gcmCatPairs <- strsplit(args[12], split = "_")[[1]]
duration <- 30

# Initialize GCM category setting
gcmCatEnv <- new.env()
gcmEnv <- new.env()
gcmColorEnv <- getDefColorEnv()
for (i in 1 : length(gcmCatPairs)) {
  tmp <- strsplit(gcmCatPairs[i], split = ":")[[1]]
  assign(tmp[1], tmp[2], envir=gcmCatEnv)
  assign(tmp[2], tmp[1], envir=gcmEnv)
  if (length(tmp) > 2 && tmp[3] != "") { # to support old format of CLIM_ID/GCM mapping (no color)
    assign(tmp[2], tmp[3], envir=gcmColorEnv)
  }
}
gcmCats <- getGCMListByOrder(gcmEnv)

# Load ACMOs and order the data by GCM
system1 <- readACMOAve(inputFolder, gcmCatEnv, duration, plotVarID)
system2 <- readACMOAve(inputFolder2, gcmCatEnv, duration, plotVarID)
if (plotMethod == "RELATIVE") {
  merged <- diffSystem(system1, system2, gcmCats)
  plotVarTitle <- paste("Relative Change of", name_unit2(plotVarID, "%"), sep = "\n")
  rangeFactors <- c(1.1, 1.1)
} else {
  merged <- combineSystem(system1, system2, gcmCats)
  plotVarTitle <- name_unit(plotVarID)
  rangeFactors <- c(0.9, 1.3)
}
gcmCats <- detectGCM(system1, system2, gcmCats)
qt <- getQuestionType(length(system1), length(system2), merged)
print(paste("Detect comparison mode as [", qt, "]", sep = ""))
colors <- getStdPlotColors(qt, gcmCats, gcmColorEnv)

# detect number of GCMs
# gcmNum <- length(gcmCatPairs)
gcmNum <- length(levels(as.factor(merged$GCM)))
print(paste("Detect", gcmNum, "combination of GCM+RAP+MAN", sep = " "))

if (plotType == "BoxPlot") {
  
  ggplot(data = merged, aes(x = MODEL, y = VALUE)) +
    geom_boxplot(
      aes(fill = GCM),
      outlier.colour = NA,
      width = gcmNum / 12,
      color = "black"
    )  +
    coord_cartesian(ylim = range(boxplot(merged$VALUE, plot = FALSE)$stats) * rangeFactors) +
    theme_bw() +
    theme(legend.text = element_text(size = 13),
          legend.title = element_text(size = 13)) +
    theme(axis.text = element_text(size = 13)) +
    theme(axis.title = element_text(size = 13, face = "bold")) +
    labs(x = "Models", y = plotVarTitle, colour = "Legend", title = title) +
    theme(panel.grid.minor = element_blank()) +
    theme(plot.margin = unit(c(1, 1, 1, 1), "mm")) +
    theme(axis.text.x = element_text(hjust = 0.5)) +
    theme(plot.title = element_text(size=20, face="bold", hjust = 0.5)) +
    scale_fill_manual(values=colors)
  
  ggsave(
    filename = outputPlot,
    plot = last_plot(),
    path = outputPath,
    device = plotFormat
  )
  
  if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
    write.csv(merged, outputAcmo)
  }
  
} else if (plotType == "CDF") {
  
  models <- levels(as.factor(merged$MODEL))
  gcms <- levels(as.factor(merged$GCM))
  mergedCDF <- NULL
  for (i in 1 : length(models)) {
    for (j in 1 : length(gcms)) {
      subData <- subset(merged, MODEL == models[i] & GCM == gcms[j])
      subData$ECDF <- ecdf(subData$VALUE)(subData$VALUE)
      if (is.null(mergedCDF)) {
        mergedCDF <- subData
      } else {
        mergedCDF <- rbind(mergedCDF, subData)
      }
    }
  }
  
  if (qt != "Cur") {
    
    # Multi GCM
    ggplot(mergedCDF, aes(VALUE, 1 - ECDF, color = GCM)) +
      geom_step() +
      facet_wrap(~MODEL, ncol = 1) +
      xlab(plotVarTitle) +
      ylab("Cumulative Frequency") +
      scale_fill_manual(values=colors) +
      labs(title=title) +
      theme(axis.title = element_text(size = 13, face = "bold")) +
      theme(plot.title = element_text(size = 20, face = "bold", hjust = 0.5))
    
  } else {
    
    # Single GCM
    colors <- c("red", "green")
    ggplot(mergedCDF, aes(VALUE, 1 - ECDF, color = MODEL)) +
      geom_step() +
      xlab(plotVarTitle) +
      ylab("Cumulative Frequency") +
      scale_fill_manual(values=colors) +
      labs(title=title) +
      theme(axis.title = element_text(size = 13, face = "bold")) +
      theme(plot.title = element_text(size = 20, face = "bold", hjust = 0.5))
    
  }
  
  ggsave(
    filename = outputPlot,
    plot = last_plot(),
    path = outputPath,
    device = plotFormat
  )
  
  if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
    write.csv(mergedCDF, outputAcmo)
  }
}

#################################################################
