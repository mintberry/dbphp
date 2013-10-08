CC=gcc
CFLAGS= -ansi -Wall -pedantic

CFILES=queue.c integrate.c main.c
HFILES=queue.h integrate.h
OFILES=queue.o integrate.o main.o

all:	main

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

main:	$(OFILES) $(HFILES)
	$(CC) $(CFLAGS) $(OFILES) -o main


clean:	
	rm -f *~ main

