#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

#plot
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Keep only meaningful columns
data <- data[data$V9==3, c("V1","V2","V4")];
colnames(data) <- c("cstr","restriction","res")
VM2VM <- c("Spread","Gather","Among", "Split","Lonely","SplitAmong")
VM2PM <- c("Ban","Fence","Root","Quarantine")
States <- c("Online","Offline","Running","Sleeping","Ready","Killed")
Counting <- c("RunningCapacity","MaxOnline")
Core <- c("toRunning","toSleeping","noVMsOnOfflineNodes","toReady")

data$kind = ifelse(data$cstr %in% States, "states",
                ifelse(data$cstr %in% Core, "core",
                ifelse(data$cstr %in% VM2VM, "vm2vm",
                ifelse(data$cstr %in% VM2PM, "vm2pm",
                ifelse(data$cstr %in% Counting, "counting","other")))))
a <- aggregate(data,by=list(data$kind,data$res, data$restriction),FUN=length)
a <- a[,c("Group.1","Group.2","Group.3", "cstr")]
colnames(a) <- c("family","kind", "restriction", "value")
d <- dcast(a, family + restriction ~ kind , value.var="value", na.rm = TRUE)
d[is.na(d)] <- 0
#to pct
#print(d)
d$failurePct = 0#d$failure / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$falsePositivePct = d$falsePositive / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$falseNegativePct = d$falseNegative / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$successPct = d$success / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
print(d)
pdf(file=paste(args[2],"/continuous.pdf",sep=""), width=5, height=5)
d2 <- d[d$restriction == "continuous",c("family", "restriction", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative")) + theme(legend.position = c(0.2, 0.8), legend.title = element_blank())

pdf(file=paste(args[2],"/discrete.pdf",sep=""), width=5, height=5)
d2 <- d[d$restriction == "discrete" ,c("family", "restriction", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative")) + theme(legend.position = c(0.2, 0.8), legend.title = element_blank())

