#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>

#define MW 1
#define TOKEN 2
#define REVERSE 3

#define START_TAG 8
#define FINISH_TAG 9
#define PRINT_TAG 10

#define SENDTOKEN(buf, buf_size, dest, tag)         \
    MPI_Send(buf, buf_size, MPI_INT, dest, tag, MPI_COMM_WORLD) 
#define RECVMSG(buf, buf_size, source, tag)         \
    MPI_Recv(buf, buf_size, MPI_INT, source,\
                                             tag, MPI_COMM_WORLD, MPI_STATUS_IGNORE)


void manager_worker(int rank, int size){
    int i, rc;
    
    if(0 == rank){ /* manager */
        printf("hello: %d processes, process %d\n", size, rank);
    
        for(i = 1; i < size;++i){
            if(MPI_SUCCESS != (rc = SENDTOKEN(0, 0,  i, START_TAG))){
                printf("rank: %d send ERROR: %d\n", rank, rc);
            }

            if(MPI_SUCCESS != (rc = RECVMSG(0, 0, i, FINISH_TAG))){
                printf("rank: %d recv ERROR: %d\n", rank, rc);
            }
        }
    } else { /* worker */
        if(MPI_SUCCESS != (rc = RECVMSG(0, 0, 0, START_TAG))){
            printf("rank: %d recv ERROR: %d\n", rank, rc);
        }

        printf("hello: %d processes, process %d\n", size, rank);
    
        if(MPI_SUCCESS != (rc = SENDTOKEN(0, 0, 0, FINISH_TAG))){
            printf("rank: %d send ERROR: %d\n", rank, rc);
        }
    }
}

void token_passing(int rank, int size, int start_rank){
    int rc, val;
    int i = 0;
    if(start_rank == 0){/* reg order */
        if(rank != size - 1){
            if(rank != start_rank){
                if(MPI_SUCCESS != (rc = RECVMSG(0, 0, rank - 1, START_TAG))){
                    printf("rank: %d recv ERROR: %d\n", rank, rc);
                }
            }

            /* instead, ask the end node to print it */
            if(MPI_SUCCESS != (rc = SENDTOKEN(&rank, 1, size - 1, PRINT_TAG))){
                printf("rank: %d send ERROR: %d\n", rank, rc);
            }
            
            /* tell next node */
            if(MPI_SUCCESS != (rc = SENDTOKEN(0, 0, rank + 1, START_TAG))){
                printf("rank: %d send ERROR: %d\n", rank, rc);
            }
        } else { /* the last node */
            while(i < size - 1){
                if(MPI_SUCCESS != (rc = RECVMSG(&val, 1, MPI_ANY_SOURCE, PRINT_TAG))){
                    printf("rank: %d recv ERROR: %d\n", rank, rc);
                }
                printf("hello: %d processes, process %d\n", size, val);
                i++;
            }
            /* print self */
            printf("hello: %d processes, process %d\n", size, rank);
        }
    } else {/* reverse order */
        /* if(rank != start_rank){ */
        /*     if(MPI_SUCCESS != (rc = RECVMSG(0, 0, rank + 1, START_TAG))){ */
        /*         printf("rank: %d recv ERROR: %d\n", rank, rc); */
        /*     } */
        /* } */

        /* /\* printf("hello: %d processes, process %d\n", size, rank);     *\/ */
        /* /\* instead, ask the end node to print it *\/ */
        /* if(MPI_SUCCESS != (rc = SENDTOKEN(&rank, 1, 0, PRINT_TAG))){ */
        /*     printf("rank: %d send ERROR: %d\n", rank, rc); */
        /* } */

        if(rank != 0){
            if(rank != start_rank){
                if(MPI_SUCCESS != (rc = RECVMSG(0, 0, rank + 1, START_TAG))){
                    printf("rank: %d recv ERROR: %d\n", rank, rc);
                }
            }

            /* printf("hello: %d processes, process %d\n", size, rank);     */
            /* instead, ask the end node to print it */
            if(MPI_SUCCESS != (rc = SENDTOKEN(&rank, 1, 0, PRINT_TAG))){
                printf("rank: %d send ERROR: %d\n", rank, rc);
            }
            
            /* tell next node */
            if(MPI_SUCCESS != (rc = SENDTOKEN(0, 0, rank - 1, START_TAG))){
                printf("rank: %d send ERROR: %d\n", rank, rc);
            }
        } else { /* the last node */
            while(i < size - 1){
                if(MPI_SUCCESS != (rc = RECVMSG(&val, 1, MPI_ANY_SOURCE, PRINT_TAG))){
                    printf("rank: %d recv ERROR: %d\n", rank, rc);
                }
                printf("hello: %d processes, process %d\n", size, val);
                i++;
            }
            /* print self */
            printf("hello: %d processes, process %d\n", size, rank);
        }
    }
}

int
main(int argc, char *argv[])
{
    int size, rank;
    int option = atoi(argv[1]);

    MPI_Init(&argc, &argv);

    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    switch(option){
    case MW:
        manager_worker(rank, size);
        break;
    case TOKEN:
        token_passing(rank, size, 0);
        break;
    case REVERSE:
        token_passing(rank, size, size - 1);
        break;
    default:
        printf("option error\n");
        break;
    }

    MPI_Finalize();
    return 0;
}


