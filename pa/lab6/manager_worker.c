#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <pthread.h>
#include <math.h>

#include "./lqueue.h"
#include "./integrate.h"

#define MW

#define BUFFER_SIZE 512

#define MSG_TAG 0
#define DATA_TAG 1

#define M_RANK 0

#define FUN_NUMBER 3
#define PARTITION_FACTOR 20

#define SENDMSG(buf, buf_size, dest, tag);                            \
    if(MPI_SUCCESS != MPI_Send(buf, buf_size, MPI_CHAR, dest, tag, MPI_COMM_WORLD)){printf("send ERROR\n");} 
#define RECVMSG(buf, buf_size, status);                                 \
    if(MPI_SUCCESS != MPI_Recv(buf, buf_size, MPI_CHAR, MPI_ANY_SOURCE,                \
                               MPI_ANY_TAG, MPI_COMM_WORLD, &status)){printf("recv ERROR\n");}

/* datatype for thread */
typedef struct tdata{
    double (*fun)(double x);
    double l_bound;
    double u_bound;
    double precision;
    integral result;
    int m;
}tdata;

typedef struct proc_stat{
    integral result;
    double time;
}proc_stat;

typedef struct part_info{
    double l_bound;
    double u_bound;
}part_info;

proc_stat regproc(double (*fun)(double x), double l_bound, double u_bound, double precision, int m);


double integral_val = 0.0;
int integral_strips = 0;

/* to collect all procs */
double avg_trape = 0.0, total_time = 0.0, final_result = 0.0;
int max_trape = 0, min_trape = 0;

/* lock */
pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

/* locked queue */
void * q_part;

int rank, size, generator_flag = 0;

/* predefined functions */
double f1(double x){
    return x + 5.0;
}

double f2(double x){
    return x * x * 2.0 + x * 9.0 + 4.0;
}

double f3(double x){
    return x * sin(x * x);
}

double (*funs[FUN_NUMBER]) (double x);

/* thread calls */
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

void * generate(void * data){
    int i = 0, rc;
    tdata * generator_data = (tdata *)data;
    part_info * proc_data = NULL;
    int partitions = size * PARTITION_FACTOR;
    /* generate partitions */
    double portion = (generator_data->u_bound - generator_data->l_bound) / partitions;

    /* queue */         
    for(i = 0;i < partitions;++i){            
        proc_data = malloc(sizeof(part_info));
        proc_data->l_bound = generator_data->l_bound + i * portion;
        proc_data->u_bound = generator_data->l_bound + (i + 1) * portion;

        lqput(q_part, proc_data);
    }
    
    /* set flag after put the last partition */
    generator_flag = 1;

    return NULL;
}


/* 
 * manager worker scheme 
*/
void manager(tdata *args){
    MPI_Status status;
    pthread_t generator;
    proc_stat stat;
    part_info *ppi;
    part_info pi;
    double total_time = 0.0, final_result = 0.0, avg_trape = 0.0;
    int rc, flag = 0, max_trape = 0, min_trape = 0, worker_rank;

    q_part = lqopen();
    /* start a thread to do partition */
    rc = pthread_create(&generator, NULL, generate, args);
    pthread_join(generator, NULL);    

    while(flag != size - 1/* workers not all done */){
        RECVMSG(&stat, sizeof(proc_stat), status);
        if(status.MPI_TAG == MSG_TAG){    
            if(!generator_flag || NULL != lqget(q_part)){
                ppi = (part_info *)lqgetrm(q_part);
                if(NULL != ppi){
                    SENDMSG(ppi, sizeof(part_info), status.MPI_SOURCE, DATA_TAG);
                }
            } else {
                pi.l_bound = 1.0;
                pi.u_bound = 0.0;
                SENDMSG(&pi, sizeof(part_info), status.MPI_SOURCE, DATA_TAG);
            }
        } else {/* must be data tag */
            worker_rank = status.MPI_SOURCE;
            min_trape = ((min_trape > stat.result.strips || flag == 0)?stat.result.strips:min_trape);
            max_trape = ((max_trape < stat.result.strips || flag == 0)?stat.result.strips:max_trape);
            avg_trape += stat.result.strips;
            final_result += stat.result.value;
            total_time += stat.time;
            flag++;
            /* printf("test %d\n", flag); */
        }
    }
    avg_trape /= size;
    printf("avg trape: %f, max trape: %d, min trape: %d, result: %f, total time: %f\n", avg_trape, max_trape, min_trape, final_result, total_time);
    lqclose(q_part);
}

void worker(tdata *args){
    MPI_Status status;
    part_info pi;
    proc_stat stat, total;
    int partitions = 0;

    int done = 0;

    total.result.strips = 0;
    total.result.value = 0.0;
    total.time = 0.0;

    SENDMSG(&stat, sizeof(stat), M_RANK, MSG_TAG);
    while(!done/* not all done */){
        RECVMSG(&pi, sizeof(pi), status);
        /* only recv data tags */
        if(pi.l_bound < pi.u_bound){
            SENDMSG(&stat, sizeof(stat), M_RANK, MSG_TAG);
            /* do the work */        
            stat = regproc(args->fun, pi.l_bound, pi.u_bound, args->precision, args->m);
            partitions++;
            /* printf("rank %d partition %d trapezoids %d\n", rank, partitions, stat.result.strips); */
            total.result.strips += stat.result.strips;
            total.result.value += stat.result.value;
            total.time += stat.time;

        } else {
            done = 1;
        }

    }
    /* printf("other test %d\n", rank); */

    SENDMSG(&total, sizeof(total), M_RANK, DATA_TAG);   
}


/* reg proc: lab2 */
proc_stat regproc(double (*fun)(double x), double l_bound, double u_bound, double precision, int m){
    double start_time, end_time;
    int rc, i = 0;
    double portion = 0.0;
    pthread_t *threads = NULL;
    tdata *tdatas = NULL;
    proc_stat stat;

    start_time = MPI_Wtime();
    
    threads = (pthread_t *)malloc(m * sizeof(pthread_t));
    tdatas = (tdata *)malloc(m * sizeof(tdata));
        
    portion = (u_bound - l_bound) / m;

    for(;i < m;++i){
        (tdatas + i)->fun = fun;
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

    end_time = MPI_Wtime();

    /* send result to 0 */
    stat.result.value = integral_val;
    stat.result.strips = integral_strips;
    stat.time = end_time - start_time;

    integral_val = 0.0;
    integral_strips = 0;

    free(tdatas);
    free(threads);

    return stat;
}

int main(int argc, char *argv[])
{
    int fun, m;
    double l_bound, u_bound, precision;

    tdata args;
    
    /* integrate params */
    args.l_bound = atof(argv[2]);   
    args.u_bound = atof(argv[3]);
    args.precision = (argc>4) ? atof(argv[4]) : 0.01;
    args.m = (argc>5) ? atoi(argv[5]) : 10;

    if(args.l_bound > args.u_bound){
        printf("invalid input\n");
        exit(-1);
    }

    funs[0] = f1;
    funs[1] = f2;
    funs[2] = f3;

    args.fun = funs[(atoi(argv[1]) - 1) % FUN_NUMBER];   

    MPI_Init(&argc, &argv);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

#ifdef MW
    if(0 == rank){ /* manager */
        manager(&args);
    } else { /* worker */
        worker(&args);
    }
#else
    regproc(funs[(fun - 1) % FUN_NUMBER], l_bound, u_bound, precision, m);
#endif

    MPI_Finalize();
    return 0;
}


