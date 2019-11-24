#!/usr/bin/env python
from __future__ import print_function
import sys
import ssl
ssl.HAS_SNI = False
import requests
import urllib3
urllib3.disable_warnings()
import os
import re
import version

REPOS = "btrplace/scheduler"
TAG_HEADER = "btrplace-scheduler-"

def header():
	return {'Content-Type' : 'application/json', 'Authorization':'token %s' %os.environ.get('GH_TOKEN')}

def api():
	return "https://api.github.com/repos/" + REPOS

def createRelease(tag, changes):
	dta = {
	"draft" : False,
	"tag_name" : TAG_HEADER + tag,
	"name": tag,
	"body": changes,
	}
	r = requests.post(api() + "/releases", json=dta, headers=header())
	if r.status_code == 201:
		return True
	print("ERROR %d\n:%s" % (r.status_code, r.text), file=sys.stderr)
	return False


def getRelease(tag):
	r = requests.get(api() + "/releases/tags/%s%s" %(TAG_HEADER,tag), headers=header())
	if r.status_code == 200:
		return r.json()
	print ("Unable to get the release object '%s%s': %d\n%s" % (TAG_HEADER,tag, r.status_code, r.text), file=sys.stderr)
	return False

def pushChanges(r, changes):
	data = changes.replace('"', '\\"')
	dta = {"draft":False, "body": data}
	r = requests.patch(api() + "/releases/%s" %  r["id"], json=dta, headers= header())
	if r.status_code == 200:
		return True
	print("ERROR %d: %s" % (r.status_code, r.text), file=sys.stderr)

def getMilestoneId(v):
	res = requests.get(api() + "/milestones?state=all", headers=header())
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
	if (ms["state"] == "closed"):
		return True
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
	cpt=0
	while True:
		line = f.readline()
		if not line: break
		if re.match("version "+v, line):
			f.readline()
			while True:
				log = f.readline()
				#2 consecutive empty lines == we stop
				if not log or log == "\n":
					cpt=cpt+1
					if cpt == 2:
						return cnt
				else:
					cpt = 0
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
		if not closeMilestone(ms):
			exit(1)
	elif (op =="push-changelog"):
		r = getRelease(v)
		if not r:
			exit(1)
		log = getLog(v)
		if not log:
			print("No log for version '" + v + "'", file=sys.stderr)
			exit(1)
		print("Captured log:")
		print(log)
		if not pushChanges(r, log):
			exit(1)
	elif (op =="release"):
		log = getLog(v)
		if not log:
			print("No log for version '" + v + "'", file=sys.stderr)
			exit(1)
		print("Captured log:")
		print(log)
		if not createRelease(v, log):
			exit(1)

	else:
		print("Unsupported operation '" + op + "'", file=sys.stderr)
		usage()