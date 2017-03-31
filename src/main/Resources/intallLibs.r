libs <- c("ggplot2", "plyr") 
if(all(libs %in% installed.packages()[, "Package"])) { 
  suppressWarnings(sapply(libs, require, character.only = TRUE)) 
} else { 
  cat(sprintf("Installing dependencies from CRAN...\n")) 
  suppressMessages({ 
    install.packages(libs, repos = 'https://cran.rstudio.com', dependencies = TRUE) 
    sapply(libs, require, character.only = TRUE) 
  }) 
}