#!/usr/bin/env Rscript
library(ggplot2)
library(reshape)

args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Get rid of useless elements there
data <- data[, c("V5","V6","V7","V8","V9","V10")];

#Group by #elements
data$elements <- data$V9 + data$V10;
colnames(data) <- c("fuzz","validation","test","reduce", "nodes","vms","elements")

agg <- aggregate(data,by=list(data$elements),FUN=mean, na.rm=TRUE)
agg <- agg[,c("fuzz","validation","test", "elements")]

m <- melt(agg,id="elements")
colnames(m) <- c("elements", "phase", "duration")
print(m)
p <- ggplot(m,aes(x = m$elements,y = m$duration, group=m$phase, fill=m$phase)) + geom_area() + theme_bw() + scale_fill_brewer(name="phase") + guides(fill = guide_legend(reverse=TRUE))
p + xlab("VMs + nodes") + ylab("duration in msec.")
foo <- dev.off()