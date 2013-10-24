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

/* client queues */
void * q_user;
void * q_active;


typedef struct user_info{
    char id[NAME_SIZE];
    struct sockaddr_in addr;
    int sock;
    int joined;
    pthread_t handler;
}user_info;

void communicate(char * buffer, user_info * user);

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

/* copy id to queue */


/* thread method */
void * handle(void *info){
    char buffer[BUFFER_SIZE];
    user_info * user = (user_info *)info; 
    while(1){
        memset(buffer, 0, BUFFER_SIZE);
        communicate(buffer, user);
    }
    return NULL;
}

void user_join(void * info){
    user_info * user = (user_info *)info;
    pthread_join(user->handler, NULL);
}


public int main(int argc, char *argv[]) {
    /* server socket */
    int sd;
    struct sockaddr_in server;
    struct sockaddr_in client;
    socklen_t addr_len = sizeof(client); 
    /* char buffer[BUFFER_SIZE]; */
    int rc;
    user_info *sender;

    q_user = lqopen();
    q_active = lqopen();

    memset((char *)&server, 0, sizeof(server));
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port = htons(atoi(argv[1]));

    if(-1 == (sd = socket(AF_INET, SOCK_STREAM, 0))){
        printf("unable to create server socket\n");
        exit(-1);
    }

    bind(sd, (struct sockaddr *)&server, sizeof(server));
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

        printf("this sock:%d\n", rc);
        
        lqput(q_user, sender);
        rc = pthread_create(&(sender->handler), NULL, handle, sender);
        if(rc) {
            printf("thread creation failed!\n");
            exit(-1);
        }
        
        /* communicate(buffer, sd, client, addr_len); */
    }

    lqapply(q_user, user_join);
    lqapply(q_active, user_join);
    lqclose(q_active);
    lqclose(q_user);
    close(sd);

    return EXIT_SUCCESS;
}


/* tcp recv & send */
void communicate(char * buffer, user_info *user){
    int sd = user->sock;
    int rc, i;
    user_info *temp_user;
    
    memset(buffer, 0, BUFFER_SIZE);
    if(0 >= (rc = recv(sd, buffer, BUFFER_SIZE, 0))){
        printf("ERROR: fail to recv from client\n");
        pthread_exit(&rc);
    }
        
    printf("received: %s\n", buffer);
    rc = 0;
    if(0 == strcmp(buffer, "/ping")){
        /* send an ack back to sender */
        send(sd, ack, sizeof(ack), 0);
    } else if(0 == strncmp(buffer, "/join:", 6)){
        /* add user to queue if id is not duplicated */
        strcpy(user->id, buffer + 6);    
        if(0 == lqremoveputifn(q_user, q_active, match_addr, &(user->addr), match_id, user)){
            /* send an ack back to sender */
            send(sd, join, sizeof(join), 0);
        } else {
            printf("ERROR: duplicated user id\n");
            rc = -1;
        }
    } else if(0 == strcmp(buffer, "/leave")){
        /* remove a user from queue, remember to free! */
        if(0 == (lqremoveputifn(q_active, q_user, match_addr, &(user->addr), NULL, NULL))){
            /* free(sender); */
            send(sd, leave, sizeof(leave), 0);
        } else {
            printf("ERROR: no such user\n");
            rc = -1;
        }
    } else if(0 == strcmp(buffer, "/who")){
        /* still not safe */
        if(NULL != lqsearch(q_active, match_addr, &(user->addr))){
            memset(buffer, 0, BUFFER_SIZE);
            for (i = 0;i < lqsize(q_active);++i){
                temp_user = (user_info *)lqat(q_active, i);
                strcat(buffer, " ");
                strcat(buffer, temp_user->id);
            }
            send(sd, buffer, BUFFER_SIZE, 0);
            printf("buffer size: %lu", sizeof(buffer));
        }
    } else if(0 == strcmp(buffer, ack) || 0 == strcmp(buffer, join) || 0 == strcmp(buffer, leave)) {
        /* just skip the server only msg */
    } else {
        /* broadcast the msg if the sender is in queue */
        if(NULL != lqsearch(q_active, match_addr, &(user->addr))){
            for (i = 0;i < lqsize(q_active);++i){
                temp_user = (user_info *)lqat(q_active, i);
                if(user->addr.sin_addr.s_addr != temp_user->addr.sin_addr.s_addr || 
                   user->addr.sin_port != temp_user->addr.sin_port){
                    send(temp_user->sock, buffer, BUFFER_SIZE, 0);
                }
            }
        } else {
            /* discard the message */
        }
    }

    /* send error back if there's any */
    if(-1 == rc){
        send(sd, error, sizeof(error), 0);
    }

}
