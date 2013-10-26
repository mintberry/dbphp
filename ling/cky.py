#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 5, cky
"""

from __future__ import division          #integer division
from collections import defaultdict
import codecs          #to read and write unicode
import sys        #for command-line args
import string
import operator
from random import *
from math import *

___author__ = 'Xiaochen Qi'
__date__ = 'Oct 26 2013'
__version__ = '1'

# class for a rule
class Rule:
    prob = 0.0
    content = None
    def __init__(self):
        pass

# class for tree node
class Node:
    i = 0  # #row
    j = 0  # #column
    prob = 0.0
    this = None
    lchild = None
    rchild = None
    def __init__(self, arg_i, arg_j, content, probability):
        self.i = arg_i
        self.j = arg_j
        self.this = content
        self.prob = probability
        pass

# language model
inverse_dict = defaultdict()


# read model from file
def read_grammar(filename, inverse_dict):
    text = codecs.open(filename, 'r', 'utf8')
    for line in text:
        if line[0] == '#' or not line.strip(): # comment
            pass
        else:
            parts = line.split()
            right = ' '.join(parts[1:-1])

            # inverse grammar
            if right not in inverse_dict:
                inverse_dict[right] = []
                new_rule = Rule()
                new_rule.content = parts[0]
                new_rule.prob = float(parts[-1])
                inverse_dict[right].append(new_rule)
            else:
                new_rule = Rule()
                new_rule.content = parts[0]
                new_rule.prob = float(parts[-1])
                inverse_dict[right].append(new_rule)


def read_observation(filename):
    f = codecs.open(filename, 'r', 'utf8')
    # text = f.read()
    lines = []
    for line in f:
        tokens = line.split()
        lines.append(tokens)
    return lines

def write_node(f, node, level):
    if None != node:
        write_node(f, node.lchild, '    ' + level)
        f.write(level + node.this + '\n')
        write_node(f, node.rchild, '    ' + level)

def write_tree(filename, root, sentence):
    text = codecs.open(filename, 'w', 'utf8')
    text.write(sentence + '\n')
    write_node(text, root, '')
    text.close()

# return a list of possible Nodes at i, j
def new_nodes(line, table, i, j, size, inverse_dict):
    ret = []
    if i == 0: # leaf nodes
        word = line[j]
        for rule in inverse_dict[word]:
            new_node = Node(i, j, rule.content, rule.prob)
            ret.append(new_node)
        if len(ret) == 0:
            print 'ERROR: Undeclared terminal'
    else: # non-leaves
        for k in range(i):
            left = table[k][j]
            right = table[i - 1 - k][j + k + 1]
            # go through all nodes in each cell
            for lnode in left:
                for rnode in right:
                    inverse_key = lnode.this + ' ' + rnode.this
                    if inverse_key in inverse_dict.keys(): # get matched case rules
                        for rule in inverse_dict[inverse_key]:
                            new_node = Node(i, j, rule.content, rule.prob * lnode.prob * rnode.prob)
                            new_node.lchild = lnode
                            new_node.rchild = rnode
                            ret.append(new_node)
                    else:
                        pass # no match

    return ret

# parse a sentence
def parse_line(line, inverse_dict):
    size = len(line)
    # total_cells = size * (size + 1) / 2
    table = []
    # loop through each row
    for i in range(size):
        cells = size - i
        row = []
        for j in range(cells):
            # cell = [] 
            # compute a list of possible Nodes
            cell = new_nodes(line, table, i, j, size, inverse_dict)
            row.append(cell)
        table.append(row)

    final_nodes = table[size - 1][0]
    prob = 0.0
    root = None
    for node in final_nodes:
        if prob < node.prob:
            prob = node.prob
            root = node

    return (table, root)


if __name__=='__main__':   #main function
    read_grammar(sys.argv[1], inverse_dict)

    lines = read_observation(sys.argv[2])

    # build parsing table
    for idx, line in enumerate(lines):
        result = parse_line(line, inverse_dict)
        write_tree('parse_tree_' + str(idx) + '.txt', result[1], ' '.join(line))








    











