/*
 * lqueue.c
 * implementation of locked queue
 * author: xiaochen qi
 */
#include "queue.h"

#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<pthread.h>
#include "queue.h"

#define LINKEDLIST

#ifdef LINKEDLIST

typedef struct lqtype{
    void *queue;
    pthread_mutex_t q_lock;
}lqtype;

/* queue match */
int match_item(void *elementp, void *keyp){
    return ((elementp == keyp)?0:1);
}

void* lqopen(){
    pthread_mutex_t q_lock = PTHREAD_MUTEX_INITIALIZER;
    lqtype *lqueue = (lqtype *)malloc(sizeof(lqtype));
    if(NULL == lqueue){
	printf("unable to alloc memory for queue\n");
	return NULL;
    }
    lqueue->q_lock = q_lock;
    lqueue->queue = qopen();
    return lqueue;
}

void lqclose(void *qp){
    if(NULL != qp){
        qclose(((lqtype *)qp)->queue);
	free((lqtype *)qp);
    }
}

void lqput(void *qp, void *elementp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        qput(((lqtype *)qp)->queue, elementp);
        rc = pthread_mutex_unlock(q_lock);
    } else {
	printf("should open queue before using it\n");
    }

}

void* lqget(void *qp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        void * ret = qget(((lqtype *)qp)->queue);
        rc = pthread_mutex_unlock(q_lock);
        return ret;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

void* lqgetrm(void *qp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        void * ret = qget(((lqtype *)qp)->queue);
        qremove(((lqtype *)qp)->queue, match_item, ret);
        rc = pthread_mutex_unlock(q_lock);
        return ret;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

void lqapply(void *qp, void (*fn)(void *elementp)){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        qapply(((lqtype *)qp)->queue, fn);
        rc = pthread_mutex_unlock(q_lock);
    } else {
	printf("should open queue before using it\n");
    }
}

/* returns first matching element */
void* lqsearch(void *qp, int (*searchfn)(void* elementp,void* keyp),
	      void* skeyp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);

        void * ret = qsearch(((lqtype *)qp)->queue, searchfn, skeyp);
        rc = pthread_mutex_unlock(q_lock);
	return ret;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

/* removes first matching element */
void* lqremove(void *qp, int (*searchfn)(void* elementp,void* keyp),
	      void* skeyp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);

        void * ret = qremove(((lqtype *)qp)->queue, searchfn, skeyp);
        rc = pthread_mutex_unlock(q_lock);

	return ret;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

void lqconcat(void *q1p, void *q2p){
    if(NULL != q1p && NULL != q2p){
        pthread_mutex_t *q1_lock = &(((lqtype *)q1p)->q_lock);
        pthread_mutex_t *q2_lock = &(((lqtype *)q2p)->q_lock);
        int rc = pthread_mutex_lock(q1_lock);
        rc = pthread_mutex_lock(q2_lock);

        qconcat(((lqtype *)q1p)->queue, ((lqtype *)q2p)->queue);
        rc = pthread_mutex_unlock(q2_lock);
        rc = pthread_mutex_unlock(q1_lock);
    } else if (NULL != q1p && NULL == q2p){
        /* do nothing */        
    } else if (NULL == q1p && NULL != q2p){
        pthread_mutex_t *q2_lock = &(((lqtype *)q2p)->q_lock);
        int rc = pthread_mutex_lock(q2_lock);

        qconcat(NULL, ((lqtype *)q2p)->queue);
        rc = pthread_mutex_unlock(q2_lock);
    } else {
        printf("both queues are NULL\n");
    }
}

/* returns data at n */
void *lqat(void *qp, int n){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        void * ret = qat(((lqtype *)qp)->queue, n);
        rc = pthread_mutex_unlock(q_lock);

	return ret;
    } else {
        printf("should open queue before using it\n");
	return NULL;
    }
}

/* get queue size */
int lqsize(void * qp){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        int ret = qsize(((lqtype *)qp)->queue);
        rc = pthread_mutex_unlock(q_lock);

	return ret;
    } else {
        printf("should open queue before using it\n");
	return -1;
    }
}

void lqsearchapply(void *qp, int (*searchfn)(void* elementp,void* keyp),
                  void* skeyp, void (*fn)(void * elementp, void * data), void * data){
    if(NULL != qp){
        pthread_mutex_t *q_lock = &(((lqtype *)qp)->q_lock);
        int rc = pthread_mutex_lock(q_lock);
        qsearchapply(((lqtype *)qp)->queue, searchfn, skeyp, fn, data);
        rc = pthread_mutex_unlock(q_lock);
    } else {
        printf("should open queue before using it\n");
    }
}

#else
/* use contiguous storage */
#endif
