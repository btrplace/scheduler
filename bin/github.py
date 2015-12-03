#!/usr/bin/env python
from __future__ import print_function
import sys
import requests
import os
import re
from bin import version


REPOS = "btrplace/scheduler"
TAG_HEADER = "btrplace-scheduler-"

def header():
	return "{'Content-Type' : 'application/json', 'Authorization':'token %s'}" % os.environ.get('GH_TOKEN')

def api():
	return "https://api.github.com/repos/" + REPOS 


def getRelease(tag):
	r = requests.get(api() + "/releases/tags/%s%s" %(TAG_HEADER,tag))
	if r.status_code == 200:
		return r.json()
	print ("Unable to get the release object:\n" + r.text, file=sys.stderr)
	return False	

def pushChanges(r, changes):	
	data = changes.translate(str.maketrans({"-":  r"\-",
                                          "]":  r"\]",
                                          "\\": r"\\",
                                          "^":  r"\^",
                                          "$":  r"\$",
                                          "*":  r"\*",
                                          ".":  r"\."}))
	dta = "{\"draft\":false, body: \"%s\"}" %data
	r = requests.patch(api() + "/releases/%s" %  r["id"], data=dta, headers= header())
	if r.status_code == 200:
		return True
	print("Error %d: %s" % (r.status_code, r.text), file=sys.stderr)

def getMilestoneId(v):
	res = requests.get(api() + "/milestones")
	if res.status_code != 200:
		print("ERROR %d\n:%s" % (res.status_code, res.text), file=sys.stderr)
		return False
	for ms in res.json():
		if ms["title"] == v:
			return ms		
	return False	

def openMilestone(v):
	req = "{\"title\": \"%s\"}" % v	
	res = requests.post(api() + "/milestones", data=req, headers=header())	
	if res.status_code == 201:
		return True
	else:
		print("ERROR %d\n:%s" % (res.status_code, res.text), file=sys.stderr)
		return False
	
def closeMilestone(ms):
	if (ms["open_issues"] != 0):
		print ("DENIED: %d open issue(s)" % ms["open_issues"], file=sys.stderr)	
		return False				
	req = requests.patch(api() + "/milestones/%d" % ms["number"], headers=header(), data="{\"state\": \"closed\"}")	
	if req.status_code != 200:
		print ("ERROR %s:\n%s" % (req.status_code, req.text), file=sys.stderr)	
		return False

def getLog(v):	
	f = open('CHANGES.md', 'r')
	cnt=""	
	for line in f:		
		if re.match("version "+v, line):									
			f.readline()
			while True:
				log = f.readline()
				if not log or log == "\n":					
					return cnt
				cnt += log
	return False

def usage():
		print("Usage %s [milestone-open|milestone-close|push-changelog] version?" % sys.argv[0], file=sys.stderr)
		exit(1)			

####### ---------- MAIN ------------- ################
if __name__ == "__main__":

	if (len(sys.argv) == 1):
		usage()

	op = sys.argv[1]

	if not os.environ.get('GH_TOKEN'):
		print("Environment variable GH_TOKEN missing", file=sys.stderr)
		exit(1)

	v=""
	if len(sys.argv) > 2:
		v = sys.argv[2]
	else:
		v = version.parseVersion()		
		if not v:
			exit(1)
	
	if (op == "milestone-open"):
		print("Opening milestone %s" % v, file=sys.stderr)	
		openMilestone(v)	
	elif (op == "milestone-close"):
		ms = getMilestoneId(v)
		if not ms:
			print("ERROR: milestone '%s' unknown" % v, file=sys.stderr)
			exit(1)
		if not closeMilestone(os.environ['GH_TOKEN'], ms):
			exit(1)
	elif (op =="push-changelog"):
		log = getLog(v)
		if not log:
			print("No log for version '" + v + "'", file=sys.stderr)
			exit(1)
		print("Captured log:")		
		print(log)		
		
		r = getRelease(v)
		if not r:
			exit(1)		
		if not pushChanges(r, log):
			exit(1)			
	else:
		print("Unsupported operation '" + op + "'", file=sys.stderr)
		usage()		

		