####################################################################################################
###----------------------------------------------------------------------------------------------###
#                     \    ||   /
#      AA             \    ||   /  MMM    MMM  IIII  PPPPP
#     AAAA            \\   ||   /   MMM  MMM    II   P  PPP
#    AA  AA    ggggg  \\\\ ||  //   M  MM  M    II   PPPPP
#   AAAAAAAA  gg  gg     \\ ////    M      M    II   P
#  AA      AA  ggggg  \\   //      MM      MM  IIII  PPP
#                  g  \\\\     //    
#              gggg      \\ ////     The Agricultural Model Intercomparison and Improvement Project
#                          //
###----------------------------------------------------------------------------------------------###
####################################################################################################
###----------------------------------------------------------------------------------------------###

### AgMIP Regional Integrated Assessments: CTWN Sensitivity Analysis ##

# This routine is structured to produce line plots and boxplots related to a linear factor analysis, intended to explore crop, crop model, and site-specific sensitivities to changes in carbon dioxide concentration, temperature, water/precipitation, and nitrogen applications. The routine can, and should, be used by all RRTs in their sensitivity investigations, particularly related to Phase 2 of the DFID-funded regional integrated assessments

# Created: May 19, 2015
# Last edited: March 09th, 2017
#   By Meng zhang, to improve API and be adopted with AgView tool (http://tools.agmip.org/agview.php).
#   By Meng zhang, add functionaity to support multi-models (more than DSSAT and APSIM) for section 1-3.
# Author: Sonali McDermid, sps246@nyu.edu
# Co-authors: Alex Ruane, Cheryl Porter, Ken Boote and Meng Zhang
# Routines are not gauranteed, and any help questions should be directed to the authors

# It is suggested that the User read carefully through this routine, and specifically note those lines that contain, or are preceeded by the word "CHANGE" in uppercase. These are areas that the User will need to change the inputs. The program is constructed to minimize the number of changes that the User makes, and can be modified by the User for her/his needs. These "CHANGE" statements can usually be found at the top of each section. 

# Also note that on some platforms, you may have difficulty creating either pdf or jpeg images. As such, we have included the commands to do both. Currently, the routine is set-up to print .pdfs. However, if you wish to switch to jpegs, please UNCOMMENT the preceding line. For example, to switch to jpeg, you would comment out the line starting with "pdf(. . .)", and uncomment the line starting with #jpeg('CO2 Sensitivity at N=30kg-ha.jpg', quality = 300). 

# The following is a key corresponding to the sensitivity tests
#CO2 @ 30
#1 360
#2 450
#3 540
#4 630
#5 720
#CO2 @180
#6 360
#7 450
#8 540
#9 630
#10 720
#Temp
#11 -2
#12 0
# 13 2
# 14 4
# 15 6
# 16 8
#Rainfall
# 17 25
# 18 50
# 19 75
# 20 100
# 21 125
# 22 150
# 23 175
# 24 200
#Fert
# 25 0
# 26 30
# 27 60
# 28 90
# 29 120
# 30 150
# 31 180
# 32 210

#----------------------Define Commom Functions---------------#

# To make this file stand alone, all the defined functions are moved from utilility script file to here.

# Setup output format
setCtwnOutput <- function(outputPlot, outputPath, plotFormat) {
  if (!dir.exists(outputPath)) {
    dir.create(outputPath)
  }
  
  output <- paste(outputPath, paste(gsub("/", "-", outputPlot), plotFormat, sep = "."), sep = "/")
  if (plotFormat == "png") {
    png(output, width = 850, height = 600, res = 96)
  } else if (plotFormat == "pdf") {
    pdf(output)
  } else if (plotFormat == "jpeg") {
    jpeg(output, quality = 300, width = 850, height = 600, res = 96)
  }
}

# read ACMO CSV file from given directory and return the data by climate ID
readACMO <- function(inputFolder, plotVarID) {
  acmoinputs <- list.files(path = inputFolder, pattern = "ACMO.*\\.csv")
  acmoinputs <- as.character(acmoinputs)
  ret <- NULL
  
  for (i in 1:length(acmoinputs)) {
    print(acmoinputs[i])
    OriData <-
      read.csv(paste(inputFolder, acmoinputs[i], sep = "/"),
               skip = 2,
               header = T)
    if (is.null(OriData$CO2D_S)) {
      OriData <- OriData[, c("CLIM_ID", "CROP_MODEL", "PDATE", "FEN_TOT", plotVarID)]
      colnames(OriData) <- c("CLIM_ID", "MODEL", "PDATE", "FEN_TOT", "VALUE")
    } else {
      OriData <- OriData[, c("CLIM_ID", "CROP_MODEL", "PDATE", "CO2D_S", "FEN_TOT", plotVarID)]
      colnames(OriData) <- c("CLIM_ID", "MODEL", "PDATE", "CO2", "FEN_TOT", "VALUE")
    }
    
    
    if (nrow(OriData) != 0) {
      
      # transfer date value to days after planting
      if (plotVarID %in% c("ADAT_S", "MDAT_S", "HADAT_S")) {
        pdate <- as.POSIXct(OriData$PDATE, format = "%Y-%m-%d")
        ndate <- as.POSIXct(OriData$VALUE, format = "%Y-%m-%d")
        dap <- as.numeric(ndate - pdate)
        OriData$VALUE <- dap
      }
      
      if (is.null(ret)) {
        ret <- OriData
      } else {
        ret <- rbind(ret, OriData)
      }
    }
    
  }
  return (ret)
}

# get list of valid value from ACMO by given variable 
getLevels <- function(OriData, var) {
  if (is.null(OriData[[var]])) {
    if (var == "CO2") {
      return (c(360,450,540,630,720))
    } else {
      print(paste("Can not find", var, "from ACMO files", sep = " "))
      return (c())
    }
  }
  return (levels(as.factor(OriData[[var]])))
}

# get list of valid value from ACMO for simulation years
getYears <- function(OriData) {
  return (as.numeric(levels(as.factor(substr(OriData$PDATE, 1, 4)))))
}

# Transform the input ACMO data to matrix with demension of year and the combination of model and climate ID
toMatrix <- function(OriData) {
  
  
  climates <- getLevels(OriData, "CLIM_ID")
  models <- getLevels(OriData, "MODEL")
  duration <- length(getYears(OriData))
  ret <- matrix(0, duration, length(climates) * length(models), dimnames = list(1:duration, as.vector(outer(models, climates, paste, sep = "_"))))
  
  for (i in 1 : length(models)) {
    
    for (j in 1 : length(climates)) {
      
      farmData <- subset(OriData, CLIM_ID == climates[j] & MODEL == models[i]) # & !is.na(VALUE) & VALUE != ""
      if (nrow(farmData) != 0) {
        
        farmData[is.na(farmData)] <- 0
        ctwnCat = paste(models[i], climates[j], sep = "_")
        ret[, ctwnCat] <- farmData$VALUE
        
      }
      
    }
    
  }
  return (ret)
}

# paste string in a opposite order
pasteOps <- function(str1, str2, sep = " ") {
  return (paste(str2, str1, sep = sep))
}

# draw and save plot
drawPlot <- function(title, ctwnCat, legend, model, years, outputPlotDir, plotFormat, plotVarID) {
  catNum <- length(ctwnCat)
  setCtwnOutput(title, outputPlotDir, plotFormat)
  plot(0,xlim=c(min(years), max(years)),ylim=c(min(model[,ctwnCat], na.rm=TRUE),max(model[,ctwnCat], na.rm=TRUE)+1000),type="n",xlab="Years",ylab="Yield (kg/ha)")
  matlines(years, model[,ctwnCat], type = "l", lty = 1:catNum, lwd = 1, pch = NULL,
           col = 1:catNum, xlab="Years", ylab="Yield")
  legend("topright", title=title, cex=0.75, pch=16, col=1:catNum, legend=legend, ncol=2)
  graphics.off()
}

drawBoxPlot <- function(title, ctwnCat, legend, model, years, outputPlotDir, plotFormat, plotVarID) {
  catNum <- length(ctwnCat)
  setCtwnOutput(paste("Boxplot", title), outputPlotDir, plotFormat)
  boxplot(model[,ctwnCat], ylab = "Yield (kg/ha)", xlab = "CO2 Level (ppm)", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14), names=c("360"," ","450"," ","540"," ","630"," ","720"," "), boxwex=0.5)
  # Plot means on top of boxplots
  col1 <- seq(1, by = 2, len = 5) 
  col2 <- seq(2, by = 2, len = 5)
  #points(c(1, 4, 7, 10, 13), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
  points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(model[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2) # Offsets for Alex
  points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(model[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
  legend("topright", title=paste("Boxplot", title), cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
  # dev.off()
  graphics.off()
}

#----------------------Start Routine-------------------------#

# Section 0 - Envirorment Initialization and read input arguments (added by Meng Zhang)
args <- commandArgs(trailingOnly = TRUE)
# utilScpPath <- "r_lib/PlotUtil.r"
if (length(args) == 0) {
  # for debug purpose and sample for commend line arguments
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "PDF",
      "HWAH_S",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\CTWN",
      "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output",
      # "..\\..\\test\\resources\\r_dev\\ACMO-p\\plot_output_ui",
      "true",
      "CTWNPLOT"
    )
  # utilScpPath <- "PlotUtil.r"
}
print(args)
# source(utilScpPath)
setLibPath(args[1])
plotFormat <- tolower(args[2])
plotVarID <- args[3]
inputFolder <- args[4]
outputPath <- args[5]
outputCsvFlag <- args[6]
outputPlotDir <- paste(outputPath, paste(args[7], plotVarID, sep = "-"), sep = "/")
outputAcmo <-
  paste(paste(args[6], plotVarID, sep = "-"),
        "csv",
        sep = ".")
outputAcmo <- paste(outputPlotDir, outputAcmo, sep = "/")


# Section 1 - Read in ACMO files and assign arrays #
## Modified by Meng Zhang, Auto detect some of the information from ACMO files #

# Read ACMO files from given directory
OriData <- readACMO(inputFolder, plotVarID)

# Definition for Simulation meta info
modname <- getLevels(OriData, "MODEL")       # modname <- c('APSIM','DSSAT') # CHANGE: LIST MODEL NAMES ALPHABETICALLY (this is the order the files will be read in)
nummod <- length(modname)
years <- getYears(OriData)                   # years <-c(1980:2009) # Assume 30 years (1980-2009) for all sensitivity tests
duration <- length(years)
ctwnCats <- getLevels(OriData, "CLIM_ID")
ctwnCatNum <- length(ctwnCats)

# Organize data into matrx
model <- toMatrix(OriData)
# Output CSV file for debug purpose
if (!is.null(outputCsvFlag) && outputCsvFlag != "") {
  write.csv(merged, outputAcmo)
}


# Section 2 #
#___________________________________________#
###### Lineplots of Linear Factor Analysis #######

# Section 2.1 #
#CO2 at 30 N________________________
# Definition for CO2
co2 <- getLevels(OriData, "CO2")             # co2 <- c(360,450,540,630,720)
co2 <- paste0(co2, "ppm")
N30Cat <- as.vector(outer(models, ctwnCats[1:5], paste, sep = "_"))
# Draw plot
drawPlot("CO2 Sensitivity at N=30kg/ha", N30Cat, as.vector(outer(models, co2, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )


# Section 2.2 #
#CO2 at 180 N________________________

# Definition for CO2
co2 <- getLevels(OriData, "CO2")             # co2 <- c(360,450,540,630,720)
co2 <- paste0(co2, "ppm")
N180Cat <- as.vector(outer(models, ctwnCats[6:10], paste, sep = "_"))
# Draw plot
drawPlot("CO2 Sensitivity at N=180kg/ha", N180Cat, as.vector(outer(models, co2, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )


# Section 2.3 #
#Tmax/Tmin________________________

# Definition for TMAX TMIN
## used for plot all tmax tmin senario
# Tmaxmin <- c(-2,0,2,4,6,8)
# Tmaxmin <- paste(Tmaxmin, "degC")
# TMaxMinCat <- as.vector(outer(models, ctwnCats[11:16], paste, sep = "_"))
## used for plot selected tmax tmin senarios
Tmaxmin <- c(0)
Tmaxmin <- paste(Tmaxmin, "degC")
TMaxMinCat <- as.vector(outer(models, ctwnCats[12:12], paste, sep = "_"))
# Draw plot
drawPlot("TmaxTmin Sensitivity", TMaxMinCat, as.vector(outer(models, Tmaxmin, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )


# Section 2.4 #
#Rainfall________________________ 

# Definition for Rain Fall
Rainfall <- c(25,50,75,100,125,150,175,200)
Rainfall <- paste0(Rainfall, "%")
RainCat <- as.vector(outer(models, ctwnCats[17:24], paste, sep = "_"))
# Draw plot
drawPlot("Rainfall Sensitivity", RainCat, as.vector(outer(models, Rainfall, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )


# Section 2.5 #
#Fertilizer (N)________________________

# Definition for Fertilizer
# Fertilizer <- getLevels(OriData, "FEN_TOT") # if original simulation provide FEN_TOT, then this auto-detect would not work properly
# Fertilizer <- round(as.numeric(Fertilizer))
Fertilizer <- c(0,30,60,90,120,150,180,210)
FertCat <- as.vector(outer(models, ctwnCats[25:32], paste, sep = "_"))
# Draw plot
drawPlot("Fertilizer Sensitivity", FertCat, as.vector(outer(models, Fertilizer, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )


# Section 3 #
#___________________________________________#
###### Prob of Exceedance of Linear Factor Analysis #######

# CHANGE NEXT LINES 311-313. Please refer to CTWN/C3MP Protocols (or above key) for identification of linear factor Test #
# CHANGE: If you would like to create Prob of Exceedance plots for a few sensitivity test at a time, then commentadjust the loop start and end
for(testnum in 1:ctwnCatNum) {
  
  title <- paste('Prob of Exceedance_Test',testnum, sep = '')
  setCtwnOutput(title, outputPlotDir, plotFormat)
  r <- range(model[,((testnum - 1) * nummod + 1):(testnum * nummod)],na.rm=TRUE) # Get x-axis limit
  plot(0,xlim=c(r[1],r[2]),ylim=c(0,1),type="n",xlab="Yield (kg/ha)",ylab="Prob of Exceedance")
  for (j in 1 : nummod) {
    idx <- (testnum - 1) * nummod + j
    P <- ecdf(model[,idx])
    y = 1-P(model[,idx])
    probe = sort(y, decreasing=TRUE)
    yields = sort(model[,idx], decreasing=FALSE)
    points(yields, probe, type="o", pch = 20, col = "red", lwd = 2)
  }
  legend("topright", cex=0.75, pch=16, col=c("red","blue"), legend=models, ncol=2)
  graphics.off()
}


# Section 4 #
#___________________________________________#
###### Boxplots of Linear Factor Analysis #######

# ! Support for multi-models is not done for section 4 yet. By Meng Zhang #
# Create a matrix of alternating model results

# Two model matrix - "multimod" is our placeholder matrix here (will make routine run faster)
multimod <- model

# Three model matrix
 # multimod <- matrix(0,30,96)
 # cols1 <- seq(1, by = 3, len = 96)
 # cols2 <- seq(2, by = 3, len = 96)
 # cols3 <- seq(3, by = 3, len = 96)
 # multimod[,cols1] <- APSIM # CHANGE: FILL IN MODEL NAME HERE
 # multimod[,cols2] <- DSSAT # CHANGE: FILL IN MODEL NAME HERE
 # multimod[,cols3] <- INFO # CHANGE: FILL IN MODEL NAME HERE

# Section 4.1 #
#CO2 at 30 N________________________

# Create the plot
title <- "CO2 Sensitivity at N=30kg/ha"
setCtwnOutput(paste("Boxplot", title, sep = " "), outputPlotDir, plotFormat)
boxplot(multimod[,1:10], ylab = "Yield (kg/ha)", xlab = "CO2 Level (ppm)", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14), names=c("360"," ","450"," ","540"," ","630"," ","720"," "), boxwex=0.5)
# Plot means on top of boxplots
col1 <- seq(1, by = 2, len = 5) 
col2 <- seq(2, by = 2, len = 5)
#points(c(1, 4, 7, 10, 13), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2) # Offsets for Alex
points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(multimod[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
legend("topright", title=title, cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
graphics.off()

# drawBoxPlot("CO2 Sensitivity at N=30kg-ha", N30Cat, as.vector(outer(models, Fertilizer, pasteOps)), model, years, outputPlotDir, plotFormat, plotVarID )

# Section 4.2 #
#CO2 at 180 N________________________

# Create the plot
title <- "CO2 Sensitivity at N=180kg/ha"
setCtwnOutput(paste("Boxplot", title, sep = " "), outputPlotDir, plotFormat)
boxplot(multimod[,11:20], ylab = "Yield (kg/ha)", xlab = "CO2 Level (ppm)", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14), names=c("360"," ","450"," ","540"," ","630"," ","720"," "), boxwex=0.5)
# Plot means on top of boxplots
col1 <- seq(11, by = 2, len = 5) 
col2 <- seq(12, by = 2, len = 5)
points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
points(c(1.5, 4.5, 7.5, 10.5, 13.5), colMeans(multimod[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
legend("topright", title=title, cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
graphics.off()

# Section 4.3 #
#Tmax/Tmin________________________

# Create the plot
title <- "TmaxTmin Sensitivity"
setCtwnOutput(paste("Boxplot", title, sep = " "), outputPlotDir, plotFormat)
boxplot(multimod[,21:32], ylab = "Yield (kg/ha)", xlab = "TmaxTmin Change (deg C)", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14, 16,17), names=c("-2"," ","0"," ","+2"," ","+4"," ","+6"," ","+8"," "), boxwex=0.5)
# Plot means on top of boxplots
col1 <- seq(21, by = 2, len = 6) 
col2 <- seq(22, by = 2, len = 6)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5), colMeans(multimod[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
legend("topright", title=title, cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
graphics.off()

# Section 4.4 #
#Rainfall________________________ 

# Create the plot
title <- "Rainfall Sensitivity"
setCtwnOutput(paste("Boxplot", title, sep = " "), outputPlotDir, plotFormat)
boxplot(multimod[,33:48], ylab = "Yield (kg/ha)", xlab = "Percent of baseline rainfall", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14, 16,17, 19,20, 22,23), names=c("25%"," ","50%"," ","75%"," ","100%"," ","125%"," ","150%"," ","175%"," ","200%"," "), boxwex=0.5)
# Plot means on top of boxplots
col1 <- seq(33, by = 2, len = 8) 
col2 <- seq(34, by = 2, len = 8)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5, 19.5, 22.5), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5, 19.5, 22.5), colMeans(multimod[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
legend("topright", title=title, cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
graphics.off()

# Section 4.5 #
#Fertilizer (N)________________________

# Create the plot
title <- "Fertilizer Sensitivity"
setCtwnOutput(paste("Boxplot", title, sep = " "), outputPlotDir, plotFormat)
boxplot(multimod[,49:64], ylab = "Yield (kg/ha)", xlab = "Fertilizer Application (kg/ha)", las = 2, col = c("red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2","red","cyan2"), at=c(1,2, 4,5, 7,8, 10,11, 13,14, 16,17, 19,20, 22,23), names=c("0"," ","30"," ","60"," ","90"," ","120"," ","150"," ","180"," ","210"," "), boxwex=0.5)
# Plot means on top of boxplots
col1 <- seq(49, by = 2, len = 8) 
col2 <- seq(50, by = 2, len = 8)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5, 19.5, 22.5), colMeans(multimod[,col1]), type="o", pch = 20, col = "darkgoldenrod1", lwd = 2)
points(c(1.5, 4.5, 7.5, 10.5, 13.5, 16.5, 19.5, 22.5), colMeans(multimod[,col2]), type="o", pch = 20, col = "chartreuse3", lwd = 2)
legend("topright", title=title, cex=0.75, pch=16, col=c("red","cyan2","darkgoldenrod1","chartreuse3"), legend=c(modname[1], modname[2],paste("Mean", modname[1]),paste("Mean", modname[2])), ncol=2)
graphics.off()

