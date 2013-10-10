/*
 * udp_client.c
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

#define BUFFER_SIZE 512

char buffer[BUFFER_SIZE];

typedef struct sockpair{
    int socket;
    struct sockaddr_in *server;
}sockpair;

void * receive(void *addr){
    int rc;
    sockpair *pair = (sockpair *)addr;
    socklen_t addr_len = sizeof(*(pair->server));
    while(1) {
        memset(buffer, 0, BUFFER_SIZE);
        rc = recvfrom(pair->socket, buffer, sizeof(buffer), 0, (struct sockaddr *)(pair->server), &addr_len);
        printf("from server: %s\n", buffer);
    }
}

int main(int argc, char *argv[]) {
    int	sd, rc;
    char *id;
    char input[BUFFER_SIZE];
    sockpair pair;
    pthread_t receiver;


    struct  sockaddr_in server;
    /* socklen_t addr_len = sizeof(server); */
    struct  hostent *hp;

    printf("user id: %s\n", argv[1]);
    id = argv[1];
    if(strlen(id) < 1){
        printf("ERROR: invalid id\n");
    }

    sd = socket(AF_INET, SOCK_DGRAM, 0);

    server.sin_family = AF_INET;
    hp = gethostbyname(argv[2]);
    if(NULL == hp){
        printf("unable to get host by name\n");
        exit(-1);
    }
    memcpy(&(server.sin_addr.s_addr), hp->h_addr, hp->h_length);
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
        }
        sendto(sd, input, sizeof(input), 0, (struct sockaddr *)&server, sizeof(server));

        /* receive response in a thread */
    }
    
    close(sd);

    return EXIT_SUCCESS;
}

