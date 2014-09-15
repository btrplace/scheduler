#!/usr/bin/env Rscript
library(ggplot2)
library(reshape)

#plot
args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Keep only meaningful columns
data <- data[data$V4 != "success" & data$V4 != "failure", c("V9","V12","V13")];
colnames(data) <- c("elements","nodes","vms")

agg <- aggregate(data,by=list(data$elements),FUN=mean, na.rm=TRUE)
agg <- agg[,c("elements", "nodes", "vms")]
m <- melt(agg,id="elements")

p <- ggplot(m, aes(x=m$elements,y=m$value, group=m$variable, colour=m$variable, name="type")) + geom_line() + geom_point()
p + theme_bw() + guides(fill = guide_legend(reverse=TRUE)) + xlab("elements") + ylab("retained")
foo <- dev.off()