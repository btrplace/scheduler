#!/bin/sh

mkdir -p $1
./testingSpeed.R $1.txt $1/testingSpeed.pdf
./reducingSpeed.R $1.txt $1/reducingSpeed.pdf
./reduction.R $1.txt $1/reduction.pdf
./verif.R $1.txt $1
./errors.R $1.txt $1/errors.pdf