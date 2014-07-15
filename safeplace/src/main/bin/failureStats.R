#!/usr/bin/env Rscript
library(ggplot2)

args <- commandArgs(T)
pdf(file=paste(args[2]), width=5, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
df1 <- read.table(args[1], header=T, sep=" ")

maxY = max(df1[,4])

ggplot(data=df1, aes(x=verif, y=failures, fill=constraint)) + geom_bar(stat="identity", position=position_dodge()) + ylim(0, maxY)