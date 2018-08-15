ALL = *.o list_test list_demo

default : linkedlist.c linkedlist.h use_ll_2.c
	gcc -o list_demo use_ll_2.c linkedlist.o
	gcc -o list_test use_linkedlist.c linkedlist.o
linkedlist.o: linkedlist.c linkedlist.h
	gcc -c linkedlist.c
list_demo: use_ll_2.c linkedlist.o
	gcc -o list_demo use_ll_2.c linkedlist.o
list_test: use_linkedlist.c linkedlist.o
	gcc -o list_test use_linkedlist.c linkedlist.o
clean:
	rm -rf  $(ALL)
