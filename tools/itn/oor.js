#! node

var $ = require("./util");	// apply(), isFunction(), etc
var FS = require("fs");
var ASSERT = require("assert");
var TSV = require("./tsv");
var out = process.stdout;
var err = process.stderr;



function log(s)
{
	process.stdout.write(s + "\n");
}

function defined(x)
{
	return $.isString(x) || $.isNumber(x);
}


function pad3(s)
{
	if (s.length >= 3) return s;
	return "000".substr(s.length) + s;
}



function reformatBarcode(s)
{
    if (/\d{6}.*/.test(s))
    {
        s = s.replace("-","");
        return s.substring(0,6) + "-" + s.substring(6);
    }
    return s;
}



function sequenceNumFromVisit(visit, visitNum)
{
	var Unassigned = 0;
	var Unscheduled = 8000;
	var Enrollment = 1000;
	var Archived = 9900;
	var High = 2000;
	var Med = 3000;
	var R = 5000;
	var Low = 9000;
	var Reject = 9100;
	
	if (defined(visit))
	{
		visit = visit.trim();
		if (visit == "Baseline Average")
			return Unassigned;
		if ($.startsWith(visit,"Month -1 (archived)"))
			return Archived;
		if ($.startsWith(visit,"Month 0 (archived)"))
			return Archived + 1;
		if (visit == "R")
			return R;
		if ($.startsWith(visit,"Month R M"))
			return Reject + parseInt(visit.substring("Month R M".length));
		if ($.startsWith(visit,"Month H"))
			return High + 10*parseInt(visit.substring("Month H".length));
		if ($.startsWith(visit,"Month M"))
			return Med + parseInt(visit.substring("Month M".length));
		if ($.startsWith(visit,"Month L"))
			return Low + parseInt(visit = visit.substring("Month L".length));
		if ("Pre-enrollment -2" == visit)
			return Enrollment;
		if ("Pre-enrollment -1" == visit)
			return Enrollment+1;
		if ("Unscheduled" == visit)
			return Unscheduled;
	}

	if (defined(visitNum))
	{
		if ($.isString(visitNum))
		{
			visitNum = visitNum.toString();
			var firstChar = visitNum.length > 0 ? visitNum.charAt(0) : '';
			if (firstChar == 'H')
				return High + 10*parseInt(visitNum.substring(1));
			if (firstChar == 'M')
				return Med + parseInt(visitNum.substring(1));
			if (firstChar == 'L')
				return Low + parseInt(visitNum.substring(1));
			if ("UN" == visitNum)
				return Unscheduled;
			visitNum = parseInt(visitNum);
		}
		if (visitNum < 0)
			return Enrollment + 2 + visitNum;
		return High + 10 * visitNum;
	}
	return Unassigned;
}



function testCase()
{
	ASSERT.equal(sequenceNumFromVisit("Pre-enrollment -2"), 1000);
	ASSERT.equal(sequenceNumFromVisit("Pre-enrollment -1"), 1001);
	ASSERT.equal(sequenceNumFromVisit("Month H1"), 2010);
	ASSERT.equal(sequenceNumFromVisit("Month M1"), 3001);
	ASSERT.equal(sequenceNumFromVisit("R"), 5000);
	ASSERT.equal(sequenceNumFromVisit("Month R M1"), 9101);
	ASSERT.equal(sequenceNumFromVisit("Month R M10"), 9110);
	ASSERT.equal(sequenceNumFromVisit("Month L2"), 9002);
	ASSERT.equal(sequenceNumFromVisit("Month H0"), 2000);
	ASSERT.equal(sequenceNumFromVisit("Month 0 (archived)"), 9901);
	ASSERT.equal(sequenceNumFromVisit("Unscheduled"), 8000);

	ASSERT.equal(sequenceNumFromVisit(null,"-2"), 1000);
	ASSERT.equal(sequenceNumFromVisit(null,"-1"), 1001);
	ASSERT.equal(sequenceNumFromVisit(null,"0"), 2000);
	ASSERT.equal(sequenceNumFromVisit(null,"1"), 2010);
	ASSERT.equal(sequenceNumFromVisit(null,"24"), 2240);
	ASSERT.equal(sequenceNumFromVisit(null,"M1"), 3001);
	ASSERT.equal(sequenceNumFromVisit(null,"M12"), 3012);
	ASSERT.equal(sequenceNumFromVisit(null,"M20"), 3020);
	ASSERT.equal(sequenceNumFromVisit(null,"UN"), 8000);
        
    ASSERT.equal(reformatBarcode("914233A03"), "914233-A03");
    ASSERT.equal(reformatBarcode("914233-A03"), "914233-A03");
    ASSERT.equal(reformatBarcode("914233A-03"), "914233-A03");
}



function usage()
{
    process.stdout.write("node " + process.argv[1] + " datafile.txt OORColumnName > output/datafile.txt\n");
}


if (process.argv.length == 2)
{
    usage();
	$.testCase();
	TSV.testCase();
	testCase();
    process.stdout.write("tests passed.\n");
}
else
{

    var args = process.argv;
    args.shift();
    args.shift();

    var filenameIN = args.shift();

    while (args.length)
    {
        var columnName = args.shift();
        
        err.write(filenameIN + "...");
        var config = {delim:'\t'};
        if ($.endsWith(filenameIN,".csv"))
            config.delim = ",";
        var tsv = TSV.parseFile(filenameIN, config);


        var field = tsv.getField(columnName);
        if (!field)
        {
            err.write("field not found: " + columnName);
            process.exit(1);
        }
        
        tsv.addColumn(columnName + "OORIndicator", function(row)
        {
            var v = row[field.index];
            if (!v)
                return "";
            var oor = v.charAt(0);
            if (oor == "<" || oor == ">")
            {
                row[field.index] = v.substring(1);
                return oor;
            }
            return "";
        });
    }

    tsv.print(process.stdout);
    err.write(" " + tsv.rows.length + "\n");
}
