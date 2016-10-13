#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")
dta$elements <- dta$vms + dta$nodes
#Get rid of useless elements there
dta <- dta[, c("elements","label","result")];
names(dta)[names(dta)=="label"] <- "population"

#Count per factor
dta <- dcast(dta, population + elements ~ result, value.var="result")

#error rate
dta$errors <- (dta$failure + dta$falseNegative + dta$falsePositive) / (dta$failure + dta$falseNegative + dta$falsePositive + dta$success) * 100
dta <- dta[,c("errors","elements","population")]
p <- ggplot(dta, aes(population, elements)) + geom_tile(aes(fill=errors), colour="white") + theme_bw()
p <- p + scale_fill_gradient(low="white",high="steelblue")
p <- p + geom_text(aes(fill = dta$errors, label = round(dta$errors, 1))) + ylab("nodes + vms")
ggsave(args[2],p, width=8, height=4)