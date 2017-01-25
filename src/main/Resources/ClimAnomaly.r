#######################################################################################
#       faceit -> AgMIP
#  This script retrieves the csv filename that contains the information
#  of country, site, scenarios, etc.
#
#   Author: Wei Xiong (Original), Meng Zhang (Enhancement)
#   Created: 09/23/2014
#   Imported: 01/16/2017 (Transfer to desktop plot tools)
#   usage:  faceit_getfilename(fileDir,ClimVar,SiteName)
#   fileDir: the directory of the csv files
#   ClimVar: Variables for generation
#   SiteName: SiteName for showing as the plot title
#######################################################################################

options(echo = T)
source("PlotUtil.r")

args <- commandArgs(trailingOnly = TRUE)
def_lib_path <- "~\\R\\win-library\\3.3"
if (length(args) == 0) {
  # for R debug purpose
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "..\\..\\test\\resources\\r_dev\\wth_data",
      "TMAX",
      "relative",
      "PNG",
      "..\\..\\test\\resources\\r_dev\\plot_output",
      "ClimPlot"
    )
}
setLibPath(args[1])

fileDir <- args[2]
ClimVar <- args[3]
plotType <- args[4]
plotFormat <- tolower(args[5])
outputFileName <- paste(args[7], toupper(plotType), ClimVar, sep = "-")
outputPlot <- paste(outputFileName, plotFormat, sep = ".")
output <- paste(args[6], outputPlot, sep = "/")

faceit_ClimAnomaly(fileDir, ClimVar, plotType, plotFormat, output)
