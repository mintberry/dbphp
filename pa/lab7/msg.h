/* msg.h */
/* macros for mpi calls */
#ifndef _MSG_H
#define _MSG_H

#define SENDMSG(buf, buf_size, dest, tag);                            \
    if(MPI_SUCCESS != MPI_Send(buf, buf_size, MPI_INT, dest, tag, MPI_COMM_WORLD)){printf("send ERROR\n");exit(-1);} 
#define RECVMSG(buf, buf_size, status);                                 \
    if(MPI_SUCCESS != MPI_Recv(buf, buf_size, MPI_INT, MPI_ANY_SOURCE,                \
                               MPI_ANY_TAG, MPI_COMM_WORLD, &status)){printf("recv ERROR\n");\
        exit(-1);}



#define PROBERECV(buf, buf_size, status);\
    {\
        if(MPI_SUCCESS != MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status)){printf("probe ERROR\n");} \
    MPI_Get_count(&status, MPI_INT, &buf_size);                \
    buf = (int*)malloc(sizeof(int) * buf_size);                         \
    MPI_Recv(buf, buf_size, MPI_INT, status.MPI_SOURCE, status.MPI_TAG, MPI_COMM_WORLD,\
             MPI_STATUS_IGNORE);\
}

#endif
