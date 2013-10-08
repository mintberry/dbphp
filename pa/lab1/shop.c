/*
 * qtest.c -- regression test for the queue module
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "./queue.h"

typedef struct item{
    char name[80];
    int amount;
}item;

void print_item(void *shopping_item){
    item *item_ptr = (item *)shopping_item;
    printf("%s: %d\n", item_ptr->name, item_ptr->amount);
}

int match_item(void *elementp, void *keyp){
    item *item1 = (item *)elementp;
    item *item2 = (item *)keyp;
    if(item1 == item2){
        return 0;
    } else {
        return 1;
    }
}

public int main(int arcc, char *argv[]) {
  void *shopping_list = qopen();
  void *shopping_list2 = qopen();
  FILE *file = fopen ("shoppinglist", "rt");
  char line[80];
  item *removed_item = NULL;

  /* add your code here */
  printf("hello there...\n");

  while(NULL != fgets(line, 80, file)){
      item *new_item = (item *)malloc(sizeof(item));
      item *new_item2 = (item *)malloc(sizeof(item));
      line[strlen(line) - 1] = (char)0;
      strcpy(new_item->name, line);
      memcpy(new_item2, new_item, sizeof(item));
      if(NULL != fgets(line, 80, file)){
          new_item->amount = new_item2->amount = atoi(line);
          qput(shopping_list, new_item);
          qput(shopping_list2, new_item2);
      } else {
          printf("input error\n");
      }
  }
  fclose(file);
  qconcat(shopping_list, shopping_list2);
  qapply(shopping_list, print_item);  
  while((removed_item = (item *)qremove(shopping_list, match_item, qget(shopping_list))) != NULL){
      free(removed_item);
  }
  qclose(shopping_list);
  qclose(shopping_list2);

  return EXIT_SUCCESS;
}

