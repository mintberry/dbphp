/*
 * queue.c
 * implementation of queue
 * author: xiaochen qi
 * coded on the flight from SEA to DTW
 */
#include "queue.h"

#include<stdio.h>
#include<stdlib.h>

#define LINKEDLIST

#ifdef LINKEDLIST

typedef struct qnode{
    void *data;
    struct qnode *next;
}qnode;

typedef struct qtype{
    qnode *head;
    int count;
}qtype;

void* qopen(){
    qtype *queue = (qtype *)malloc(sizeof(qtype));
    if(NULL == queue){
	printf("unable to alloc memory for queue\n");
	return NULL;
    }
    queue->head = NULL;
    queue->count = 0;
    return queue;
}

void qclose(void *qp){
    if(NULL != qp){
        if(NULL != ((qtype *)qp)->head){
            printf("queue is not empty\n");
            return;
        }
	free((qtype *)qp);
    }
}

void qput(void *qp, void *elementp){
    if(NULL != qp){
	qnode *head = ((qtype *)qp)->head;
	qnode *new_node = (qnode *)malloc(sizeof(qnode));
	if(NULL == new_node){
	    printf("unable to alloc memory for queue node\n");
	    return;
	}
	new_node->data = elementp;
	new_node->next = NULL;
        ((qtype *)qp)->count++;
	if(NULL == head){
	    ((qtype *)qp)->head = new_node;
	    return;
	}
	while(NULL != head->next){
	    head = head->next;
	}
	head->next = new_node;
    } else {
	printf("should open queue before using it\n");
    }
}

void* qget(void *qp){
    if(NULL != qp){
	if(NULL != ((qtype *)qp)->head){
	    return ((qtype *)qp)->head->data;
	} else {
	    return NULL;
	}
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

void qapply(void *qp, void (*fn)(void *elementp)){
    if(NULL != qp){
	qnode *head = ((qtype *)qp)->head;
	while(NULL != head){
	    fn(head->data);
	    head = head->next;
	}
    } else {
	printf("should open queue before using it\n");
    }
}

/* returns first matching element */
void* qsearch(void *qp, int (*searchfn)(void* elementp,void* keyp),
	      void* skeyp){
    if(NULL != qp){
	qnode *head = ((qtype *)qp)->head;
	while(NULL != head){
	    if(0 == searchfn(head->data, skeyp)){
		return head->data;
	    }
	    head = head->next;
	}
	return NULL;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

/* removes first matching element */
void* qremove(void *qp, int (*searchfn)(void* elementp,void* keyp),
	      void* skeyp){
    if(NULL != qp){
	qnode *head = ((qtype *)qp)->head;
	qnode *prev = NULL;
	void *data = NULL;
	while(NULL != head){
	    if(0 == searchfn(head->data, skeyp)){
		if(NULL == prev){
		    ((qtype *)qp)->head = head->next;
		} else {
		    prev->next = head->next;
		}
                ((qtype *)qp)->count--;
		data = head->data;
		free(head);
		return data;
	    }
	    prev = head;
	    head = head->next;
	}
	return NULL;
    } else {
	printf("should open queue before using it\n");
	return NULL;
    }
}

void qconcat(void *q1p, void *q2p){
    if(NULL != q1p){
	if(NULL != q2p && NULL != ((qtype *)q2p)->head){
	    if(NULL == ((qtype *)q1p)->head){
		((qtype *)q1p)->head = ((qtype *)q2p)->head;
	    } else {
		qnode *head1 = ((qtype *)q1p)->head;
		while(NULL != head1->next){
		    head1 = head1->next;
		}
		head1->next = ((qtype *)q2p)->head;
	    }
            ((qtype *)q2p)->head = NULL;
	}
    } else {
        if(NULL != q2p && NULL != ((qtype *)q2p)->head){
            q1p = qopen();
            ((qtype *)q1p)->head = ((qtype *)q2p)->head;
            ((qtype *)q2p)->head = NULL;
        }        
    }
    ((qtype *)q1p)->count += ((qtype *)q2p)->count;   
}

/* returns data at n */
void *qat(void *qp, int n){
    qtype *q = (qtype *)qp;
    qnode *head = q->head;
    int i = 0;
    if(n >= 0 && n < q->count){
        for(;i < n;++i){
            head = head->next;
        }
        return head->data;
    } else {
        printf("ERROR: queue is empty\n");
        return NULL;
    }
}

/* get queue size */
int qsize(void * qp){
    return ((qtype *)qp)->count;
}
#else
/* use contiguous storage */
#endif
