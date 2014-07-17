#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

pdf(file="tmp/VM2VM.pdf", width=10, height=4)
par(mar=c(3, 3.2, 1, 1), mgp=c(1.8,0.6,0), cex=1.3)
raw <- read.table("tmp/VM2VM.data", header=T, sep=" ", na.strings = "-")
df1 <- raw[raw$verif != "checker",]

colnames(df1)[2] = "mode";
#df1$mode = ifelse(df$mode == "impl", "build", "repair")
df1 <- transform(df1,mode=ifelse(mode=="impl","build","repair"))

long <- melt(df1, id.vars=c("constraint","mode","restriction","tests"), variable.name='errType', value.name="errors");
#long <- transform(long,errType=ifelse(mode=="falseOk","under-filtering","over-filtering"))
print(long)
hp <- ggplot(long, aes(x=mode, y=errors, fill=restriction)) + geom_bar(stat="identity", position=position_dodge(), width=0.7)
hp + facet_grid(errType~constraint)
#foo <- dev.off();

