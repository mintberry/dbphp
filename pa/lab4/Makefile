CC=gcc
CFLAGS=  -Wall -pedantic

CFILES=queue.c lqueue.c tcp_server.c tcp_client.o
HFILES=queue.h lqueue.h
OFILES=queue.o lqueue.o tcp_server.o tcp_client.o

all:	tcp_server tcp_client

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

tcp_server:	tcp_server.o lqueue.o queue.o $(HFILES)
	$(CC) $(CFLAGS) tcp_server.o lqueue.o queue.o -o tcp_server

tcp_client:	tcp_client.o lqueue.o queue.o $(HFILES)
	$(CC) $(CFLAGS) tcp_client.o lqueue.o queue.o -o tcp_client


clean:	
	rm -f *~ *.o tcp_server tcp_client

