#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")
#Get rid of useless elements there
dta <- dta[,c("constraint","result")]
#Count per factor
dta <- dcast(dta, constraint ~ result, value.var="result")

#error rate
dta$total <- dta$failure + dta$falseNegative + dta$falsePositive + dta$success
dta$failure <- dta$failure / dta$total * 100
dta$falseNegative <- dta$falseNegative / dta$total * 100
dta$falsePositive <- dta$falsePositive / dta$total * 100
dta <- dta[,c("constraint","falseNegative","falsePositive","failure")]
names(dta) <- c("constraint","over-filtering","under-filtering","crashes")

dta <- melt(dta, by="constraint")
p <- ggplot(dta, aes(constraint, value)) + geom_bar(aes(fill=variable), stat="identity") + scale_fill_brewer(name="error")

p <- p + theme_bw() + ylim(0, 100) + ylab("defect rate") + xlab("constraint")

#theme(axis.text.x  = element_text(angle=45,hjust=1))

big = element_text(size = 16, family="Times")
med = element_text(size = 12, family="Times")
lbls = element_text(angle=45,hjust=1, size=11, family="Times")

p <- p + theme(panel.grid.minor = element_blank(), legend.position = c(0.85, 0.65), axis.text = med, axis.text.x = lbls, axis.title = big, axis.title = big, legend.title=med, legend.text=med)

ggsave(args[2],p, width=9, height=4)
