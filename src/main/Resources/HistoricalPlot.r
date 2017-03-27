#################################################################################################
#
#  This script generates a boxplot/CDF for a comparison between historical/observed data and simulated result
#
#  Author: Meng Zhang
#  Created: 03/12/2017
#################################################################################################

options(echo = F)

args <- commandArgs(trailingOnly = TRUE)
utilScpPath <- "r_lib/PlotUtil.r"
if (length(args) == 0) {
  # for debug purpose and sample for commend line arguments
  args <-
    c(
      "~\\R\\win-library\\3.3", #1
      "CM0 historical", #2
      # "BoxPlot", #3
      # "CDF", #3
      "ScatterPlot", #3
      "PNG", #4
      # "PDF", #4
      "HWAH_S", #5
      # "CWAH_S", #5
      # "HADAT_S", #5
      # "ABSOLUTE", #6
      "RELATIVE", #6
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM0", #7
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output", #8
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output_ui", #8
      "true", #9
      "CM0" #10
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
outputPath <- args[8]
outputCsvFlag <- args[9]
outputAcmo <-
  paste(paste(args[10], plotType, plotMethod, plotVarID, sep = "-"),
        "csv",
        sep = ".")
outputAcmo <- paste(outputPath, outputAcmo, sep = "/")
outputPlot <-
  paste(paste(args[10], plotType, plotMethod, plotVarID, sep = "-"),
        plotFormat,
        sep = ".")

merged <- readACMOCM0(inputFolder, plotVarID, getHistVarID(plotVarID))

if (plotMethod == "RELATIVE") {
  merged <- diffSystemCM0(merged)
  plotVarTitle <- paste("Relative Change of", name_unit2(plotVarID, "%"), sep = "\n")
  groups <- levels(as.factor(merged$GROUP))
  colors <- getAutoColors(length(groups) - 2, c("red", "blue"))
  rangeFactors <- c(0.9, 1.1)
} else {
  plotVarTitle <- name_unit(getHistVarID(plotVarID))
  groups <- levels(as.factor(merged$GROUP))
  colors <- getAutoColors(length(groups) - 2, c("red", "blue", "#333333"))
  rangeFactors <- c(0.9, 1.3)
}

# merged <- subset(merged, !is.null(VALUE) & VALUE != "" & VALUE != "-99")
groupNum <- length(groups)

if (plotType == "BoxPlot") {
  
  ggplot(data = merged, aes(x = GROUP, y = VALUE)) +
    geom_boxplot(
      aes(fill = GROUP),
      outlier.colour = NA,
      width = groupNum / 12,
      color = "black"
    ) +
    coord_cartesian(ylim = adjustRange(range(boxplot(merged$VALUE, plot = FALSE)$stats), rangeFactors)) +
    theme_bw() +
    theme(legend.text = element_text(size = 13),
          legend.title = element_text(size = 13)) +
    theme(axis.text = element_text(size = 13)) +
    theme(axis.title = element_text(size = 13, face = "bold")) +
    labs(x = "Groups", y = plotVarTitle, colour = "Legend", title = title) +
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
  
  models <- levels(as.factor(merged$GROUP))
  mergedCDF <- NULL
  for (i in 1 : length(models)) {
    subData <- subset(merged, GROUP == models[i])
    subData$ECDF <- ecdf(subData$VALUE)(subData$VALUE)
    if (is.null(mergedCDF)) {
      mergedCDF <- subData
    } else {
      mergedCDF <- rbind(mergedCDF, subData)
    }
  }
  
  ggplot(mergedCDF, aes(VALUE, 1 - ECDF, color = GROUP)) +
    geom_step() +
    xlab(plotVarTitle) +
    ylab("Cumulative Frequency") +
    scale_color_manual(values=colors) +
    labs(title=title) +
    theme(axis.title = element_text(size = 13, face = "bold")) +
    theme(plot.title = element_text(size = 20, face = "bold", hjust = 0.5))
  
  ggsave(
    filename = outputPlot,
    plot = last_plot(),
    path = outputPath,
    device = plotFormat
  )
  
  if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
    write.csv(mergedCDF, outputAcmo)
  }
  
} else if (plotType == "ScatterPlot") {
  
  if (plotMethod == "RELATIVE") {
    
    # Currently limited to 2 model comparison.
    if (groupNum != 2) {
      
      warning("Current Scatter Plot only support for 2 models, please contact IT team to update the script")
      
    } else {
      
      mergedScatter <- subset(merged, GROUP == groups[1])
      mergedScatter$VALUE2 <- subset(merged, GROUP == groups[2])$VALUE
      mergedScatter <- mergedScatter[,c("EXNAME", "VALUE", "VALUE2")]
      colnames(mergedScatter) <- c("EXNAME", "VALUE_X", "VALUE_Y")
      ggplot(data = mergedScatter, aes(x = VALUE_X, y = VALUE_Y)) +
        geom_point() + 
        geom_smooth(method=lm, se=FALSE, fullrange=TRUE) +
        
        coord_cartesian(xlim = adjustRange(range(boxplot(merged$VALUE, plot = FALSE)$stats), rangeFactors),
                        ylim = adjustRange(range(boxplot(merged$VALUE, plot = FALSE)$stats), rangeFactors)) +
        theme_bw() +
        theme(legend.text = element_text(size = 13),
              legend.title = element_text(size = 13)) +
        theme(axis.text = element_text(size = 13)) +
        theme(axis.title = element_text(size = 13, face = "bold")) +
        labs(x = paste(models[1], plotVarTitle), y = paste(models[2], plotVarTitle), title = title) +
        theme(panel.grid.minor = element_blank()) +
        theme(plot.margin = unit(c(1, 1, 1, 1), "mm")) +
        theme(axis.text.x = element_text(hjust = 0.5)) +
        theme(plot.title = element_text(size=20, face="bold", hjust = 0.5))
    }
    
  } else {
    
    histData <- subset(merged, GROUP == "Observed")[, c("EXNAME", "VALUE")]
    modelData <- subset(merged, GROUP != "Observed")
    models <- levels(as.factor(modelData$GROUP))
    mergedScatter <- NULL
    
    for ( i in 1 : length(models)) {
      data <- subset(modelData, GROUP == models[i])
      tmp <- histData
      tmp$MODEL <- models[i]
      tmp$VALUE_S <- data$VALUE
      
      if (is.null(mergedScatter)) {
        mergedScatter <- tmp
      } else {
        mergedScatter <- rbind(mergedScatter, tmp)
      }
    }
    
    ggplot(data = mergedScatter, aes(x = VALUE, y = VALUE_S, color=MODEL, shape=MODEL)) +
      # facet_wrap(~MODEL) +
      geom_point() + 
      # geom_text(EXNAME) +
      geom_smooth(method=lm, se=FALSE, fullrange=TRUE) +
      coord_cartesian(xlim = adjustRange(range(boxplot(merged$VALUE, plot = FALSE)$stats), rangeFactors),
                      ylim = adjustRange(range(boxplot(merged$VALUE, plot = FALSE)$stats), rangeFactors)) +
      theme_bw() +
      theme(legend.text = element_text(size = 13),
            legend.title = element_text(size = 13)) +
      theme(axis.text = element_text(size = 13)) +
      theme(axis.title = element_text(size = 13, face = "bold")) +
      labs(x = plotVarTitle, y = gsub("Simulated", "Observed", plotVarTitle), title = title) +
      theme(panel.grid.minor = element_blank()) +
      theme(plot.margin = unit(c(1, 1, 1, 1), "mm")) +
      theme(axis.text.x = element_text(hjust = 0.5)) +
      theme(plot.title = element_text(size=20, face="bold", hjust = 0.5)) +
      scale_color_manual(values=colors)
  }
  
  
  ggsave(
    filename = outputPlot,
    plot = last_plot(),
    path = outputPath,
    device = plotFormat
  )
  
  if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
    write.csv(mergedScatter, outputAcmo)
  }
}
