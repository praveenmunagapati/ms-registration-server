# D(group, lv, cd4, d8)

report.DeviationFromMean <- function(D, tsvFile, pngFile)
	{
	names <- c("lv","cd4","cd8")
	means <- paste(names,"mean",sep=".")
	diffs <- paste(names,"diff",sep=".")

	#
	# Compute summary statistics
	#
	
	D <- mergeMean(D, D["group"], names)
	#D <- mergeMedian(D, D["group"], names)
	D <- mergeDiffs(D, names, means, diffs)
	D <- mergeCount(D, D["group"])
	
	#
	# some formatting
	#
	
	D <- data.frame( D,
	        lv.diff.style=iif(abs(D$lv.diff/D$lv.mean)>.1,"","color:red;"),
	        cd4.diff.style=iif(abs(D$cd4.diff/D$cd4.mean)>.1,"","color:red;"),
	        cd8.diff.style=iif(abs(D$cd8.diff/D$cd8.mean)>.1,"","color:red;"),
	        verdict=paste(iif(abs(D$lv.diff/D$lv.mean)>.1,"","Live"),iif(abs(D$cd4.diff/D$cd4.mean)>.1,"","CD4"),iif(abs(D$cd8.diff/D$cd8.mean)>.1,"","CD8"),sep=" ")
	        )
	
	#
	# results
	#
	
	png(filename=pngFile, width=1024, height=(60+20*(length(levels(D$group)))))
	layout(matrix(1:3,ncol=3))
	boxplot(lv ~ group, D, horizontal=TRUE, col="blue", main="Live", las=1)
	boxplot(cd4 ~ group, D, horizontal=TRUE, col="blue", main="CD4", las=1)
	boxplot(cd8 ~ group, D, horizontal=TRUE, col="blue", main="CD8", las=1)
	
	dev.off()
	
	write.table(data.round(D), file=tsvFile, sep="\t", qmethod="double", col.names=NA)
	}
