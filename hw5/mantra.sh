# !/bin/bash
# Name: Jichun Li
# Course: CSE 391 Summer 2018
# Student ID: 1531264

read STR_ARG TIMES 
LENGTH=$(wc -m <<< $STR_ARG)
printf "**"
for char in $(seq $LENGTH); do
	printf "*";
done
printf "*\n"
for num in $(seq $TIMES); do
	printf "* "
	printf "$STR_ARG"
	printf " *\n"
done

printf "**"
for char in $(seq $LENGTH); do
	printf "*"
done
printf "*\n"


