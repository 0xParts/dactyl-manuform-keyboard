#!/bin/bash


for i in {1..3}
do

	if [ $i -eq 1 ]
	then
		thumb="(def thumb-style \"default\")"
	elif [ $i -eq 2 ]
	then
		thumb="(def thumb-style \"mini\")"
	elif [ $i -eq 3 ]
	then
		thumb="(def thumb-style \"tightly\")"
	fi

	for k in {1..4}
	do

		if [ $k -eq 1 ]
		then
			size="(def nrows 4)(def ncols 5)"
		elif [ $k -eq 2 ]
		then
			size="(def nrows 4)(def ncols 6)"
		elif [ $k -eq 3 ]
		then
			size="(def nrows 5)(def ncols 6)"
		elif [ $k -eq 4 ]
		then
			size="(def nrows 6)(def ncols 6)"
		fi

		echo $thumb$size"(load-file \"src/dactyl.clj\")" | lein repl

	done
done
