#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
dta <- read.table(args[1], header=T, sep=";",quote="")

#Get rid of useless elements there
dta <- dta[,c("constraint","label","result")]
names(dta)[names(dta)=="label"] <- "restriction"


## Fine grain: number of errors per constraint wrt. the restriction
#Count per factor
byCstr <- dcast(dta, restriction + constraint ~ result, value.var="result")
#error rate
byCstr$errors <- (byCstr$failure + byCstr$falseNegative + byCstr$falsePositive) / (byCstr$failure + byCstr$falseNegative + byCstr$falsePositive + byCstr$success) * 100
byCstr <- byCstr[,c("errors","constraint","restriction")]

p <- ggplot(byCstr, aes(constraint, errors)) + geom_bar(aes(fill=restriction), position="dodge", stat="identity")
p <- p + theme_bw() + theme(axis.text.x  = element_text(angle=45,hjust=1))  + ylim(0, 100)  + ylab("errors (%)")
ggsave(paste0(args[2],"-fine.pdf"),p, width=8, height=4)


#Corse grain: error type wrt. the restriction
fine <- dcast(dta, result  ~ restriction, value.var="result")

total = sum(fine$continuous)
fine <- fine[!fine$result=="success",]
fine <- melt(fine, c("result"))

cat(length(unique(byCstr$constraint)), " constraint(s)\n")
cat("continuous error rate : ", sum(fine[fine$variable=="continuous",]$value) / total * 100, "%\n")
cat("discrete error rate : ", sum(fine[fine$variable=="discrete",]$value) / total * 100, "%\n")
fine$value = fine$value / total * 100
names(fine) <- c("result","verifier","value")
p <- ggplot(fine, aes(result, value)) + geom_bar(stat="identity", aes(fill=verifier), position="dodge")
p <- p + theme_bw() + ylab("errors (%)") + scale_x_discrete("Error type", labels = c("crashes","over-filtering","under-filtering"))
ggsave(paste0(args[2],"-coarse.pdf"),p, width=8, height=4)



