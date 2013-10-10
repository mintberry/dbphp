/*
 * udp_server.c
 * author: xiaochen qi
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "./queue.h"

#define BUFFER_SIZE 512
#define NAME_SIZE 16

const char * ack = "ACK";
const char * leave = "LEAVE";
const char * error = "ERROR";


typedef struct user_info{
    char id[NAME_SIZE];
    struct sockaddr_in addr;
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
    user_info * u2 = (user_info *)keyp;
    if(u1->addr.sin_addr.s_addr == u2->addr.sin_addr.s_addr){
        return 0;
    } else {
        return 1;
    }
}

public int main(int argc, char *argv[]) {
    int sd;
    struct sockaddr_in server;
    struct sockaddr_in client;
    socklen_t addr_len = sizeof(client); 
    char buffer[BUFFER_SIZE];
    void * q_user;
    int rc, i;
    user_info *sender, *temp_user;

    q_user = qopen();

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port = htons(atoi(argv[1]));

    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if(-1 == sd){
        printf("unable to create server socket\n");
        exit(-1);
    }

    bind (sd, (struct sockaddr *)&server, sizeof(server));
    printf("server starts\n");
   
    while(1) {
        memset(buffer, 0, BUFFER_SIZE);
        rc = recvfrom(sd, buffer, sizeof(buffer), 0, (struct sockaddr *)&client, &addr_len);
        
        printf("received: %s\n", buffer);
        if(0 == strcmp(buffer, "/ping")){
            /* send an ack back to sender */
            sendto(sd, ack, sizeof(ack), 0, (struct sockaddr *)&client, sizeof(client));
        } else if(0 == strncmp(buffer, "/join", 5)){
            /* add user to queue if id is not duplicated */
            sender = (user_info *)malloc(sizeof(user_info));
            strcpy(sender->id, buffer + 6); /* copy id start after /join */
            memcpy(&(sender->addr), &client, addr_len);
            if(NULL == qsearch(q_user, match_id, sender)){
                qput(q_user, sender);
                /* send an ack back to sender */
                sendto(sd, ack, sizeof(ack), 0, (struct sockaddr *)&client, sizeof(client));
            } else {
                printf("ERROR: choose another user id\n");
                sendto(sd, error, sizeof(error), 0, (struct sockaddr *)&client, sizeof(client));
            }
        } else if(0 == strcmp(buffer, "/leave")){
            /* remove a user from queue */
            sender = (user_info *)malloc(sizeof(user_info));
            memcpy(&(sender->addr), &client, addr_len);
            if(NULL != (sender = qremove(q_user, match_addr, sender))){
                free(sender);
                sendto(sd, leave, sizeof(leave), 0, (struct sockaddr *)&client, sizeof(client));
            } else {
                printf("ERROR: no such user\n");
                sendto(sd, error, sizeof(error), 0, (struct sockaddr *)&client, sizeof(client));
            }
        } else if(0 == strcmp(buffer, "/who")){
            memset(buffer, 0, sizeof(buffer));
            for (i = 0;i < qsize(q_user);++i){
                temp_user = (user_info *)qat(q_user, i);
                strcat(buffer, ",");
                strcat(buffer, temp_user->id);
            }
            sendto(sd, buffer, sizeof(buffer), 0, (struct sockaddr *)&client, sizeof(client));
            
        } else {
            /* broadcast the msg if the sender is in queue */
            sender = (user_info *)malloc(sizeof(user_info));
            memcpy(&(sender->addr), &client, addr_len);
            if(NULL != qsearch(q_user, match_addr, sender)){
                for (i = 0;i < qsize(q_user);++i){
                temp_user = (user_info *)qat(q_user, i);
                if(sender->addr.sin_addr.s_addr != temp_user->addr.sin_addr.s_addr){
                    sendto(sd, buffer, sizeof(buffer), 0, (struct sockaddr *)(&(temp_user->addr)), sizeof(client));
                }
            }
            } else {
                /* discard the message */
            }
        }
    }

    
    qclose(q_user);
    close(sd);

    return EXIT_SUCCESS;
}

