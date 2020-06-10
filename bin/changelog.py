#!/usr/bin/env python3
#Script to manipulate the changelog: create a new entry or put a timestamp on an entry
import re
import sys
import version

from datetime import date

def newChangelog(v):
	f = open("CHANGES.md")
	lines = []
	headerRead = False
	for line in f:
		if not headerRead and line=="\n":
			headerRead=True
		elif headerRead:
			if re.match("^version "+v, line):
				print("A log entry already exists for version '%s'" % v, file=sys.stderr)
				return False
			lines.append(line)
	f.close()

	f = open("CHANGES.md", "w")
	f.write("Release notes\n")
	f.write("======================\n\n")

	f.write("version %s - soon come\n" %v)
	f.write("----------------------\n")
	f.write("See milestone [%s](https://github.com/btrplace/scheduler/milestones/%s)\n\n" %(v,v))
	for l in lines:
		f.write(l)
	f.write("\n\n")
	f.close()

def timestamp(v):
	f = open("CHANGES.md")
	lines = []
	found=False
	now = date.today()
	for line in f:
		if re.match("^version "+v, line):
			found=True
			line = re.sub("^.+-.+$", "version " + v + " - " + now.strftime("%d %b %Y"), line)
		lines.append(line)
	f.close()

	if not found:
		print("No log entry for version '%s'" % v, file=sys.stderr)
		return False

	f = open("CHANGES.md", "w")
	for l in lines:
		f.write(l)
	f.close()
	return True

def getLog(v):
	f = open('CHANGES.md', 'r')
	cnt=""
	while True:
		line = f.readline()
		if not line: break
		if re.match("version "+v, line):
			f.readline() # skip the ####
			while True:
				log = f.readline()
				if not log or re.match("version ", log):
					return cnt.rstrip()
				cnt += log
	return False

def usage():
		print("Usage %s [new|timestamp] version?" % sys.argv[0], file=sys.stderr)
		exit(1)

####### ---------- MAIN ------------- ################
if __name__ == "__main__":

	if (len(sys.argv) < 2):
		usage()

	v=""
	if len(sys.argv) > 2:
		v = sys.argv[2]
	else:
	#the timestamp is the current version. Might be a snapshot but not my problem
		v = version.parseVersion()
		if not v:
			exit(1)

	op = sys.argv[1]
	if (op == "new"):
		if not newChangelog(v):
			exit(1)
	elif (op == "timestamp"):
		if not timestamp(v):
			exit(1)
		print("Changelog timestamped to version " + v)
	elif (op == "log"):
	    log = getLog(sys.argv[2])
	    if log:
	        print(log)
	    else:
	        exit(1)
	else:
		print("Unsupported operation '%s'" % op, file=sys.stderr)
		exit(1)
