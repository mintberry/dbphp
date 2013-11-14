#ifndef MW_H_
#define MW_H_

#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <pthread.h>
#include <math.h>

#include "./lqueue.h"
#include "./msg.h"

#define MSG_TAG 0
#define DATA_TAG 1
#define RESULT_TAG 2

#define M_RANK 0
#define PARTITION_FACTOR 20

/* datatype for thread */
typedef struct tdata{
    double (*fun)(double x);
    double l_bound;
    double u_bound;
    double precision;
    int m;
}tdata;

typedef struct proc_stat{
    double time;
}proc_stat;

typedef struct part_info{
    double l_bound;
    double u_bound;
}part_info;

typedef struct msg_size{
    int * msg;
    int size;
}msg_size;

/* manager-worker scheme */
void manager(int size, void *args, void * (*f)(void * data, void * queue, int * done, int * tasks), void* (* handler)(void * data, void * queue, int * done, int * tasks));

void worker(void *args, void * (*f)(void * data));

void concurrent(int argc, char *argv[], void * args, void * (*m)(void * data, void * queue, int * done, int * tasks), void * (*w)(void * data), void* (* handler)(void * data, void * queue, int * done, int * tasks));

#endif
