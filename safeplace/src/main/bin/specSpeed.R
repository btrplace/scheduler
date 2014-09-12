#!/usr/bin/env Rscript
library(ggplot2)
library(reshape)

args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)
#Rate for each test
data$total <- data$V3 + data$V4 + data$V5
data$fuzz <- data$V7 / data$total
data$pre <- data$V8 / data$total
data$test <- data$V9 / data$total
agg <- aggregate(data,by=list(data$V1),FUN=mean, na.rm=TRUE)
agg <- agg[, c("V1","fuzz","pre","test")]
m <- melt(agg,id="V1", variable.name = "phase", value.name = "duration")
print(m)
g <-  ggplot(m,aes(x = m$V1,y = m$value, group= m$variable, fill=m$variable)) + geom_area() + theme_bw() + scale_fill_brewer() + guides(fill = guide_legend(reverse=TRUE))
g + xlab("Elements") + ylab("duration (msec.)")
foo <- dev.off()