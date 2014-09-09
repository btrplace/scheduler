#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)
library(scales)
args <- commandArgs(T)
input=args[1]

pdf(file=paste0(input,"/all-impl.pdf"), width=10, height=4)
par(mar=c(3, 3.2, 1, 1), mgp=c(1.8,0.6,0), cex=1.3)

coarse <- function(id) {
	dta <- read.table(paste0(input,"/",id,".data"), header=T, sep=" ", na.strings = "-")	
	ok <- aggregate(dta$falseOk, by=list(dta$verif, dta$restriction), FUN=sum, na.rm=TRUE)
	colnames(ok) = c("verif","restriction","falseOk")
	ko <- aggregate(dta$falseKo, by=list(dta$verif, dta$restriction), FUN=sum, na.rm=TRUE)
	colnames(ko) = c("verif","restriction","falseKo")
	#print(dta);
	#print(ko);	
	res = merge(ok, ko);	
	res["type"] = id;
	return(res);
}

df <- rbind(coarse("core"), coarse("states"), coarse("VM2VM"), coarse("VM2PM"), coarse("counting"))
df <- df[df$verif != "checker",]

colnames(df)[1] = "mode";
df <- transform(df, mode=ifelse(mode=="impl","build","repair"))
long <- melt(df, id.vars=c("type","mode","restriction"), variable.name='errType', value.name="errors");
hp <- ggplot(long, aes(x=mode, y=errors, fill=restriction)) + geom_bar(stat="identity", position=position_dodge(), width=0.7)
hp + facet_grid(errType~type)