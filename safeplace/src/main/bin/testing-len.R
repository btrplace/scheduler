#!/usr/bin/env Rscript
library(ggplot2)
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
d <- read.table(args[1], header=T, sep=";",quote="")

m1 = mean(d[which(d$testing=="safeplace"),] $sloc)
m2 = mean(d[which(d$testing=="legacy"),] $sloc)
factor = m2 / m1
cat("Mean sloc. per testing campaign", m1,"\n")
cat("Mean sloc. per legacy unit test", m2,"\n")
cat("Reduction factor: ",factor,"\n")

p <- ggplot(d, aes(sloc, color=testing)) + stat_ecdf(geom="step")
p <- p + xlab("length (loc.)") + ylab("ratio") + theme_bw()
p <- p + theme(legend.justification=c(1,0), legend.position=c(1,0))
ggsave(args[2],p,width=4,height=4)