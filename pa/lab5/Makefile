CC=mpicc
CFLAGS= -ansi -Wall -pedantic

CFILES=starter.c mpi_test.c
HFILES=
OFILES=starter.o mpi_test.o

all:	starter mpi_test

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

starter:	starter.o $(HFILES)
	$(CC) $(CFLAGS) starter.o -o starter

mpi_test:	mpi_test.o $(HFILES)
	$(CC) $(CFLAGS) mpi_test.o -o mpi_test


clean:	
	rm -f *~ *.o starter mpi_test

