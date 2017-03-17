#################################################################################################
#
#  This script is to list all the non-repeated value for a particular variable from ACMO files
#
#  Author: Meng Zhang
#  Created: 01/23/2017
#################################################################################################

options(echo = F)
args <- commandArgs(trailingOnly = TRUE)
def_lib_path <- "~\\R\\win-library\\3.3"
if (length(args) == 0) {
  # for R debug purpose
  args <-
    c(
      "~\\R\\win-library\\3.3",
      "CLIM_ID",
      "..\\..\\test\\resources\\r_dev\\ACMO-p",
      "gcm.txt"
    )
}
getwd()
print(args)
.libPaths(def_lib_path)
.libPaths(args[1])
paths <- .libPaths()
print(paths)
library(ggplot2)

plotVarID <- args[2]
inputFolder <- args[3]
output <- args[4]

acmoinputs <- list.files(path = inputFolder, pattern = ".*\\.csv", recursive = TRUE)
acmoinputs <- as.character(acmoinputs)

for (i in 1:length(acmoinputs)) {
  print(acmoinputs[i])
  OriData <-
    read.csv(paste(inputFolder, acmoinputs[i], sep = "/"),
             skip = 2,
             header = T)
  if (is.null(OriData$SUITE_ID)) {
    next
  }
  OriData <-
    OriData[, c("SUITE_ID",plotVarID)]
  if (i == 1) {
    merged <- OriData
  } else {
    merged <- rbind(merged, OriData)
  }
  
}
colnames(merged) <- c("SUITE_ID", "VALUE")
# print(levels(merged$VALUE))
write(levels(merged$VALUE), output)