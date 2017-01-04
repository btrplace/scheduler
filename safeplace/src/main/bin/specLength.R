#!/usr/bin/env Rscript
library(ggplot2)
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
d <- read.table(args[1], header=F)
cat("Mean chars. per invariant", median(d$V1),"\n")
cat(quantile(d$V1, c(.50, .95)),"\n")
p <- ggplot(d, aes(V1)) + stat_ecdf(geom="step")
p <- p + xlab("length (char.)") + ylab("ratio") + theme_bw()

big = element_text(size = 19, family="Times")
med = element_text(size = 16, family="Times")
p <- p + theme(axis.text = med, axis.title = big, axis.title = big, legend.title=big, legend.text=med)

ggsave(args[2],p,width=4,height=4)