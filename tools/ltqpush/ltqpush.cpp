// ltqpush.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

const char logName[] = "ltqpush.log";
const char logTempName[] = "ltqpush-processing.log";
const char cometDefName[] = "comet.def";
const char rawFilter[] = "*.raw";
const char datFilter[] = "*.dat";
const char allFilter[] = "*.*";
const char rawDir[] = "raw";
const char datDir[] = "dat";

/////////////////////////////////////////////////////////////////////////////
// Gather a list of directories to be converted before doing the
// conversion.  Conversion can take a long time, and this allows
// the list to be built quickly between MS runs.

struct ConvertNode
{
	ConvertNode::ConvertNode(const char* srcDir, const char* destDir,
		ConvertNode* nextNode)
	{
		src = strdup(srcDir);
		dest = strdup(destDir);
		next = nextNode;
	}

	ConvertNode::~ConvertNode()
	{
		free((void*)src);
		free((void*)dest);
	}

	const char* src;
	const char* dest;
	ConvertNode* next;
};

/////////////////////////////////////////////////////////////////////////////
// Utility functions

char* pathcat(const char* dir, const char* filter)
{
	char* ret = (char*) malloc(strlen(dir) + strlen(filter) + 2);
	strcpy(ret, dir);
	strcat(ret, "\\");
	strcat(ret, filter);
	return ret;
}

bool exist(const char* path)
{
	_finddata_t fd;
	long l = _findfirst(path, &fd);
	if (l == -1)
		return false;
	_findclose(l);
	return true;
}

bool exist(const char* dir, const char* filter)
{
	char* filterPath = pathcat(dir, filter);
	bool ret = exist(filterPath);
	free(filterPath);
	return ret;
}

void ensure_dir(const char* dir)
{
	if (dir == NULL || exist(dir))
		return;

	char* pch = strrchr(dir, '\\');

	// If not in current directory, or at drive root.
	if (pch != NULL && (pch == dir || *(pch - 1) != ':'))
	{
		// Make sure parent exits.
		char* parent = strdup(dir);
		parent[pch - dir] = '\0';

		ensure_dir(parent);

		free(parent);
	}

	// Create the directory.
	_mkdir(dir);
}

/////////////////////////////////////////////////////////////////////////////
//	Checks for important files

bool hasRaw(const char* dir)
{	return exist(dir, rawFilter); }
bool hasLog(const char* dir)
{	return exist(dir, logName); }
bool hasCometDef(const char* dir)
{	return exist(dir, cometDefName); }

/////////////////////////////////////////////////////////////////////////////
//	Helper functions for parts of file name
//		Format: <date - MMDDYY>_<Sample ID>[_ID<Experiment ID>][_<3-letter comet spec>]

char* getDefSpec(const char* dir)
{
	char* defSpec = strrchr(dir, '_');
	if (defSpec == NULL)
		return NULL;
	defSpec++;
	if (strlen(defSpec) != 3)
		return NULL;
	return strdup(defSpec);
}

// Assumes the path has a def spec.
char* truncDefSpec(char* path)
{
	char* defSpec = path + strlen(path) - 4;
	*defSpec = '\0';
	return path;
}

/////////////////////////////////////////////////////////////////////////////
//	Do the work of conversion from .dat to .raw

int convert(const char* src, const char* dest, const char* def)
{
	// Open log file.
	char* logTempPath = pathcat(src, logTempName);
	FILE* log = fopen(logTempPath, "w");

	// Write time and date of conversion.
    char timeBuf[128];
	char dateBuf[128];
    _strtime(timeBuf);
    _strdate(dateBuf);
	printf("%s %s\n", timeBuf, dateBuf);
	fprintf(log, "%s %s\n", timeBuf, dateBuf);

	// Write conversion start header.
	printf("Converting %s to %s...\n", src, dest);
	fprintf(log, "Converting %s to %s...\n", src, dest);

	// Get the directory name.
	const char* dirName = strrchr(src, '\\');
	if (dirName == NULL)
		dirName = src;
	else
		dirName++;
	char* defSpec = getDefSpec(dirName);


	// Delete anything currently at the destination.
	_spawnlp(_P_WAIT, "cmd", "/q", "/c", "rd", "/q", "/s", dest, ">nul", "2>nul", NULL);

	// Make sure the destination diretory exists.
	ensure_dir(dest);

	_finddata_t fd;

	// Convert all the .raw files with the same root name as the
	// directory itself.
	char* filterPath = (char*) malloc(strlen(src) + strlen(dirName) + strlen(rawFilter) + 2);
	strcpy(filterPath, src);
	strcat(filterPath, "\\");
	strcat(filterPath, dirName);
	if (defSpec != NULL)
		truncDefSpec(filterPath);
	strcat(filterPath, rawFilter);
	long l = _findfirst(filterPath, &fd);
	free(filterPath);

	assert(l != 0); // No raw files found.

	int lenSrc = strlen(src) + 1;
	char* srcPath = (char*) malloc(lenSrc + sizeof(fd.name));
	strcpy(srcPath, src);
	strcat(srcPath, "\\");

	char* destRaw = pathcat(dest, rawDir);
	ensure_dir(destRaw);
	char* destDat = pathcat(dest, datDir);
	ensure_dir(destDat);

	long status = 0;

	// Attempt to copy a comet.def file into the dat destination directory.
	char* defdir = strdup(src);

	if (def != NULL)
	{
		// Comet def spec should be at the end of the directory name.
		if (defSpec != NULL)
		{
			defdir = pathcat(def, defSpec);
			free(defSpec);
		}
	}

	if (exist(defdir, cometDefName))
	{
		char* defFile = pathcat(defdir, cometDefName);
		status =
			_spawnlp(_P_WAIT, "cmd", "/c", "copy", defFile, destDat, ">nul", "2>nul", NULL);
		free(defFile);

		if (status == 0)
		{
			printf("Copied comet.def from %s\n", defdir);
			fprintf(log, "Copied comet.def from %s\n", defdir);
		}
		else
		{
			printf("ERROR: Unable to copy comet.def from %s\n\n", defdir);
			fprintf(log, "ERROR: Unable to copy comet.def from %s\n", defdir);
		}
	}
	else
	{
		printf("ERROR: Missing comet.def in %s\n\n", defdir);
		fprintf(log, "ERROR: Missing comet.def in %s\n", defdir);
		status++;
	}

	free(defdir);

	if (status != 0)
	{
		fclose(log);
		free(srcPath);
		free(destRaw);
		free(destDat);
		free(logTempPath);
		return status;
	}

	do
	{
		strcpy(srcPath + lenSrc, fd.name);

		printf("Converting %s... ", fd.name);
		fprintf(log, "Converting %s... ", fd.name);

		fflush(log);

		// Need to use "cmd /c" to run XConvert, or it runs in detaches
		// immediately, and then runs as a background process.

		long statSpawn =
			_spawnlp(_P_WAIT,
				"cmd",
				"/c",
				"XConvert.exe",
				"/SL",
				"/DI",
				srcPath,
				"/O",
				src,
				NULL);

		// Convert locally and then copy, because converting to dest
		// was seeing occasional write failures when dest was network
		// drive.

		if (statSpawn == 0)
		{
			printf("copy... ");
			fprintf(log, "copy... ");

			// Copy raw file
			statSpawn += _spawnlp(_P_WAIT, "cmd", "/c","copy", srcPath, destRaw, ">nul", "2>nul", NULL);

			strcpy(srcPath + lenSrc, datFilter);

			// Copy dat file
			statSpawn += _spawnlp(_P_WAIT, "cmd", "/c","copy", srcPath, destDat, ">nul", "2>nul", NULL);
		}
		else if (statSpawn == -1)
		{
			status++;
			if (errno == ENOENT)
			{
				printf("\nERROR: XConvert.exe not found.\nCheck your path.\n");
				exit(2);
			}
			else
			{
				// CONSIDER: exit?
				printf("ERROR: Unable to run XConvert.exe.\n");
				fprintf(log, "ERROR: Unable to run XConvert.exe.\n");
			}
		}

		if (statSpawn == 0)
		{
			printf("success\n");
			fprintf(log, "success\n");
		}
		else
		{
			status += statSpawn;
			printf("ERROR\n");
			fprintf(log, "ERROR\n");
		}

		// Remove any leftover .dat files.
		strcpy(srcPath + lenSrc, datFilter);
		_spawnlp(_P_WAIT, "cmd", "/c","del", "/f", srcPath, ">nul", "2>nul", NULL);

		fflush(log);
	}
	while (_findnext(l, &fd) == 0);

	if (status == 0)
	{
		printf("Success\n");
		fprintf(log, "Success\n");
	}
	else
	{
		printf("Errors\n");
		fprintf(log, "Errors\n");
	}

    _strtime(timeBuf);
    _strdate(dateBuf);
	printf("%s %s\n\n", timeBuf, dateBuf);
	fprintf(log, "%s %s\n\n", timeBuf, dateBuf);

	_findclose(l);
	free(srcPath);
	free(destRaw);
	free(destDat);

	fclose(log);

	if (status == 0)
	{
		char* logPath = pathcat(src, logName);
		rename(logTempPath, logPath);
		free(logPath);
	}

	free(logTempPath);

	return status;
}

/////////////////////////////////////////////////////////////////////////////
//	Collect the list of directories to be converted.

ConvertNode* mirror(const char* src, const char* dest,
					ConvertNode* convertList)
{
	// If this directory has .raw files, then it is a leaf directory.
	if (hasRaw(src))
	{
		// If it already has a log file, then it has been converted.
		if (hasLog(src))
		{
			printf("Skipping %s - %s found\n", src, logName);
			return convertList;
		}

		// Add to the list.
		printf("Queuing %s - for conversion\n", src);
		return new ConvertNode(src, dest, convertList);
	}

	// Recurse into subdirectories.
	_finddata_t fd;

	char* filterPath = pathcat(src, allFilter);
	long l = _findfirst(filterPath, &fd);
	free(filterPath);

	if (l == -1)
	{
		printf("ERROR: Could not find source directory %s.\n", src);
		exit(2);
	}

	int lenSrc = strlen(src) + 1;
	int lenDest = strlen(dest) + 1;
	char* srcSub = (char*) malloc(lenSrc + sizeof(fd.name));
	strcpy(srcSub, src);
	strcat(srcSub, "\\");
	char* destSub = (char*) malloc(lenDest + sizeof(fd.name));
	strcpy(destSub, dest);
	strcat(destSub, "\\");

	do
	{
		if ((fd.attrib & _A_SUBDIR) == 0)
			continue;
		else if (strcmp(fd.name, ".") == 0 ||
				strcmp(fd.name, "..") == 0)
			continue;

		strcpy(srcSub + lenSrc, fd.name);
		strcpy(destSub + lenDest, fd.name);

		convertList = mirror(srcSub, destSub, convertList);			
	}
	while (_findnext(l, &fd) == 0);

	_findclose(l);
	free(srcSub);
	free(destSub);

	return convertList;
}

int main(int argc, char* argv[])
{
	const char* src = NULL;
	const char* dest = NULL;
	const char* def = NULL;

	bool usage = false;

	for (int i = 1; i < argc; i++)
	{
		char* pch = argv[i];
		if (*pch == '/')
		{
			pch++;
			if ((*pch == 'D' || *pch == 'd') && *(pch + 1) == '\0')
			{
				i++;
				if (i < argc)
					def = argv[i];
				else 
					usage = true;
				continue;
			}
		}

		if (src == NULL)
			src = argv[i];
		else if (dest == NULL)
			dest = argv[i];
		else
			usage = true;
	}

	if (usage || src == NULL || dest == NULL)
	{
		printf("USAGE: %s [/d <comet-def dir>] <source dir> <dest dir>\n", argv[0]);
		printf("\tMirrors the directory structure in <source dir>\n");
		printf("\tto <dest dir>, and converts any .raw files found\n");
		printf("\tin <src dir> to .dat files in <dest dir>.\n\n");

		printf("\tDirectories in <src dir> with a %s file\n", logName);
		printf("\tare excluded.\n\n");

		printf("\tSpecify a comet-def directory using /d.  If one is present,\n");
		printf("\tdirectories with .raw files will be checked for a 3-letter\n");
		printf("\tcomet.def specification, and a comet.def file will be\n");
		printf("\tcopied from the directory to the dest dir.\n\n");

		printf("\tThe comet-def directory can also be specified by setting\n");
		printf("\tCOMET_DEF_LIB environment variable.\n");
		return 1;
	}

	if (def == NULL)
		def = getenv("COMET_DEF_LIB");

	int status = 0;

	ConvertNode* convertList = mirror(argv[1], argv[2], NULL);
	if (convertList != NULL)
	{
		printf("\n");

		// Reverse the list, so processing happens in directory list order.
		ConvertNode* newList = NULL;
		while (convertList != NULL)
		{
			ConvertNode* node = convertList->next;
			convertList->next = newList;
			newList = convertList;
			convertList = node;
		}
		convertList = newList;

		// Convert everything.
		while (convertList != NULL)
		{
			ConvertNode* node = convertList;
			convertList = convertList->next;

			status += convert(node->src, node->dest, def);

			delete node;
		}
	}

	return status;
}
