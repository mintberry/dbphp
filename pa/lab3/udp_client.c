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

#include "./queue.h"

#define BUFFER_SIZE 512

public int main(int argc, char *argv[]) {
    int	sd, rc;
    char *id;
    char input[BUFFER_SIZE];


    struct  sockaddr_in server;
    socklen_t addr_len = sizeof(server); 
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
    bcopy(hp->h_addr, &(server.sin_addr.s_addr), hp->h_length);
    server.sin_port = htons(atoi(argv[3]));

    while(1) {
        memset(input, 0, BUFFER_SIZE);
        
        printf("type a command or msg:\n");
        scanf("%s", input);
        if(0 == strcmp(input, "/join")){
            strcat(input + 5, ":");
            strcat(input + 6, id);
        }
        sendto(sd, input, sizeof(input), 0, (struct sockaddr *)&server, sizeof(server));

        /* receive response */
        memset(input, 0, BUFFER_SIZE);
        rc = recvfrom(sd, input, sizeof(input), 0, (struct sockaddr *)&server, &addr_len);
        printf("received: %s\n", input);
    }
    
    close(sd);

    return EXIT_SUCCESS;
}

