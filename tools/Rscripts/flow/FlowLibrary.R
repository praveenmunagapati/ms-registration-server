iif <- function(cond, f, t) { (c(f,t))[cond+1] }

any <- function(x)
{
   if (length(x) > 0)
      x[1]
   else
   	  c()
}


data.round <- function(D) 
{
    for (i in 1:length(D))
    {
        if ("double" == typeof(D[[i]]))
	      D[[i]] <- round(D[[i]],round=2)
    }
    D
}


# map = list(newname="oldname",...)
data.rename <- function(D, map)
{
    for (j in 1:length(D))
    {
    	name = names(D)[j]
		for (i in 1:length(map))
		{
             if (name == map[[i]])
             {
                 names(D)[j] <- names(map)[i]
                 break;
             }	
        }
	}
	D
}


#
# Grouping methods
#

mergeMean <- function(D, by, cols)
{
	SUM <- aggregate(D[,cols], by, mean)
	merge(D,SUM,names(by),suffixes=c("",".mean"))
}

mergeMedian <- function(D, by, cols)
{
	SUM <- aggregate(D[,cols], by, median)
	merge(D,SUM,names(by),suffixes=c("",".median"))
}

mergeCount <- function(D, by)
{
	data.frame(D,count=ave(D$cd4,by,FUN=length))
}

mergeDiffs <- function(D, x, y, cols)
{
	DIFFS <- data.frame(D[x] - D[y])
	names(DIFFS) <- cols
	data.frame(D, DIFFS)
}
