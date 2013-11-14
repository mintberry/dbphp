/*
 * nqueen.c
 * implementation of nqueen solution
 * author: xiaochen qi
 */
#include "nqueen.h"

#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<string.h>

#include "./manager_worker.h"
#include "./lqueue.h"

int on_board(int board_size, int row, int col){
    return (board_size - row > 0) && (board_size - col > 0);
}

int legal_move(int * board, int N, int row, int col){
    int legal = 1, i, row_diff, col_diff;

    /* same row */
    if(board[row] != -1){
        legal = 0;
    }

    for(i = 0;i != N;++i){
        /* same column */
        if(board[i] == col){
            legal = 0;
        }

        /* diagonal */
        if(-1 != board[i]){
            row_diff = row - i;
            col_diff = col - board[i];
            if(abs(row_diff) == abs(col_diff)){
                legal = 0;
            }
        }
    }
    return legal;
}

void print_board(int * board, int N){
    int i = 0;
    for(;i != N;++i){
        printf(" %d", board[i]);
    }
    printf("\n");
}

void place_queen(int * board, int N, int row, int col){
    if(on_board(N, row, col)){
        /* count a placement */
        if(legal_move(board, N, row, col)){
            board[row] = col;
            place_queen(board, N, row + 1, 0);
            board[row] = -1;
        }
        place_queen(board, N, row, col + 1);
    } else {
        if(row == N){/* board is filled */
            print_board(board, N);
        }
    }
}

void Queens(int N){
    int i;
    int * board = (int *)malloc(N * sizeof(int));
    /* init the board */
    for(i = 0;i != N;++i){
        board[i] = -1;
    }
    place_queen(board, N, 0, 0);
}


int same_board(int * board1, int * board2, int N){
    int i;
    for(i = 0;i != N;++i){
        if(board1[i] != board2[i]){
            return 0;
        }
    }
    return 1;
   
}

/* calls in mpi module */
int placement(void *args){
    int * move = (int *)args;
    int N = move[0], row = move[1], col = move[2];
    int * board = move + 3;
    int ret = NEXT_COL;
    if(on_board(N, row, col)){
        /* count a placement */
        if(legal_move(board, N, row, col)){
            board[row] = col;
            ret = NEXT_ROW_COL;
            board[row] = -1;
        }
    } else {
        ret = OUT_OF_BOARD;
        if(row == N){
            print_board(board, N);
        }
    }
    return ret;
}

/* worker: handler computation result */
/* malloc called */
void * post_placement(void * args){
    msg_size * data_msg = (msg_size *)args;
    int * move = data_msg->msg;
    int size = data_msg->size;
    int * new_task = NULL;
    msg_size *new_msg = NULL;
    int N = move[0];
    int ret, row, col;

    if(size > 1){/* valid task */
        new_msg = (msg_size *)malloc(sizeof(msg_size));
        ret = placement(move);

        row = move[1];
        col = move[2];

        new_msg->size = 0;

        switch(ret){
        case NEXT_ROW_COL:
            new_task = (int *)malloc(sizeof(int) * MOVE(N) * 2);
            memcpy(new_task, move, MOVE(N) * sizeof(int));
            memcpy(new_task + MOVE(N), new_task, MOVE(N) * sizeof(int));
            new_task[1] += 1;
            new_task[2] = 0;
            new_task[3 + row] = col;
            new_task[MOVE(N) + 2] += 1;
            new_msg->size = MOVE(N) * 2;
            break;
        case NEXT_COL:
            new_task = (int *)malloc(sizeof(int) * MOVE(N));
            memcpy(new_task, move, MOVE(N) * sizeof(int));
            new_task[2] += 1;
            new_msg->size = MOVE(N);
            break;
        case OUT_OF_BOARD:
            break;
        default:
            break;
        }
        new_msg->msg = new_task;
    }

    return new_msg;
}

/* manager: data handler */
/* malloc called and added to queue */
void * new_moves(void * data, void * queue, int * done, int * tasks){
    msg_size * new_msg = (msg_size *)data;
    int N;
    int * move;

    if(0 == new_msg->size){
        (*done)++;
    } else {
        N = (new_msg->msg)[0];
        move = (int *)malloc(sizeof(int) * MOVE(N));
        (*tasks) += (new_msg->size / MOVE(N));
        (*done)++;
        switch(new_msg->size / MOVE(N)){
        case NEXT_ROW_COL:
            memcpy(move, new_msg->msg, MOVE(N) * sizeof(int));
            lqput(queue, move);
            move = (int *)malloc(sizeof(int) * MOVE(N));
            memcpy(move, (new_msg->msg) + MOVE(N), MOVE(N) * sizeof(int));
            lqput(queue, move);
            break;
        case NEXT_COL:
            memcpy(move, new_msg->msg, MOVE(N) * sizeof(int));
            lqput(queue, move);
            break;
        default:
            break;
        }
    }
    return NULL;
}

/* manager: request handler */
/* malloc called for msg_size */
void * dispatch_moves(void * data, void * queue, int * done, int * tasks){
    int N = *((int *)data);
    msg_size * new_msg = (msg_size *)malloc(sizeof(msg_size));
    int * move;

    if(*tasks != *done || NULL != lqget(queue)){
        move = (int *)lqgetrm(queue);
        if(NULL != move){
            new_msg->msg = move;
            new_msg->size = MOVE(N);
        } else {
            new_msg->msg = done;
            new_msg->size = 1;   
        }
    } else {
        new_msg->msg = NULL;
        new_msg->size = 0;
    }

    return new_msg;
}
