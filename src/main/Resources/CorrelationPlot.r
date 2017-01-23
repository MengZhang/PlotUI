#################################################################################################
#     faceit -> AgMIP
#  This script generates a correlation scatterplot of simulated variables with climatic variables
#
#  Author: Wei Xiong (Original), Meng Zhang (Enhancement)
#  Created: 09/29/2014
#  Updated: 01/16/2017 (Transfer to desktop plot tools)
#################################################################################################

options(echo = TRUE)
args <- commandArgs(trailingOnly = TRUE)
def_lib_path <- "~\\R\\win-library\\3.3"
if (length(args) == 0) {
  # for R debug purpose
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "..\\..\\test\\resources\\r_dev\\good_data",
      "png",
      "NUCM_S",
      "HWAH_S",
      "CRID_text",
      "SOIL_ID",
      "..\\..\\test\\resources\\r_dev\\plot_output",
      "CorPlot"
    )
}
getwd()
print(args)
.libPaths(def_lib_path)
.libPaths(args[1])
paths <- .libPaths()
print(paths)
library(ggplot2)
library(lattice)
library(MASS)

acmocsv <- args[2]
plotFormat <- args[3]
varNameX <- args[4]#"ETCP_S"
varNameY <- args[5]#"HWAH_S"
group1 <- args[6]
group2 <- args[7]
outputPlot <- paste(args[9], plotFormat, sep = ".")
output <- paste(args[8], outputPlot, sep = "/")

Group <- "NO"
if (group1 != "NO" && group2 != "NO") {
  if (group1 == group2) {
    Group <- group1
  } else {
    Group <- paste(group1, group2, sep = "+")
  }
} else if (group1 == "NO" || group2 == "NO") {
  if (group1 != "NO") {
    Group <- group1
  } else {
    Group <- group2
  }
} else{
  Group <- "NO"
}

name_unit <- function(inputcode) {
  name <-
    c(
      "ID",
      "Name of experiment",
      "Field Overlay",
      "Seaonal Strategy",
      "Rotational Analysis",
      "",
      "Treatment Name",
      "Climate ID code",
      "Climate replication number",
      "Region ID",
      "Regional stratum identification number",
      "RAP ID",
      "Management regimen ID",
      "Names of institutions",
      "Crop rotation",
      "Weather station ID",
      "Soil ID",
      "Site Latitude",
      "Site Longitude",
      "Crop type",
      "Crop model-specific cultivar ID",
      "Cultivar name",
      "Start of simulation date",
      "Planting date",
      "Observed harvested yield, dry weight",
      "Observed total above-ground biomass at harvest",
      "Observed harvest date",
      "Total number of irrigation events",
      "Total amount of irrigation",
      "Type of irrigation application",
      "Total number of fertilizer applications",
      "Total N applied",
      "Total P applied",
      "Total K applied",
      "Manure and applied oganic matter",
      "Total number of tillage applications",
      "Tillage type (hand, animal or mechanized)",
      "Experiment ID",
      "Weather ID",
      "Soil ID",
      "DOME ID for Overlay",
      "DOME ID for Seasonal",
      "DOME ID for Rotational",
      "Short name of crop model used for simulations",
      "Model name and version number",
      "Simulated harvest yield, dry matter",
      "Simulated above-ground biomass at harvest, dry matter",
      "Simulated anthesis date",
      "Simulated maturity date",
      "Simulated harvest date",
      "Simulated leaf area index, maximum",
      "Total precipitation from planting to harvest",
      "Simulated evapotranspiration, planting to harvest",
      "Simulated N uptake during season",
      "Simulated N leached up to harvest maturity"
    )
  unit <-
    c(
      "text",
      "text",
      "text",
      "text",
      "text",
      "number",
      "text",
      "code",
      "number",
      "code",
      "number",
      "code",
      "code",
      "text",
      "number",
      "text",
      "text",
      "decimal degrees",
      "decimal degrees",
      "text",
      "text",
      "text",
      "yyyy-mm-dd",
      "yyyy-mm-dd",
      "kg/ha",
      "kg/ha",
      "yyyy-mm-dd",
      "number",
      "mm",
      "text",
      "number",
      "kg[N]/ha",
      "kg[P]/ha",
      "kg[K]/ha",
      "kg/ha",
      "#",
      "text",
      "text",
      "text",
      "text",
      "text",
      "text",
      "text",
      "text",
      "text",
      "kg/ha",
      "kg/ha",
      "das",
      "das",
      "das",
      "m2/m2",
      "mm",
      "mm",
      "kg/ha",
      "kg/ha"
    )
  code <-
    c(
      "SUITE_ID",
      "EXNAME",
      "FIELD_OVERLAY",
      "SEASONAL_STRATEGY",
      "ROTATIONAL_ANALYSIS",
      "RUN#",
      "TRT_NAME",
      "CLIM_ID",
      "CLIM_REP",
      "REG_ID",
      "STRATUM",
      "RAP_ID",
      "MAN_ID",
      "INSTITUTION",
      "ROTATION",
      "WST_ID",
      "SOIL_ID",
      "FL_LAT",
      "FL_LONG",
      "CRID_text",
      "CUL_ID",
      "CUL_NAME",
      "SDAT",
      "PDATE",
      "HWAH",
      "CWAH",
      "HDATE",
      "IR#C",
      "IR_TOT",
      "IROP_text",
      "FE_#",
      "FEN_TOT",
      "FEP_TOT",
      "FEK_TOT",
      "OM_TOT",
      "TI_#",
      "TIIMP_text",
      "EID",
      "WID",
      "SID",
      "DOID",
      "DSID",
      "DRID",
      "CROP_MODEL",
      "MODEL_VER",
      "HWAH_S",
      "CWAH_S",
      "ADAT_S",
      "MDAT_S",
      "HADAT_S",
      "LAIX_S",
      "PRCP_S",
      "ETCP_S",
      "NUCM_S",
      "NLCM_S"
    )
  for (thisi in 1:length(code)) {
    if (inputcode == code[thisi]) {
      all <- paste(name[thisi], "(", unit[thisi], ")")
      break
    }
  }
  return(all)
}

# OriData <- read.csv(acmocsv, skip = 2, header = T)
acmoinputs <- list.files(path = inputFolder, pattern = ".*\\.csv")
acmoinputs <- as.character(acmoinputs)

for (i in 1:length(acmoinputs)) {
  print(acmoinputs[i])
  OriData <-
    read.csv(paste(inputFolder, acmoinputs[i], sep = "/"),
             skip = 2,
             header = T)
  if (group2 == "NO") {
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

if (Group != "NO") {
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
)

graphics.off()
#}
