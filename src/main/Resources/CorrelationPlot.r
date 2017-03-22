#################################################################################################
#     faceit -> AgMIP
#  This script generates a correlation scatterplot of simulated variables with climatic variables
#
#  Author: Wei Xiong (Original), Meng Zhang (Enhancement)
#  Created: 09/29/2014
#  Updated: 01/16/2017 (Transfer to desktop plot tools)
#################################################################################################

options(echo = F)

args <- commandArgs(trailingOnly = TRUE)
utilScpPath <- "r_lib/PlotUtil.r"
def_lib_path <- "~\\R\\win-library\\3.3"
if (length(args) == 0) {
  # for R debug purpose
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "Result",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\CM2",
      "PNG",
      "HWAH_S",
      "CWAH_S",
      "CLIM_ID",
      "No",
      "..\\..\\test\\resources\\r_dev\\plot_output",
      "true",
      "CorPlot"
    )
  utilScpPath <- "PlotUtil.r"
}

source(utilScpPath)
setLibPath(args[1])
library(ggplot2)
library(lattice)
library(MASS)

title <- args[2]
inputFolder <- args[3]
plotFormat <- tolower(args[4])
varNameX <- args[5]
varNameY <- args[6]
group1 <- args[7]
group2 <- args[8]
outputPath <- args[9]
outputCsvFlag <- args[10]
outputName <- paste(args[11], varNameX, varNameY, group1, group2, sep = "-")
outputPlot <- paste(outputName, plotFormat, sep = ".")
output <- paste(outputPath, outputPlot, sep = "/")
outputAcmo <-
  paste(paste(args[11], varNameX, varNameY, group1, group2, sep = "-"),
        "csv",
        sep = ".")
outputAcmo <- paste(outputPath, outputAcmo, sep = "/")

Group <- "No"
if (group1 != "No" && group2 != "No") {
  if (group1 == group2) {
    Group <- group1
  } else {
    Group <- paste(group1, group2, sep = "+")
  }
} else if (group1 == "No" || group2 == "No") {
  if (group1 != "No") {
    Group <- group1
  } else {
    Group <- group2
  }
} else{
  Group <- "No"
}

# OriData <- read.csv(acmocsv, skip = 2, header = T)
acmoinputs <- list.files(path = inputFolder, pattern = "ACMO.*\\.csv", recursive = T)
acmoinputs <- as.character(acmoinputs)

for (i in 1:length(acmoinputs)) {
  print(acmoinputs[i])
  OriData <-
    read.csv(paste(inputFolder, acmoinputs[i], sep = "/"),
             skip = 2,
             header = T)
  if (group2 == "No") {
    OriData <-
      OriData[, c(varNameX, varNameY, group1)]
  } else {
    OriData <-
      OriData[, c(varNameX, varNameY, group1, group2)]
  }
  
  if (i == 1) {
    merged <- OriData
  } else {
    merged <- rbind(merged, OriData)
  }
  
}

if (plotFormat == "png") {
  png(output)
} else if (plotFormat == "pdf") {
  pdf(output)
}

if (Group != "No") {
  form <- as.formula(paste(varNameY, "~", varNameX, "|", Group))
} else {
  form <- as.formula(paste(varNameY, "~", varNameX))
}
xyplot(
  form,
  merged,
  panel = function(x, y, ...) {
    #panel.abline(h=seq(0,8000,2000),col="gray")
    #panel.abline(v=seq(0,1500,500),col="gray")
    panel.xyplot(x,
                 y,
                 type = "p",
                 col = "red",
                 pch = 20,
                 ...)
    panel.abline(fit <- lm(y ~ x), col = "blue")
    panel.text(600, 7000, paste("R2=", format(summary(fit)$adj.r.squared, digits =
                                                3)))
  },
  scales = list(cex = 1.2),
  xlab = list(name_unit(varNameX), cex = 1.4),
  ylab = list(name_unit(varNameY), ces = 1.4),
  main = title
)

graphics.off()

if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
  write.csv(merged, outputAcmo)
}
