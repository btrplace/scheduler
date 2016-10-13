#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")

#Get rid of useless elements there
dta <- dta[,c("constraint","label","result")]
names(dta)[names(dta)=="label"] <- "verifier"


## Fine grain: number of errors per constraint wrt. the verifier
#Count per factor
byCstr <- dcast(dta, verifier + constraint ~ result, value.var="result")

#error rate
byCstr$errors <- (byCstr$failure + byCstr$falseNegative + byCstr$falsePositive) / (byCstr$failure + byCstr$falseNegative + byCstr$falsePositive + byCstr$success) * 100
byCstr <- byCstr[,c("errors","constraint","verifier")]

p <- ggplot(byCstr, aes(constraint, errors)) + geom_bar(aes(fill=verifier), position="dodge", stat="identity")
p <- p + theme_bw() + theme(axis.text.x  = element_text(angle=45,hjust=1))  + ylim(0, 100)  + ylab("errors (%)")
ggsave(paste0(args[2],"-fine.pdf"),p, width=8, height=4)


#Corse grain: error type wrt. the verifier
fine <- dcast(dta, verifier ~ result, value.var="result")
fine$total <- fine$failure + fine$falseNegative + fine$falsePositive + fine$success
fine$errs <- fine$failure + fine$falseNegative + fine$falsePositive

e1 = fine[which(fine$verifier == "checker"),]$errs
e2 = fine[which(fine$verifier == "spec"),]$errs
cat("Bugs with the checkers: ",e1,"\n")
cat("Bugs with safeplace: ",e2,"\n")
cat("Improvement: ",e2/e1,"\n")

fine$falseNegative = fine$falseNegative / fine$total * 100
fine$falsePositive = fine$falsePositive / fine$total * 100
fine$failure = fine$failure / fine$total * 100
fine <- fine[,c("verifier","failure","falseNegative","falsePositive")]
fine <- melt(fine, by="verifier")

p <- ggplot(fine, aes(verifier, value)) + geom_bar(aes(fill=variable), stat="identity")
p <- p + theme_bw() + ylab("errors (%)")
ggsave(paste0(args[2],"-coarse.pdf"),p, width=5, height=4)



