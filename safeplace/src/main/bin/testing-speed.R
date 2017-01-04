#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")

#Get rid of useless elements there
dta_speed <- dta[, c("vms","fuzzing","validation","testing")];

dta_speed <- aggregate(dta_speed,by=list(dta$vms),FUN=mean, na.rm=TRUE)
dta_speed$Group.1 <- NULL
dta_speed <- melt(dta_speed,id="vms")

big = element_text(size = 19, family="Times")
med = element_text(size = 16, family="Times")

p <- ggplot(dta_speed,aes(x = dta_speed$vms,y = dta_speed$value, group=dta_speed$variable, fill=dta_speed$variable))
p <- p + geom_area() + scale_fill_brewer(name="stage") + guides(fill = guide_legend(reverse=TRUE))
p <- p + xlab("scaling") + ylab("duration (msec.)") + theme_bw() + theme(legend.position = c(0.2, 0.75)) + ylim(0, 40)
p <- p + theme(axis.text = med, axis.title = big, axis.title = big, legend.title=big, legend.text=med)

ggsave(paste(args[2],"-speed.pdf",sep=""),p, width=4, height=4)