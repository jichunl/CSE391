#!/bin/bash
# Name: Jichun Li
# Course: CSE 391 Summer 2018
# Student Number: 1531264



for file in $@; do
	NAME="$file"
	printf "$NAME:\n"
	LINE=$(wc -l < $file)
	printf "  lines: %d\n" $LINE
	BLANK=$(grep -cvP '\S' $file)
	let BLANK_PERCENT="$BLANK * 100 / $LINE" 
	printf "  blank: %d (%d%%)\n" $BLANK $BLANK_PERCENT
	CHAR=$(wc -m < $file)
	WORD=$(wc -w < $file)
	let CHAR_AVG="CHAR / WORD"
	printf "  chars: %d in %d word(s) (%d char/word)\n" $CHAR $WORD $CHAR_AVG
done 

