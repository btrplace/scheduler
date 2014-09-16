#!/usr/bin/env Rscript
library(ggplot2)
library(reshape)

#plot
args <- commandArgs(T)
pdf(file=paste(args[2]), width=8, height=4)
par(mar=c(3, 3.2, 1, 1),mgp=c(1.8,0.6,0), cex=1.3)
data <- read.table(args[1], header=F)

#Keep only meaningful columns
data <- data[data$V4 != "success" & data$V4 != "failure", c("V9","V11", "V12","V13","V14")];
colnames(data) <- c("elements","raw_arity", "nodes","VMs", "arity")
arity <- data[,c("raw_arity","arity")]
arity <- aggregate(arity, by=list(arity$raw_arity), FUN=mean, na.rm=TRUE)
arity$variable = "arity"
arity <- arity[, c("raw_arity","arity","variable")]
colnames(arity) = c("elements", "value", "variable")

data <- data[,c("elements","nodes","VMs")]
data <- aggregate(data,by=list(data$elements), FUN=mean, na.rm=TRUE)
data <- data[, c("elements","nodes","VMs")]

data <- melt(data,id="elements")

d <- rbind(data,arity)
colnames(d) <- c("elements","type","value")
p <- ggplot(d, aes(x=d$elements,y=d$value, group=d$variable, colour=type, name="type")) + geom_line() + geom_point()
p + ylim(0, 3) + theme_bw() + xlab("elements") + ylab("retained") + theme(legend.position = c(0.1, 0.75))
foo <- dev.off()