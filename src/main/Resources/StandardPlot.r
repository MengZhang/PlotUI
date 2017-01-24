#################################################################################################
#
#  This script generates a boxplot/CDF for a simulated variables with GCMs and models
#
#  Author: Meng Zhang
#  Created: 01/12/2017
#################################################################################################

name_unit<-function(inputcode){
  name<-c("ID","Name of experiment", "Field Overlay","Seaonal Strategy","Rotational Analysis","","Treatment Name","Climate ID code","Climate replication number",	"Region ID","Regional stratum identification number","RAP ID", "Management regimen ID","Names of institutions","Crop rotation", "Weather station ID","Soil ID", "Site Latitude", "Site Longitude",	"Crop type", "Crop model-specific cultivar ID", "Cultivar name", "Start of simulation date",	"Planting date","Observed harvested yield, dry weight", "Observed total above-ground biomass at harvest",	"Observed harvest date",	"Total number of irrigation events",	"Total amount of irrigation",	"Type of irrigation application",	"Total number of fertilizer applications",	"Total N applied",	"Total P applied",	"Total K applied",	"Manure and applied oganic matter",	"Total number of tillage applications",	"Tillage type (hand, animal or mechanized)",	"Experiment ID",	"Weather ID",	"Soil ID",	"DOME ID for Overlay",	"DOME ID for Seasonal",  "DOME ID for Rotational", "Short name of crop model used for simulations",	"Model name and version number", "Simulated harvest yield, dry matter", "Simulated above-ground biomass at harvest, dry matter",	"Simulated anthesis date",	"Simulated maturity date",	"Simulated harvest date",	"Simulated leaf area index, maximum",	"Total precipitation from planting to harvest",	"Simulated evapotranspiration, planting to harvest",	"Simulated N uptake during season", "Simulated N leached up to harvest maturity")
  unit<-c("text",	"text",	"text",	"text",	"text",	"number",	"text",	"code",	"number",	"code",	"number",	"code",	"code",	"text",	"number",	"text",	"text",	"decimal degrees",	"decimal degrees",	"text",	"text",	"text",	"yyyy-mm-dd",	"yyyy-mm-dd",	"kg/ha",	"kg/ha",	"yyyy-mm-dd",	"number",	"mm",	"text",	"number",	"kg[N]/ha",	"kg[P]/ha",	"kg[K]/ha",	"kg/ha",	"#",	"text",	"text",	"text",	"text",	"text",	"text",	"text",	"text",	"text",	"kg/ha",	"kg/ha",	"das",	"das",	"das",	"m2/m2",	"mm",	"mm",	"kg/ha",	"kg/ha")
  code<-c("SUITE_ID",	"EXNAME",	"FIELD_OVERLAY",	"SEASONAL_STRATEGY",	"ROTATIONAL_ANALYSIS",	"RUN#",	"TRT_NAME",	"CLIM_ID",	"CLIM_REP",	"REG_ID",	"STRATUM",	"RAP_ID",	"MAN_ID",	"INSTITUTION",	"ROTATION",	"WST_ID",	"SOIL_ID",	"FL_LAT",	"FL_LONG",	"CRID_text",	"CUL_ID",	"CUL_NAME",	"SDAT",	"PDATE",	"HWAH",	"CWAH",	"HDATE",	"IR#C",	"IR_TOT",	"IROP_text",	"FE_#",	"FEN_TOT",	"FEP_TOT",	"FEK_TOT",	"OM_TOT","TI_#",	"TIIMP_text",	"EID",	"WID",	"SID",	"DOID",	"DSID",	"DRID",	"CROP_MODEL",	"MODEL_VER",	"HWAH_S",	"CWAH_S",	"ADAT_S",	"MDAT_S",	"HADAT_S",	"LAIX_S",	"PRCP_S",	"ETCP_S",	"NUCM_S",	"NLCM_S")
  for (thisi in 1:length(code)) {
    if (inputcode==code[thisi]) {
      all<-paste(name[thisi],"(",unit[thisi],")")
      break
    }
  }
  return(all)
}

# RHOME = "D:\\SSD_USER\\Documents\\R projects\\R_lib\\"
# library(ggplot2, lib.loc = RHOME)
# library(labeling, lib.loc = RHOME)
# library(digest, lib.loc = RHOME)
options(echo = TRUE)
args <- commandArgs(trailingOnly = TRUE)
def_lib_path <- "~\\R\\win-library\\3.3"
if (length(args) == 0) {
  # for R debug purpose
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "Result",
      "HWAH_S",
      "png",
      "HWAH_S",
      "..\\..\\test\\resources\\r_dev\\bad_data",
      "..\\..\\test\\resources\\r_dev\\plot_output",
      "acmo_output.csv",
      "STDPLOT",
      "IEFA:Hot-Dry_0XFX:Base_"
    )
}
getwd()
print(args)
.libPaths(def_lib_path)
.libPaths(args[1])
paths <- .libPaths()
print(paths)
library(ggplot2)
title <- args[2]
plotType <- args[3]
plotFormat <- args[4]
plotVarID <- args[5]
inputFolder <- args[6]
outputPath <- args[7]
outputAcmo <- paste(outputPath, args[8], sep = "/")
outputPlot <-
  paste(paste(args[9], args[3], "ABSOLUTE", plotVarID, sep = "-"),
        plotFormat,
        sep = ".")
gcmCatPairs <- strsplit(args[10], split = "_")[[1]]

acmoinputs <- list.files(path = inputFolder, pattern = ".*\\.csv")
acmoinputs <- as.character(acmoinputs)

for (i in 1:length(acmoinputs)) {
  print(acmoinputs[i])
  OriData <-
    read.csv(paste(inputFolder, acmoinputs[i], sep = "/"),
             skip = 2,
             header = T)
  OriData <-
    OriData[, c("CLIM_ID", "CROP_MODEL", plotVarID, "PDATE")]
  if (i == 1) {
    merged <- OriData
  } else {
    merged <- rbind(merged, OriData)
  }
  
}

if (plotVarID %in% c("ADAT_S", "MDAT_S", "HADAT_S")) {
  pdate <- as.POSIXct(merged$PDATE, format = "%Y-%m-%d")
  ndate <- as.POSIXct(merged[[plotVarID]], format = "%Y-%m-%d")
  dap <- as.numeric(ndate - pdate)
  merged <- data.frame(
    GCM = merged$CLIM_ID,
    MODEL = merged$CROP_MODEL,
    VALUE = dap
  )
  merged <- subset(merged, VALUE != "NA")
} else {
  colnames(merged) <- c("GCM", "MODEL", "VALUE")
}

gcmNum <- length(levels(merged$GCM))
merged$GCM <- as.character(merged$GCM)

for (i in 1 : length(gcmCatPairs)) {
  tmp <- strsplit(gcmCatPairs[i], split = ":")[[1]]
  merged$GCM[merged$GCM == tmp[1]] <- paste(tmp[2], tmp[1], sep = "_")
}
merged$GCM <- as.factor(merged$GCM)

print(paste("Detect", gcmNum, "GCMs", sep = " "))
#str(merged)

# end85 <- read.csv(outputAcmo,
#                   sep = ",",
#                   dec = ".",
#                   skip = 0)
end85 <- merged

num <- nrow(merged)
start <- 1
while (start <= num) {
  end <- start + 29
  if (end > num) {
    end <- num
  }
  farm <- merged[start:end, ]
  farm <- subset(farm, VALUE != "" & VALUE != -99 )
  print(farm)
  if (nrow(farm) == 0) {
    start <- end + 1
    next
  }
  farmData <- data.frame(
    GCM = farm[1,]$GCM,
    MODEL = farm[1,]$MODEL,
    VALUE = ave(farm$VALUE)[1]
  )
  if (start == 1) {
    mergedAve <- farmData
  } else {
    mergedAve <- rbind(mergedAve, farmData)
  }
  
  start <- end + 1
}
print(mergedAve)
write.csv(mergedAve, outputAcmo)

if (plotType == "BoxPlot") {
  ggplot(data = mergedAve, aes(x = MODEL, y = VALUE)) +
    geom_boxplot(
      aes(fill = GCM),
      outlier.colour = NA,
      width = 0.05 * gcmNum,
      color = "black"
    )  +
    coord_cartesian(ylim = range(boxplot(mergedAve$VALUE, plot = FALSE)$stats) *
                      c(.9, 1.3)) +  theme_bw() +
    theme(legend.text = element_text(size = 13),
          legend.title = element_text(size = 13)) +
    theme(axis.text = element_text(size = 13)) +
    theme(axis.title = element_text(size = 13, face = "bold")) +
    labs(x = "Models", y = plotVarID, colour = "legend") +
    theme(panel.grid.minor = element_blank()) +
    theme(plot.margin = unit(c(1, 1, 1, 1), "mm")) +
    theme(axis.text.x = element_text(angle = 60, hjust = 1)) +
    # scale_colour_manual(values = c("black", "green", "red", "blue"))
  
  # ggplot( mergedAve, aes( GCM, YIELD)) + geom_boxplot() +
  #   ggtitle( paste( paste0("Crop", "-Climate Change Ratio"), "RCP", sep="\n")) +
  #   scale_x_discrete(name =" " ) +
  #     # coord_cartesian(ylim = range(boxplot(mergedAve$YIELD, plot = FALSE)$stats) *
  #     #                   c(.9, 1.3)) +
  #   ylab( "CC Ratio") + #ylim(0, (max( CCRatioBox[2]) + 0.1) ) +
  #   theme(axis.text.x = element_text(face="bold", color="#993333", size=14, angle=0), axis.text.y = element_text(face="bold", color="#993333", size=14, angle=0) ) +
  #   theme(plot.title = element_text(lineheight=1, face="bold", size = rel(1.5))) +
  #   theme(axis.title.y = element_text(size = rel(1.5), angle = 90, face="bold", color="black" )) +
  #   scale_fill_manual(values=c("turquoise3", "indianred1"),  name="Model", breaks=c("DSSAT", "APSIM"), labels=c("DSSAT", "APSIM"))
  
  ggsave(
    filename = outputPlot,
    plot = last_plot(),
    path = outputPath,
    device = plotFormat
  )
  
} else if (plotType == "CDF") {
  if (plotFormat == "png") {
    png(paste(outputPath, outputPlot, sep = "/"),
        width = 850,
        height = 500)
  } else if (plotFormat == "pdf") {
    pdf(paste(outputPath, outputPlot, sep = "/"),
        width = 9,
        height = 5)
  }

  r <- range(mergedAve$VALUE, na.rm = TRUE)
  colors <- c("red", "green")
  models <- levels(mergedAve$MODEL)

  for (i in 1:length(models)) {

    ddsub <- ecdf(subset(mergedAve$VALUE, mergedAve$MODEL == models[i]))

    if (i == 1) {
      curve(
        1 - ddsub(x),
        from = r[1],
        to = r[2],
        col = colors[1],
        xlim = r,
        main = title,
        ylab = "Cumulative Frequency",
        xlab = name_unit(plotVarID)
      )
    } else {
      curve(
        1 - ddsub(x),
        from = r[1],
        to = r[2],
        col = c(colors[i]),
        add = TRUE
      )
    }

  }
  legend("topright",
         models,
         col = colors,
         lty = "solid")

  graphics.off()
}

#################################################################
