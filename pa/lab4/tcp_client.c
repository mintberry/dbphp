/*
 * tcp_client.c
 * author: xiaochen qi
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <unistd.h>

#define BUFFER_SIZE 512

char buffer[BUFFER_SIZE];
int retries = 3;

/* global socket info */
int sd;
struct  sockaddr_in server;


typedef struct sockpair{
    int socket;
    struct sockaddr_in *server;
}sockpair;

/* The signal handler just clears the flag and re-enables itself. */
void catch_alarm (int sig)
{
    if(retries--){
        /* resend the ping */
        printf("re send\n");
        sendto(sd, "/ping", sizeof("/ping"), 0, (struct sockaddr *)&server, sizeof(server));
        alarm(5);
    } else {
        printf("Server Not Available\n");
        exit(-1);
    }
}


void * receive(void *addr){
    int rc;
    sockpair *pair = (sockpair *)addr;
    socklen_t addr_len = sizeof(*(pair->server));
    while(1) {
        memset(buffer, 0, BUFFER_SIZE);
        rc = recvfrom(pair->socket, buffer, sizeof(buffer), 0, (struct sockaddr *)(pair->server), &addr_len);
        if(0 == strcmp(buffer, "ACK")){
            alarm(0);
            retries = 3;
        }
        printf("from server: %s\n", buffer);
    }
}

int main(int argc, char *argv[]) {
    int	rc;
    char *id;
    char input[BUFFER_SIZE];
    sockpair pair;
    pthread_t receiver;

    /* socklen_t addr_len = sizeof(server); */
    /*
    struct  hostent *hp;
    */

    printf("user id: %s\n", argv[1]);
    id = argv[1];
    if(strlen(id) < 1 || strlen(id) > 15){
        printf("ERROR: invalid id\n");
    }

    sd = socket(AF_INET, SOCK_DGRAM, 0);

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = inet_addr(argv[2]);
    server.sin_port = htons(atoi(argv[3]));

    /* set up receiver */
    pair.socket = sd;
    pair.server = &server;
    rc = pthread_create(&receiver, NULL, receive, &pair);

    while(1) {
        memset(input, 0, BUFFER_SIZE);
        
        printf("type a command or msg:\n");
        scanf("%s", input);
        if(0 == strcmp(input, "/join")){
            strcat(input + 5, ":");
            strcat(input + 6, id);
        } else if(0 == strcmp(input, "/ping")) {
            signal (SIGALRM, catch_alarm);
            alarm(5);
        }
        sendto(sd, input, sizeof(input), 0, (struct sockaddr *)&server, sizeof(server));

        /* receive response in a thread */
    }
    

    close(sd);

    return EXIT_SUCCESS;
}

