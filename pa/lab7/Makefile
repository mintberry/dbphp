CC=mpicc
CFLAGS= -ansi -Wall -pedantic

CFILES=queue.c lqueue.c nqueen.c manager_worker.c main.c
HFILES=queue.h lqueue.h nqueen.h manager_worker.h msg.h
OFILES=queue.o lqueue.o nqueen.o manager_worker.o main.o

all:	main

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

main:	main.o manager_worker.o nqueen.o lqueue.o queue.o $(HFILES)
	$(CC) $(CFLAGS) main.o manager_worker.o nqueen.o lqueue.o queue.o -o main -lpthread


clean:	
	rm -f *~ *.o main

