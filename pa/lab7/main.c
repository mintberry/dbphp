#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include <pthread.h>
#include <math.h>

#include "./lqueue.h"
#include "./nqueen.h"
#include "./manager_worker.h"


int main(int argc, char *argv[])
{
    int N;
    
    /* nqueen params */
    N = atoi(argv[1]);   

    if(N < 4){
        printf("bad input\n");
        exit(-1);
    }

    /* Queens(N); */
    concurrent(argc, argv, &N, dispatch_moves, post_placement, new_moves);

    return 0;
}


