#!/bin/bash
git checkout -b release || exit 1
git push origin release || exit 1
git checkout -