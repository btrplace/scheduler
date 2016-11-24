#!/usr/bin/env Rscript
library(ggplot2)
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
d <- read.table(args[1], header=T,sep=";")
total <- sum(d$freq)
d$ratio = d$freq / total * 100;
print(d)
p <- ggplot(d, aes(freq)) + stat_ecdf(geom="step")
p <- p + xlab("frequency") + ylab("ratio") + theme_bw()
ggsave(args[2],p,width=4,height=4)