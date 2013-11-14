
/* 
 * nqueen.h -- public interface to the nqueen module
 */
#define public
#define private static


#define OUT_OF_BOARD 0
#define NEXT_COL 1
#define NEXT_ROW_COL 2

#define MOVE(N) (N + 3)
/* a move is stored in an int array: N, row, col, board */

/* the Move struct, may not be useful */
typedef struct Move{
    int * board;
    int N;
    int row;
    int col;
}Move;

/* place queen on the board */
void Queens(int N);

int same_board(int * board1, int * board2, int N);

void * dispatch_moves(void * data, void * queue, int * done, int * tasks);
void * new_moves(void * data, void * queue, int * done, int * tasks);
void * post_placement(void * args);

void print_board(int * board, int N);
