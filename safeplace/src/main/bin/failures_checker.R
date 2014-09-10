#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
out=args[2]
input=args[1]


pdf(file=out, width=10, height=4)
par(mar=c(3, 3.2, 1, 1), mgp=c(1.8,0.6,0), cex=1.3)
raw <- read.table(input, header=T, sep=" ", na.strings = "-")
df1 <- raw[raw$verif == "checker",]

colnames(df1)[2] = "mode";

long <- melt(df1, id.vars=c("constraint","mode","restriction","tests"), variable.name='errType', value.name="errors");
maxY = max(long[,4], na.rm = T)
ggplot(data=long, aes(x=constraint, y=errors, fill=errType)) + geom_bar(stat="identity", position=position_dodge()) + ylim(0, maxY)

