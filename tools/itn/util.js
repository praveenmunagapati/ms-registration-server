var ASSERT = require("assert");


function apply(o,a)
{
    for (var p in a)
		o[p] = a[p];
	return o;
}

function applyIf(o,a)
{
    for (var p in a)
    	if (typeof o[o] !== 'undefined')
			o[p] = a[p];
	return o;
}


function copy(a)
{
	return apply({}, a);
}


function testCase()
{
	var a = {x : 1, y : 2, z:3};
	apply(a, {w: 0, y:4});
	ASSERT.ok(a.w == 0 && a.x == 1 && a.y == 4 && a.z == 3);
	var b = copy(a);
	b.x=100;
	ASSERT.ok(a.w == 0 && a.x == 1 && a.y == 4 && a.z == 3);
	ASSERT.ok(b.w == 0 && b.x == 100 && b.y == 4 && b.z == 3);
	
	ASSERT.ok(exports.startsWith("abc","a"));
	ASSERT.ok(!exports.startsWith("abc", "abcd"));
	ASSERT.ok(exports.endsWith("abc","bc"));
	ASSERT.ok(!exports.endsWith("abc", "abcd"));
}


apply(exports,
{
	apply : apply,
	applyIf : apply,
	
	isFunction : function(fn)
	{
		var ret = typeof fn == "function" && Object.prototype.toString.apply(fn) == '[object Function]';
		return ret;
	},
	
	isString : function(s)
	{
		return typeof s == "string";
	},
	
	isNumber : function(n)
	{
		return typeof n == "number";
	},
	
	startsWith : function (a,b)
	{
		return a.length >= b.length && a.substring(0,b.length) == b;
	},

	endsWith : function(a,b)
	{
		return a.length >= b.length && a.substring(a.length-b.length) == b;
	},

	testCase : testCase	
});
