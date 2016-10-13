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
p <- ggplot(dta_speed,aes(x = dta_speed$vms,y = dta_speed$value, group=dta_speed$variable, fill=dta_speed$variable))
p <- p + geom_area() + scale_fill_brewer(name="phase") + guides(fill = guide_legend(reverse=TRUE))
p <- p + xlab("scaling") + ylab("duration in msec.") + theme_bw() + theme(legend.position = c(0.1, 0.75))
ggsave(paste(args[2],"-speed.pdf",sep=""),p, width=8, height=4)

dta_iter <- dta[,c("vms","iterations")]
dta_iter <- aggregate(dta_iter,by=list(dta$vms),FUN=mean, na.rm=TRUE)
dta_iter$Group.1 <- NULL
dta_iter <- melt(dta_iter,id="vms")
p <- ggplot(dta_iter,aes(x = dta_iter$vms,y = dta_iter$value))
p <- p + geom_line()
p <- p + xlab("scaling") + ylab("fuzzing iterations") + theme_bw()
ggsave(paste(args[2],"-iterations.pdf",sep=""),p, width=8, height=4)