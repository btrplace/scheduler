#!/usr/bin/env Rscript
library(ggplot2)
library(reshape)

args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Get rid of useless elements there
data <- data[data$V4 != "success", c("V8","V9","V10")];

#Group by #elements
data$elements <- data$V9 + data$V10;
colnames(data) <- c("reduce", "nodes", "vms", "elements")

agg <- aggregate(data,by=list(data$elements),FUN=mean, na.rm=TRUE)
print(agg)
p <- ggplot(agg,aes(x = agg$elements,y = agg$reduce)) + geom_line() + geom_point() + theme_bw()
p + xlab("VMs + nodes") + ylab("duration in msec.")
foo <- dev.off()