#ifndef QUEUE
#define QUEUE
/* 
 * lqueue.h -- public interface to the queue module
 */
#define public
#define private static

/* create an empty queue */
public void* lqopen(void);        

/* deallocate a queue, assuming every element has been removed and deallocated */
public void lqclose(void *qp);   

/* put element at end of queue */
public void lqput(void *qp, void *elementp); 

/* get first element from a queue */
public void* lqget(void *qp);

/* apply a void function (e.g. a printing fn) to every element of a queue */
public void lqapply(void *qp, void (*fn)(void* elementp));

/* search a queue using a supplied boolean function, returns an element */
public void* lqsearch(void *qp, 
		     int (*searchfn)(void* elementp,void* keyp),
		     void* skeyp);

/* search a queue using a supplied boolean function, removes an element */
public void* lqremove(void *qp,
		     int (*searchfn)(void* elementp,void* keyp),
		     void* skeyp);

/* concatenatenates q2 onto q1, q2 may not be subsequently used */
public void lqconcat(void *q1p, void *q2p);
/* returns data at n */
void *lqat(void *qp, int n);
/* get queue size */
int lqsize(void * qp);

#endif
