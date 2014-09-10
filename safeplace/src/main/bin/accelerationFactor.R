#!/usr/bin/env Rscript
args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=T, sep=" ")
baseline = data[1,2];
#print(baseline)
data[,3] = data[,2] / baseline
colnames(data)[3] = "acceleration factor"
print(data)
plot(data[,1], data[,3], cex=0.5, type="o", col=grey(0), lty=1, do.points=FALSE, lwd=1, xlab="Cores", ylab="Acceleration factor", main="", verticals = TRUE, panel.first=grid())
#foo <- dev.off()