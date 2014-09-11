#!/usr/bin/env Rscript
args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=T, sep=" ")
cdf <- ecdf(data$length)
plot(cdf, col=grey(0), lty=1, do.points=FALSE, lwd=3, xlab="# characters", ylab="ratio of constraints", main="", verticals = TRUE, panel.first=grid())
foo <- dev.off()