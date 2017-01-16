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
      "b",
      "png",
      "HWAH_S",
      "..\\..\\test\\resources\\r_dev\\input_AVG.txt",
      "..\\..\\test\\resources\\r_dev\\output",
      "acmo_output.csv",
      "output"
    )
}
getwd()
print(args)
.libPaths(args[1])
.libPaths(def_lib_path)
paths <- .libPaths()
print(paths)
library(ggplot2)
title <- args[2]
plotType <- args[3]
plotFormat <- args[4]
plotVarID <- args[5]
inputFile <- args[6]
outputPath <- args[7]
outputAcmo <- paste(outputPath, args[8], sep = "/")
outputPlot <- paste(args[9], plotFormat, sep = ".")
gcmNum <- 3

df <- read.table(inputFile, header = T, sep = "")
acmoinputs <- as.character(df$csv)
groups <- as.character(df$group)
colors <- as.character(df$color)

for (i in 1:length(acmoinputs)) {
  OriData <- read.csv(acmoinputs[i], skip = 2, header = T)
  OriData <- OriData[, c("CLIM_ID", "CROP_MODEL", plotVarID)]
  if (i == 1) {
    merged <- OriData
  } else {
    merged <- rbind(merged, OriData)
  }
  
}

if (plotVarID == "HWAH_S") {
  plotVarName <- "YIELD"
} else {
  plotVarName <- plotVarID
}
colnames(merged) <- c("GCM", "MODEL", plotVarName)
str(merged)
write.csv(merged, outputAcmo)

# end85 <- read.csv(outputAcmo,
#                   sep = ",",
#                   dec = ".",
#                   skip = 0)
end85 <- merged
#head(end85)
# #factor 1 -CLIMID
# end85$f1 <- factor(end85$GCM)
# #factor 2 -MODEL
# end85$f2 <- factor(end85$MODEL)

# ggplot(data = end85, aes(x = GCM, y = YIELD)) +
#   geom_boxplot(
#     aes(fill = MODEL),
#     outlier.colour = NA,
#     width = 0.05 * gcmNum,
#     color = "black"
#   )  +
#   coord_cartesian(ylim = range(boxplot(end85$YIELD, plot = FALSE)$stats) *
#                     c(.9, 1.3)) +  theme_bw() +
#   theme(legend.text = element_text(size = 13),
#         legend.title = element_text(size = 13)) +
#   theme(axis.text = element_text(size = 13)) +
#   theme(axis.title = element_text(size = 13, face = "bold")) +
#   labs(x = "GCMs", y = "Maize Yield (Kg/Ha)", colour = "legend") +
#   theme(panel.grid.minor = element_blank()) +
#   theme(plot.margin = unit(c(1, 1, 1, 1), "mm")) +
#   theme(axis.text.x = element_text(angle = 60, hjust = 1))

ggplot( end85, aes( GCM, YIELD)) + geom_boxplot() + 
  ggtitle( paste( paste0("Crop", "-Climate Change Ratio"), "RCP", sep="\n")) + 
  scale_x_discrete(name =" " ) + 
    # coord_cartesian(ylim = range(boxplot(end85$YIELD, plot = FALSE)$stats) *
    #                   c(.9, 1.3)) +
  ylab( "CC Ratio") + #ylim(0, (max( CCRatioBox[2]) + 0.1) ) +
  theme(axis.text.x = element_text(face="bold", color="#993333", size=14, angle=0), axis.text.y = element_text(face="bold", color="#993333", size=14, angle=0) ) +   
  theme(plot.title = element_text(lineheight=1, face="bold", size = rel(1.5))) +  
  theme(axis.title.y = element_text(size = rel(1.5), angle = 90, face="bold", color="black" )) +
  scale_fill_manual(values=c("turquoise3", "indianred1"),  name="Model", breaks=c("DSSAT", "APSIM"), labels=c("DSSAT", "APSIM"))  

ggsave(
  filename = outputPlot,
  plot = last_plot(),
  path = outputPath,
  device = plotFormat
)

#################################################################
