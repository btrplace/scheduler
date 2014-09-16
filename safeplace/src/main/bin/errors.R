#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

#plot
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Keep only meaningful columns
data <- data[, c("V4","V9","V10")];

data$elements <- data$V9 + data$V10
colnames(data) <- c("error","nodes","vms","elements")
data <- data[,c("error","elements")]
agg <- aggregate(data, by=list(data$error,data$elements), FUN=length)
agg <- agg[,c("Group.1","Group.2","error")]
colnames(agg) <- c("kind","elements","value")

d <- dcast(agg, elements ~ kind , value.var="value", na.rm = TRUE)
d$errorPct = (d$failure + d$falsePositive + d$falseNegative) / (d$failure + d$falsePositive + d$falseNegative + d$success) * 100
d <- d[,c("elements","errorPct")]

pdf(file=args[2], width=8, height=4)
ggplot(d,aes(x=elements,y=errorPct)) + geom_line() + theme_bw() + ylim(0,50) + ylab("% of errors") + xlab("VMs + nodes")
foo <- dev.off()
