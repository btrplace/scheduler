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
dta <- melt(dta, by="constraint")

p <- ggplot(dta, aes(constraint, value)) + geom_bar(aes(fill=variable), stat="identity")
p <- p + theme_bw() + theme(axis.text.x  = element_text(angle=45,hjust=1))  + ylim(0, 100) + ylab("percentage")
ggsave(args[2],p, width=8, height=4)
