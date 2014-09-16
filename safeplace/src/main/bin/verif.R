#!/usr/bin/env Rscript
library(ggplot2)
library(reshape2)

#plot
args <- commandArgs(T)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Keep only meaningful columns
data <- data[data$V9==5, c("V1","V2","V3","V4")];
colnames(data) <- c("cstr","restriction","against","res")
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
a <- aggregate(data,by=list(data$kind,data$res, data$against, data$restriction),FUN=length)
a <- a[,c("Group.1","Group.2","Group.3", "Group.4", "cstr")]
colnames(a) <- c("family","kind","against", "restriction", "value")
#print(a)
d <- dcast(a, family + against + restriction ~ kind , value.var="value", na.rm = TRUE)
d[is.na(d)] <- 0
#to pct
d$failurePct = d$failure / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$falsePositivePct = d$falsePositive / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$falseNegativePct = d$falseNegative / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100
d$successPct = d$success / (d$falseNegative + d$falsePositive + d$success + d$failure) * 100

pdf(file=paste(args[2],"/continuous_repair.pdf",sep=""), width=8, height=4)
d2 <- d[d$restriction == "continuous" & d$against=="impl_repair" ,c("family", "restriction", "against", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction","against"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative"))

pdf(file=paste(args[2],"/continuous_rebuild.pdf",sep=""), width=8, height=4)
d2 <- d[d$restriction == "continuous" & d$against=="impl_rebuild" ,c("family", "restriction", "against", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction","against"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative"))

pdf(file=paste(args[2],"/discrete_repair.pdf",sep=""), width=8, height=4)
d2 <- d[d$restriction == "discrete" & d$against=="impl_repair" ,c("family", "restriction", "against", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction","against"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative"))

pdf(file=paste(args[2],"/discrete_rebuild.pdf",sep=""), width=8, height=4)
d2 <- d[d$restriction == "discrete" & d$against=="impl_rebuild" ,c("family", "restriction", "against", "failurePct","falsePositivePct","falseNegativePct")]
a <- melt(d2, id=c("family","restriction","against"))
ggplot(a, aes(x=a$family, y=a$value, fill=a$variable)) + geom_bar(stat="identity") + xlab("Constraint family") + ylab("%") + ylim(c(0,75)) + theme_bw() + scale_fill_discrete(name="Error type", labels=c("runtime failure", "false positive", "false negative"))


