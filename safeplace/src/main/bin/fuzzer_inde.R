#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)
library(MASS)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")
dta$errors = round(dta$errors)
dta <- dta[which(dta$errors != 17 & dta$errors != 13),]
t <- table(dta$errors,dta$population)
print(t)
chisq.test(t)
#cor(t, use="all.obs", method="kendall")