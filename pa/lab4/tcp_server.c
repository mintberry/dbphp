/*
 * tcp_server.c
 * author: xiaochen qi
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include <pthread.h>

#include "./lqueue.h"

#define BUFFER_SIZE 512
#define NAME_SIZE 16
#define MAX_LISTEN 10
#define JOINED 1
#define LEFT 0

const char * ack = "ACK";
const char * join = "JOIN";
const char * leave = "LEAVE";
const char * error = "ERROR";

/* client queue */
void * q_user;

/* server socket */
int sd;


void communicate(char * buffer, int sd, struct sockaddr_in client, 
                 socklen_t addr_len);


typedef struct user_info{
    char id[NAME_SIZE];
    struct sockaddr_in addr;
    int sock;
    int joined;
    pthread_t handler;
}user_info;

/* search for user id */
int match_id(void *elementp, void *keyp){
    user_info * u1 = (user_info *)elementp;
    user_info * u2 = (user_info *)keyp;
    return strcmp(u1->id, u2->id);
}

/* search for user addr */
int match_addr(void *elementp, void *keyp){
    user_info * u1 = (user_info *)elementp;
    struct sockaddr_in * addr = (struct sockaddr_in *)keyp;
    if(u1->addr.sin_addr.s_addr == addr->sin_addr.s_addr){
        if(u1->addr.sin_port == addr->sin_port){
            return 0;
        } else {
            return 1;
        }
    } else {
        return 1;
    }
}

/* thread method */
void * handle(void *info){
    char buffer[BUFFER_SIZE];
    user_info * user = (user_info *)info;
    struct sockaddr_in client = user->addr;
    socklen_t addr_len = sizeof(client); 
    communicate(buffer, sd, client, addr_len);

    return NULL;
}

void user_join(void * info){
    user_info * user = (user_info *)info;
    pthread_join(user->handler, NULL);
}



public int main(int argc, char *argv[]) {
    struct sockaddr_in server;
    struct sockaddr_in client;
    socklen_t addr_len = sizeof(client); 
    char buffer[BUFFER_SIZE];
    int rc, i;
    user_info *sender, *temp_user;

    q_user = lqopen();

    memset((char *)&server, 0, sizeof(server));
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port = htons(atoi(argv[1]));

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if(-1 == sd){
        printf("unable to create server socket\n");
        exit(-1);
    }

    bind (sd, (struct sockaddr *)&server, sizeof(server));
    printf("server starts\n");

    /* listen  */
    /* set the socket for listening (queue backlog of 5) */
    if (listen(sd, MAX_LISTEN) < 0) {
        printf("failed to listen\n");
        exit(-1);
    }
   
    while(1) {
        /* accpect */
        while((rc = accept(sd, (struct sockaddr *)&client, &addr_len)) < 0){
            printf("failed to accept\n");
        }
        sender = (user_info *)malloc(sizeof(user_info));
        memcpy(&(sender->addr), &client, sizeof(client));
        sender->sock = rc;
        sender->joined = LEFT;
        
        rc = pthread_create(sender->handler, NULL, handle, &sender);
        if(rc) {
            printf("thread creation failed!\n");
            exit(-1);
        }
        
        /* communicate(buffer, sd, client, addr_len); */
    }

    lqapply(q_user, user_join);
    lqclose(q_user);
    close(sd);

    return EXIT_SUCCESS;
}


/* tcp recv & send */
void communicate(char * buffer, int sd, struct sockaddr_in client, 
socklen_t addr_len){
    int rc, i;
    user_info *sender, *temp_user;
    
    memset(buffer, 0, BUFFER_SIZE);
    rc = recvfrom(sd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&client, &addr_len);
        
    printf("received: %s\n", buffer);
    if(0 == strcmp(buffer, "/ping")){
        /* send an ack back to sender */
        sendto(sd, ack, sizeof(ack), 0, (struct sockaddr *)&client, sizeof(client));
    } else if(0 == strncmp(buffer, "/join", 5)){
        /* add user to queue if id is not duplicated */
        sender = (user_info *)malloc(sizeof(user_info));
        strcpy(sender->id, buffer + 6); /* copy id start after /join */
        memcpy(&(sender->addr), &client, sizeof(client));
        if(NULL == lqsearch(q_user, match_id, sender)){
            lqput(q_user, sender);
            /* send an ack back to sender */
            sendto(sd, join, sizeof(join), 0, (struct sockaddr *)&client, sizeof(client));
        } else {
            printf("ERROR: choose another user id\n");
            sendto(sd, error, sizeof(error), 0, (struct sockaddr *)&client, sizeof(client));
        }
    } else if(0 == strcmp(buffer, "/leave")){
        /* remove a user from queue, remember to free! */
        if(NULL != (sender = lqremove(q_user, match_addr, &client))){
            free(sender);
            sendto(sd, leave, sizeof(leave), 0, (struct sockaddr *)&client, sizeof(client));
        } else {
            printf("ERROR: no such user\n");
            sendto(sd, error, sizeof(error), 0, (struct sockaddr *)&client, sizeof(client));
        }
    } else if(0 == strcmp(buffer, "/who")){
        if(NULL !=  lqsearch(q_user, match_addr, &client)){
            memset(buffer, 0, BUFFER_SIZE);
            for (i = 0;i < lqsize(q_user);++i){
                temp_user = (user_info *)lqat(q_user, i);
                strcat(buffer, " ");
                strcat(buffer, temp_user->id);
            }
            sendto(sd, buffer, sizeof(buffer), 0, (struct sockaddr *)&client, sizeof(client));
        }
    } else if(0 == strcmp(buffer, ack) || 0 == strcmp(buffer, join) || 0 == strcmp(buffer, leave)) {
        /* just skip the server only msg */
    } else {
        /* broadcast the msg if the sender is in queue */
        if(NULL != lqsearch(q_user, match_addr, &client)){
            for (i = 0;i < lqsize(q_user);++i){
                temp_user = (user_info *)lqat(q_user, i);
                if(client.sin_addr.s_addr != temp_user->addr.sin_addr.s_addr || 
                   client.sin_port != temp_user->addr.sin_port){
                    sendto(sd, buffer, sizeof(buffer), 0, (struct sockaddr *)(&(temp_user->addr)), sizeof(client));
                }
            }
        } else {
            /* discard the message */
        }
    }

}
