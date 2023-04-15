#!/bin/bash

# a script that exports png files from openscad (.scad) files and uses gimp scripts to make it's background transparent

readdir="things/"

savedir="things/stl/"

for k in {1..3}
do
	
	if [ $k -eq 1 ]
	then
		key="6key"
	elif [ $k -eq 2 ]
	then
		key="5key"
	elif [ $k -eq 3 ]
	then
		key="3key"
	fi

	for x in {1..2}
	do
		
		if [ $x -eq 1 ]
		then
			pos="right"
		else
			pos="left"
		fi
		
		for i in {1..4}
		do
			
			camera=-30,20,0,40,0,30,600

			if [ $i -eq 1 ]
			then
				size="4x5"
			elif [ $i -eq 2 ]
			then
				size="4x6"
			elif [ $i -eq 3 ]
			then
				size="5x6"
			elif [ $i -eq 4 ]
			then
				size="6x6"
			fi

			for j in {1..4}
			do

				if [ $j -eq 1 ]
				then
					part="-ic"
				elif [ $j -eq 2 ]
				then
					part="-plate"
				elif [ $j -eq 3 ]
				then
					part="-rest"
				elif [ $j -eq 4 ]
				then
					part=""
				fi

				dir=$key"/"$size"/"
				file=$key"-"$size"-"$pos$part

				openscad \
					--export-format stl -o $savedir$dir$file".stl" \
					--enable=fast-csg \
					--render \
					--quiet \
					$readdir$dir$file".scad"

				echo $file" done"


			done
		done
	done
done
