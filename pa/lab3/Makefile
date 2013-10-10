CC=gcc
CFLAGS=  -Wall -pedantic

CFILES=queue.c udp_server.c udp_client.o
HFILES=queue.h
OFILES=queue.o udp_server.o udp_client.o

all:	udp_server udp_client

%.o:	%.c $(HFILES)
	$(CC) -c $(CFLAGS) $< -o $@

udp_server:	udp_server.o queue.o $(HFILES)
	$(CC) $(CFLAGS) udp_server.o queue.o -o udp_server

udp_client:	udp_client.o queue.o $(HFILES)
	$(CC) $(CFLAGS) udp_client.o queue.o -o udp_client


clean:	
	rm -f *~ *.o udp_server udp_client

