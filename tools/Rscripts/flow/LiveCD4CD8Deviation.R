path <- "/usr/local/webroot/Shared/Rscripts/flow"
showRun <- "https://flow.labkey.org/demo/flow-run/Duke/Demo%20Analysis%20of%20PV1/showRun.view?runId"
showWell <- "https://flow.labkey.org/demo/flow-well/Duke/Demo%20Analysis%20of%20PV1/showWell.view?wellId"


source(paste(path,"FlowLibrary.R",sep="/"))
source(paste(path,"Reports.R",sep="/"))

filter <- labkey.data$fcsfile_keyword_sample_order != 'Comp' & labkey.data$fcsfile_keyword_sample_order != 'PBS'

D <- data.frame(
        runid=labkey.data$run_rowid[filter],
	run=labkey.data$run_name[filter],
        run.href=paste(showRun,labkey.data$run_rowid[filter],sep="="),
        wellid=labkey.data$rowid[filter],
        well=labkey.data$name[filter],
#        well.href=paste(showWell,labkey.data$rowid[filter],sep="="),
        group=paste(labkey.data$run_rowid[filter], labkey.data$fcsfile_keyword_sample_order[filter], sep=":"),
#        sample=paste(labkey.data$run_rowid[filter], labkey.data$fcsfile_keyword_sample_order[filter], sep=":"),
        comment=labkey.data$flag_comment[filter],
        order=labkey.data$fcsfile_keyword_sample_order[filter],
        lv=labkey.data$statistic_s_slv_freq_of_parent[filter],
        cd4=labkey.data$statistic_s_slv_sl_s3__s4__freq_of_paren[filter],
        cd8=labkey.data$statistic_s_slv_sl_s3__s8__freq_of_paren[filter]
        )
# order matters
pngFile <- "${imgout:labkeyl_png}"
tsvFile <- "${tsvout:tsvfile}"
report.DeviationFromMean(D, tsvFile, pngFile )
