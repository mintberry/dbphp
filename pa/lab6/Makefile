CC=mpicc
CFLAGS= -ansi -Wall -pedantic

CFILES=queue.c lqueue.c integrate.c manager_worker.c
HFILES=queue.h lqueue.h integrate.h
OFILES=queue.o lqueue.o integrate.o manager_worker.o

all:	manager_worker

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

manager_worker:	manager_worker.o integrate.o lqueue.o queue.o $(HFILES)
	$(CC) $(CFLAGS) manager_worker.o integrate.o lqueue.o queue.o -o manager_worker -lpthread


clean:	
	rm -f *~ *.o manager_worker

