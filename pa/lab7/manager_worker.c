#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <pthread.h>
#include <math.h>

#include "./lqueue.h"
#include "./manager_worker.h"

/* lock */
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

/* locked queue */
void * q_part;

int rank, size, generator_flag = 0;

/* thread calls */
void * generate(void * data){
    int i, N = *((int *)data);
    int * move = (int *)malloc(sizeof(int) * (N + 3));
    move[0] = N;
    move[1] = 0;
    move[2] = 0;
    for(i = 0;i != N;++i){
        move[i + 3] = -1;
    }
    lqput(q_part, move);
    return NULL;
}

/* 
 * manager worker scheme 
*/
void manager(int size, void *args, void * (*f)(void * data, void * queue, int * done, int * tasks), void* (* handler)(void * data, void * queue, int * done, int * tasks)){
    int rc, tasks = 1, done = 0, buf_size;
    MPI_Status status;
    pthread_t generator;
    int *buf;
    msg_size data_msg;
    msg_size * new_msg;

    int results = size - 1;

    /* start a thread to do partition */
    q_part = lqopen();
    rc = pthread_create(&generator, NULL, generate, args);
    pthread_join(generator, NULL);    

    while(results != 0/* workers not all done */){
        PROBERECV(buf, buf_size, status);    
        if(status.MPI_TAG == MSG_TAG){  
            new_msg = f(args, q_part, &done, &tasks);
            SENDMSG(new_msg->msg, new_msg->size, status.MPI_SOURCE, DATA_TAG);
            free(new_msg);
        } else if (status.MPI_TAG == RESULT_TAG){   
            results--;
            printf("%d placed %d\n", status.MPI_SOURCE, buf[0]);
        } else {/* must be data tag */
            data_msg.msg = buf;
            data_msg.size = buf_size;
            handler(&data_msg, q_part, &done, &tasks);
            /* printf("test %d\n", flag); */        
        }
        
        if(NULL != buf){
            free(buf);
        }
    }

    lqclose(q_part);
}

void worker(void *args, void * (*f)(void * data)){
    MPI_Status status;
    int * buf;
    int buf_size = 0;
    msg_size * new_msg;
    msg_size data_msg;

    int done = 0;
    int count = 0;

    SENDMSG(&done, 1, M_RANK, MSG_TAG);
    while(!done/* not all done */){
        PROBERECV(buf, buf_size, status);

        data_msg.msg = buf;
        data_msg.size = buf_size;

        /* only recv data tags */
        if(buf_size > 0/* a legal task */){
            SENDMSG(&done, 1, M_RANK, MSG_TAG);
            /* do the work */         
            new_msg = f(&data_msg);
            if(NULL != new_msg){
                count++;
                SENDMSG(new_msg->msg, new_msg->size, M_RANK, DATA_TAG);
                free(new_msg);
            }
        } else {
            done = 1;
        }
        
        if(NULL != buf){
            free(buf);
        }

    }

    SENDMSG(&count, 1, M_RANK, RESULT_TAG);    
}

void concurrent(int argc, char *argv[], void * args, void * (*m)(void * data, void * queue, int * done, int * tasks), void * (*w)(void * data), void* (* handler)(void * data, void * queue, int * done, int * tasks)){
    int rank, size;
    MPI_Init(&argc, &argv);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    
    if(M_RANK == rank){ /* manager */
        manager(size, args, m, handler);
    } else { /* worker */
        worker(args, w);
    }   

    MPI_Finalize();
}


