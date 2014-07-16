#!/usr/bin/env Rscript
library(ggplot2)

args <- commandArgs(T)
out=args[3]
r = args[1]
input=args[2]

pdf(file=out, width=5, height=4)
par(mar=c(3, 3.2, 1, 1), mgp=c(1.8,0.6,0), cex=1.3)
raw <- read.table(input, header=T, sep=" ", na.strings = "-")

df1 <- raw[raw$restriction==r,]
maxY = max(df1[,5], na.rm = T)
ggplot(data=df1, aes(x=verif, y=failures, fill=constraint)) + geom_bar(stat="identity", position=position_dodge()) + ylim(0, maxY)
foo <- dev.off()
