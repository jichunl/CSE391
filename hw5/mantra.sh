# !/bin/bash
# Name: Jichun Li
# Course: CSE 391 Summer 2018
# Student ID: 1531264

 
LENGTH=$(wc -m <<< $1)
printf "**"
for char in $(seq $LENGTH); do
	printf "*";
done

printf "*\n"
for num in $(seq $2); do
	printf "* "
	printf "$1"
	printf " *\n"
done

printf "**"
for char in $(seq $LENGTH); do
	printf "*"
done
printf "*\n"


