#!/usr/bin/env python
#Script to get the maven project version and manipulate it a bit
#basically, it is faster and more robust than mvn versions
from __future__ import print_function
import xml.etree.ElementTree as ET
import re
import sys
import subprocess 


def toRelease(v):
	if v.endswith("-SNAPSHOT"):
		return v[0:-1*len("-SNAPSHOT")]
	return v


def nextVersion(v):
	if v.endswith("-SNAPSHOT"):
		return v[0:-1*len("-SNAPSHOT")]			
	else:
		nbs = v.split('.')
		last = int(nbs[-1])
		last += 1
		nbs[-1] = str(last)
		return ".".join(nbs)

def parseVersion():
	try:
		tree = ET.parse('./pom.xml')
		root = tree.getroot()	
		return tree.find("{http://maven.apache.org/POM/4.0.0}version").text		
	except:
		print("Unable to parse 'pom.xml'", file=sys.stderr)
		return None

if __name__ == "__main__":
	version=parseVersion()
	if not version:
		exit(1)
		
	if (len(sys.argv) == 1):
		print(version)
		exit(0)

	if (sys.argv[1] == "--release"):
		print(toRelease(version))
	elif (sys.argv[1] == "--next"):
		print(nextVersion(version))
	else:
		print("Usage: %s (--release|--next)" % sys.argv[0], file=sys.stderr)
		exit(1)