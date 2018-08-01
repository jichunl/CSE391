#!/bin/bash

if [ $# -ne 1 ]; then
	echo "Usage: ./$0 MAXPOINTS"
	exit 1
fi

echo "Retro Grade-It, 1970s version"
echo "Grading with a max score of $1"
echo ""



for DIR in ./students/*; do
	(
	cd $DIR && STUDENT_NAME=${PWD##*/} && 
	SCORE=50
	FILE=gettysburg.sh
	if [ -f $FILE ]; then
		bash ./gettysburg.sh > output.txt
		DIFF=$(diff -b ../../expected.txt ./output.txt | grep '^>' | wc -l)
		if [ $DIFF -eq 0 ]; then 
			echo "$STUDENT_NAME has correct output"
		else
			echo "$STUDENT_NAME has incorrect output ($DIFF lines do not match)"
			let SCORE="$SCORE-$DIFF*5"
		fi
		COMMENTS=$(grep '^#' ./gettysburg.sh  | wc -l)
		echo "$STUDENT_NAME has $COMMENTS lines with comments"
		if [ $COMMENTS -lt 3 ]; then
			let SCORE="$SCORE-7"
		fi
		if [ $SCORE -lt 0 ]; then
			let SCORE=0
		fi
		rm ./output.txt
	else 
		let SCORE=0
		echo "$STUDENT_NAME did not turn in the assignment"
	fi
	echo "$STUDENT_NAME has earned a score of $SCORE / 50"
	echo ""
	);
done
