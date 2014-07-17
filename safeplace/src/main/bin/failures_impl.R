#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
out=args[2]
input=args[1]

pdf(file=out, width=10, height=4)
par(mar=c(3, 3.2, 1, 1), mgp=c(1.8,0.6,0), cex=1.3)
raw <- read.table(input, header=T, sep=" ", na.strings = "-")
df1 <- raw[raw$verif != "checker",]

colnames(df1)[2] = "mode";
df1 <- transform(df1,mode=ifelse(mode=="impl","build","repair"))

long <- melt(df1, id.vars=c("constraint","mode","restriction","tests"), variable.name='errType', value.name="errors");
hp <- ggplot(long, aes(x=mode, y=errors, fill=restriction)) + geom_bar(stat="identity", position=position_dodge(), width=0.7)
hp + facet_grid(errType~constraint)

