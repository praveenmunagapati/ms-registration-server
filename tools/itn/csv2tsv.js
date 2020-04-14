#! node

var $ = require("./util");	// apply(), isFunction(), etc
var FS = require("fs");
var ASSERT = require("assert");
var TSV = require("./tsv");
var out = process.stdout;
var err = process.stderr;


(function()
{
	var args = process.argv;
	args.shift();
	args.shift();
	
	var filenameIN = args[0];
	if (!$.endsWith(filenameIN,".csv"))
	{
		err.write("file does not end with .csv\n");
		return;
	}
	var filenameOUT = filenameIN.substring(0,filenameIN.length-4) + ".txt";
out.write(filenameIN + " -> " + filenameOUT + "\n");	
	var data = TSV.parseFile(filenameIN, {delim:','});
	var ws = FS.createWriteStream(filenameOUT, {flags:'w', encoding:'utf8'});
	data.print(ws);
	ws.end();
})();

