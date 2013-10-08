/*
 * main.c -- computes approximations to several predefined functions
 * author: xiaochen qi
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>

#include "./queue.h"
#include "./integrate.h"

#define FUN_NUMBER 4
#define PARTITION_THREAD 15

#define GENERATOR

/* predefined functions */
double f1(double x){
    return x + 5.0;
}

double f2(double x){
    return x * x * 2.0 + x * 9.0 + 4.0;
}

double f3(double x){
    return x * x * x * 3.0 - x * 5.0 - 10.0;
}

double f4(double x){
    return 30.0 / x - 3.5 * x * x;
}

double (*funs[FUN_NUMBER]) (double x);

/* queue match */
int match_item(void *elementp, void *keyp){
    if(elementp == keyp){
        return 0;
    } else {
        return 1;
    }
}

/* data for thread */
typedef struct tdata{
    double (*fun)(double x);
    double l_bound;
    double u_bound;
    double precision;
    integral result;
}tdata;

typedef struct gdata{
    int threads;
    tdata basics;
}gdata;

double integral_val = 0.0;
int integral_strips = 0;

/* lock */
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t q_lock = PTHREAD_MUTEX_INITIALIZER;


/* queue for partitions */
void *q_part = NULL;
int generator_flag = 0;/* if generator has finished */

/* thread function */
#ifndef GENERATOR
void * calc(void * data){
    int rc;
    tdata * portion = (tdata *)data;
    portion->result = integrate(portion->fun, portion->l_bound, portion->u_bound, portion->precision);
    /* update val and strips */
    rc = pthread_mutex_lock(&lock);
    if (rc) {
        printf("thread acquire failed!\n");
        exit(-1);
    }
    integral_val += portion->result.value;
    integral_strips += portion->result.strips;
    rc = pthread_mutex_unlock(&lock);
    return NULL;
}
#else
void * calc(void * data){
    int loop = 1, rc, done;
    tdata * portion;
    while(loop){
        rc = pthread_mutex_lock(&q_lock);
        if (rc) {
            printf("thread acquire failed!\n");
            exit(-1);
        }
        portion = (tdata *)qremove(q_part, match_item, qget(q_part));
        done = generator_flag;
        rc = pthread_mutex_unlock(&q_lock);
        if(NULL == portion){
            loop = !done;
        } else {        
            portion->result = integrate(portion->fun, portion->l_bound, portion->u_bound, portion->precision);
            /* update val and strips */
            rc = pthread_mutex_lock(&lock);
            if (rc) {
                printf("thread acquire failed!\n");
                exit(-1);
            }
            integral_val += portion->result.value;
            integral_strips += portion->result.strips;
            free(portion);
            rc = pthread_mutex_unlock(&lock);
        }
    }
    return NULL;
}
#endif

#ifdef GENERATOR
void * manage(void * data){
    int i = 0, rc;
    pthread_t *threads = NULL;
    tdata * thread_data = NULL;
    gdata * generator_data = (gdata *)data;
    int partitions = generator_data->threads * PARTITION_THREAD;
    /* generate partitions */
    double portion = (generator_data->basics.u_bound - generator_data->basics.l_bound) / partitions;

    /* queue */
    q_part = qopen();

    /* may do this before adding partitions to the queue */
    threads = (pthread_t *)malloc(generator_data->threads * sizeof(pthread_t));
    for(i = 0;i < generator_data->threads;++i){
        rc = pthread_create(threads + i, NULL, calc, NULL);
        if(rc) {
            printf("thread creation failed!\n");
            exit(-1);
        }
    }
         
    for(i = 0;i < partitions;++i){
        thread_data = malloc(sizeof(tdata));
        thread_data->fun = generator_data->basics.fun;
        thread_data->l_bound = generator_data->basics.l_bound + i * portion;
        thread_data->u_bound = generator_data->basics.l_bound + (i + 1) * portion;
        thread_data->precision = generator_data->basics.precision;

        rc = pthread_mutex_lock(&q_lock);
        if (rc) {
            printf("thread acquire failed!\n");
            exit(-1);
        }
        qput(q_part, thread_data);
        /* set flag after put the last partition */
        if(i + 1 == partitions){
            generator_flag = 1;
        }
        rc = pthread_mutex_unlock(&q_lock);
    }
    
    for(i = 0; i < generator_data->threads; i++){
        pthread_join(*(threads + i), NULL);
    }

    printf("threaded, integral: %f, strip calcs: %d\n", integral_val, integral_strips);
    printf("non-thread, integral: %f, strip calcs: %d\n", integrate(generator_data->basics.fun, generator_data->basics.l_bound, generator_data->basics.u_bound, generator_data->basics.precision).value, integrate(generator_data->basics.fun, generator_data->basics.l_bound, generator_data->basics.u_bound, generator_data->basics.precision).strips);

    free(threads);
    qclose(q_part);
    return NULL;
}
#endif

public int main(int argc, char *argv[]) {
    /* args: function#, interval * 2, precision, threads m  */
    /* checks needed */
    int fun, m;
    double l_bound, u_bound, precision;

    /* prepare thread data */
#ifdef GENERATOR
    pthread_t generator;
    gdata generator_data;
#else
    int i = 0;
    double portion = 0.0;
    pthread_t *threads = NULL;
    tdata *tdatas = NULL;
#endif

    int rc;
    double (*cur_fun)(double x);

    funs[0] = f1;
    funs[1] = f2;
    funs[2] = f3;
    funs[3] = f4;

    fun = atoi(argv[1]);   
    l_bound = atof(argv[2]);   
    u_bound = atof(argv[3]);
    precision = (argc>4) ? atof(argv[4]) : 0.01;
    m = (argc>5) ? atoi(argv[5]) : 10;

    cur_fun = funs[(fun - 1) % FUN_NUMBER];

    /* create threads */
    if(m > 0){
#ifdef GENERATOR
        generator_data.threads = m;
        generator_data.basics.fun = cur_fun;
        generator_data.basics.l_bound = l_bound;
        generator_data.basics.u_bound = u_bound;
        generator_data.basics.precision = precision;
        rc = pthread_create(&generator, NULL, manage, &generator_data);
        if(rc) {
            printf("thread creation failed!\n");
            exit(-1);
        }
        pthread_join(generator, NULL);
#else
        threads = (pthread_t *)malloc(m * sizeof(pthread_t));
        tdatas = (tdata *)malloc(m * sizeof(tdata));
        
        portion = (u_bound - l_bound) / m;
        
        for(;i < m;++i){
            (tdatas + i)->fun = cur_fun;
            (tdatas + i)->l_bound = l_bound + i * portion;
            (tdatas + i)->u_bound = l_bound + (i + 1) * portion;
            (tdatas + i)->precision = precision;
            rc = pthread_create(threads + i, NULL, calc, tdatas + i);
            
            if(rc) {
                printf("thread creation failed!\n");
                exit(-1);
            }
        }

        for(i = 0; i < m; i++){
            pthread_join(*(threads + i), NULL);
        }

        printf("threaded, integral: %f, strip calcs: %d\n", integral_val, integral_strips);
        printf("non-thread, integral: %f, strip calcs: %d\n", integrate(cur_fun, l_bound, u_bound, precision).value, integrate(cur_fun, l_bound, u_bound, precision).strips);


        free(tdatas);
        free(threads);
#endif
    } else {
        printf("input error\n");
    }


    return EXIT_SUCCESS;
}

