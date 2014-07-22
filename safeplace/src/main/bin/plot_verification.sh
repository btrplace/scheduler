#!/bin/sh

for c in core states VM2VM VM2PM counting; do
    	./failures_impl.R $1/${c}.data $1/${c}.pdf
    	./failures_checker.R $1/${c}.data $1/${c}.pdf    
done

./allfailures_impl.R $1
./allfailures_checker.R $1

