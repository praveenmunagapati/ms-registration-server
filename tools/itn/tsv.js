#! /usr/local/bin/node

var $ = require("./util");	// apply(), isFunction(), etc
var FS = require("fs");
var ASSERT = require("assert");


var log = function(s){(process.stderr||process.stdout).write(s+"\n");};


var Results = function()
{
	this.fieldMap = {};
	this.fields = [];
	this.rows = [];
}


$.apply(Results.prototype,
{
	getField : function(field)
	{
		if ($.isString(field))
			field = this.fieldMap[field.toLowerCase()];
		if ($.isNumber(field))
			return this.fields[field];			
		if (typeof field === 'object' && 'name' in field && 'index' in field)
			return field;
		return null;
	},

	print : function print(stream)
	{
		var tsv = this;
		var tab = "";
		var line = [];

		for (var f=0 ; f<tsv.fields.length ; f++)
		{
			if (f) line.push("\t");
			line.push(tsv.fields[f].name);
		}
		line.push("\n");
		stream.write(line.join(''));
		
		this.each(function(row)
		{
			line = [];	
			tab = "";
			for (var f=0 ; f<tsv.fields.length ; f++)
			{
				if (f) line.push("\t");
				if (typeof row[f] !== 'undefined' && row !== null)
					line.push(row[f].toString());
			}
			line.push("\n");
			stream.write(line.join(''));
		});
	},
	
	
	inferTypes : function()
	{
		var types = [];
		for (var i=0 ; i<this.fields.length ; i++)
		{
			types.push(inferType(this,this.fields[i]));
		}
		return types;
	},
	 
	
	convertToIntegerColumn : function(field)
	{
		var count = convertColumn(this, field, function(s) {return s=="" ? null : parseInt(s);});
		if (count == this.rows.length)
			this.getField(field).type='int';
	},
	
	convertToDoubleColumn : function(field)
	{
		var count = convertColumn(this, field, function(s) {return s=="" ? null : parseDouble(s);});
		if (count == this.rows.length)
			this.getField(field).type='double';
	},
	
	convertToDateColumn : function(field)
	{
		var count = convertColumn(this, field, function(s) {return s=="" ? null : parseDate(s);});
		if (count == this.rows.length)
			this.getField(field).type='dateTime';
	},
	

	addColumn : function(name, fn)
	{
		var field = this.getField(name);
		if (field)
			throw "addColumn() Field already exists: " + field.name;
			
		var c = this.fields.length;
		this.fieldMap[name.toLowerCase()] = c;
		this.fields.push({name:name, index:c});
	
		
		if ($.isFunction(fn))
		{
			this.each(function(row)
			{
				row[c] = fn(row);
			});
		}
		else
		{
			this.each(function(row)
			{
				row[c] = fn;
			});
		}
	},
	
	
	renameColumn : function(from, to)
	{
		var field = this.getField(from);
		if (!field)
			throw "renameColumn(), Field not found: " + from;
		var test = this.getField(to);
		if (test)
			throw "renameColumn(): Field already exists: " + to;	
		delete this.fieldMap[field.name.toLowerCase()];
		this.fieldMap[to.toLowerCase()] = field.index;
		field.name = to;
	},
	

	each : function(fn)
	{
        var rows = this.rows;
        var len = rows.length;
		var r;
		for (r=0 ; r<len ; r++)
			fn(rows[r]);
	}
});


/** 
 * TODO : newlines in values
 *
 * return 
 * {
 *		fieldMap : {a:0, b:1, c:2}	// note lowercase names in fieldMap
 *		fields:[{name:'A', index:0, type:'string'}, {name:'B', index:1, type:'dateTime'}, {name:'C', index:2, type:'double'}], 
 *		rows:[[A1, B1, C1], [A2, B2, C2]]
 * } 
 */
function parseFile(f, config)
{
	var file;
	if (typeof f == "string")
		file = FS.readFileSync(f,"utf8");
	else
		file = FS.readSync(f,"utf8");

	return parseText(file, config);
}


var _emptyValue = "";

function _readValue(s)
{
	s = s.trim();
	if (s.length == 0)
		return _emptyValue;
	s = trimQuotes(s);
	if (s == "NaN")
		process.err.write("NaN in input\n");
	return s;
}


function replaceAll(s,a,b)
{
	return s.split(a).join(b);
}


function trimQuotes(s)
{
	var len = s.length;
	if (len >= 2 && s.charAt(0)=='"' && s.charAt(len-1)=='"')
	{
		s = s.substring(1,len-1);
		s = replaceAll(s,'""','"');
	}
	return s;
}


function leftTrimSpace(s,startAt)
{
	if (!s || s.length == 0)
		return '';
	var i;
	for (i=startAt||0 ; i<s.length && s.charAt(i)==' '; i++)
		;
	return s.substring(i);
}


var commaRE = /[ ]*("([^"]|"")*")|([^,]*)/;
var tabRE   = /[ ]*("([^"]|"")*")|([^\t]*)/;

function splitLine(line, config)
{
	var re = config.delim=='\t' ? tabRE : config.delim==','  ? commaRE : null;
	
	var s = line;
	if (!re)
	{
		return s.split(config.delim);
	}
	else
	{
		var a = []; var m, v;
		while (s)
		{
			m=re.exec(s)
			var match = m[0];
			var index = m.index;
			a.push(match);
			var length = s.length;
			s = leftTrimSpace(s,match.length);
			if (s && s.charAt(0) == config.delim)
				s = leftTrimSpace(s,1);
			if (s.length == length)
				throw "ERROR " + line + "\n" + s + "\n";
		}
		return a;
	}
}



function parseText(text, c)
{
	var config = $.apply({delim:'\t'},c||{});
	var lines = text.split("\n");

	var fieldNames = splitLine(lines[0],config);

	var tsv = new Results();
	
	var f;
	for (f=0 ; f<fieldNames.length ; f++)
	{
		var name = trimQuotes(fieldNames[f].trim());
		tsv.fields[f] = {name:name, index:f, type:'string'};
		tsv.fieldMap[name.toLowerCase()] = f;
	}

	var row;
	for (var i=1 ; i<lines.length ; i++)
	{
		if (!lines[i].trim())
			continue;
		row = splitLine(lines[i],config);
		var count = Math.max(row.length,fieldNames.length);
		for (f=0 ; f<row.length ; f++)
		{
			if (f >= row.length)
				row[f] = this._emptyValue;
			else
				row[f] = _readValue(row[f]);
		}
		tsv.rows.push(row);
	}
	return tsv;
}


function convertColumn(tsv, fieldSpec, fn)
{
	var field = tsv.getField(fieldSpec);
	if (!field)
		throw "convertColumn(), Field not found: " + fieldSpec;
	var f = field.index;
	var count = 0;
	for (var i=0 ; i<tsv.rows.length ; i++)
	{
		var row = tsv.rows[i];
		try
		{
			row[f] = fn(row[f]);
			count++;
		}
		catch (x)
		{
		}
	}
	return count;
}



function inferType(tsv, fieldSpec)
{
	var field = tsv.getField(fieldSpec);
	if (!field)
		throw "inferType(), Field not found: " + fieldSpec;
	if (field.type != 'string' || tsv.rows.length==0)
		return field.type;
	var f = field.index;
	var countEmpty = 0;
	var countInteger = 0;
	var countDouble = 0;
	var countDate = 0;
	var countUndefined = 0;
	for (var i=0 ; i<tsv.rows.length ; i++)
	{
		var v = tsv.rows[i][f];
		if (typeof v === 'undefined')
		{
			countUndefined++;
			continue;
		}
		if (isIntegerString(v)) countInteger++;
		if (isDoubleString(v)) countDouble++;
		if (isDateTimeString(v)) countDate++;
	}
	var count = tsv.rows.length - countEmpty;
	if (count == 0)
		return field.type;
	if (countDate == count)
		return 'dateTime';
	if (countInteger == count)
		return 'int';
	if (countDouble == count)
		return 'double';
	return 'string';
}


function isIntegerString(s)
{
    try
    {
        return !isNaN(parseInt(s)) && /^-?[0-9]*$/.test(s);
    }
    catch (x)
    {
        return false;
    }
}


function isDoubleString(s)
{
    try
    {
        return !isNaN(parseFloat(s)) && /^[0-9eE+\.\-]*$/.test(s);
    }
    catch (x)
    {
        return false;
    }
}


function isDateTimeString(s)
{
    try
    {
        if (/^\d+$/.test(s))
            return false;
        var ms = Date.parse(s);
        return !isNaN(ms);
    }
    catch (x)
    {
        return false;
    }
}


function testCase()
{
	ASSERT.ok(!isIntegerString('1.asf'));
	ASSERT.ok(!isIntegerString('1.0'));
	ASSERT.ok(isIntegerString('1234'));
	ASSERT.ok(isIntegerString('-1234'));

	ASSERT.ok(!isDoubleString('1.0asf'));
	ASSERT.ok(isDoubleString('1.0'));
	ASSERT.ok(isDoubleString('-1234.0e-10'));

	ASSERT.ok(!isDateTimeString('001001'));
	ASSERT.ok(isDateTimeString('2001-01-01'));
	ASSERT.ok(isDateTimeString('1 Jan 2001'));
	ASSERT.ok(isDateTimeString('1/1/2011'));
	ASSERT.ok(isDateTimeString('17JAN2007'));
    

	// node.js fails this test
	//ASSERT.equal("ABA".replace("A","X",'g'),"XBX");
	ASSERT.equal(replaceAll("ABA","A","X"),"XBX");
	
	var text = "A\tB\tC\n"+'"A1"\t"B1"\t"1"\n' + "A2\tB2\t2\n";
	// TODO test empty values
	// TODO test CSV
	var tsv = parseText(text);
//	process.stdout.write(JSON.stringify(tsv))
//  test printing to buffer/nul stream

	ASSERT.equal(tsv.getField('a').name, 'A');
	ASSERT.equal(tsv.getField('A').name, 'A');
	ASSERT.equal(tsv.getField('b').index, 1);
	ASSERT.ok( !tsv.getField('x') );
	ASSERT.equal(tsv.rows.length, 2);
	ASSERT.equal(tsv.rows[0][tsv.getField('a').index], 'A1');
	ASSERT.equal(tsv.rows[1][tsv.getField('a').index], 'A2');
	ASSERT.equal(tsv.rows[0][tsv.getField('b').index], 'B1');
	ASSERT.equal(tsv.rows[1][tsv.getField('b').index], 'B2');
	ASSERT.equal(tsv.rows[0][tsv.getField('c').index], '1');
	ASSERT.equal(tsv.rows[1][tsv.getField('c').index], '2');
	tsv.convertToIntegerColumn('c');
	ASSERT.equal(tsv.rows[0][tsv.getField('C').index], 1); 
	ASSERT.equal(tsv.rows[1][tsv.getField('C').index], 2);
	tsv.convertToDoubleColumn("C");
	ASSERT.equal(tsv.rows[0][2], 1.0);
	ASSERT.equal(tsv.rows[1][2], 2.0);
	tsv.addColumn('D', 'XXYZZY');
	ASSERT.equal(tsv.getField('d').name, "D");
	ASSERT.equal(tsv.getField('d').index, 3);
	ASSERT.equal(tsv.rows[1][3], "XXYZZY");
}


testCase();

$.apply(exports, {
	parseFile : parseFile,
	parseText : parseText,
	testCase : testCase,
	STRING : 'string',
	DOUBLE : 'double',
	INTEGER : 'int',
	DATETIME : 'dateTime'
});
