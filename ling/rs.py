#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 5, random sentence
"""

from __future__ import division          #integer division
from collections import defaultdict
from random import *
import codecs          #to read and write unicode
import sys        #for command-line args
import string
from math import *

___author__ = 'Xiaochen Qi'
__date__ = 'Oct 26 2013'
__version__ = '1'

# language model
grammar_dict = defaultdict()

# read model from file
def read_grammar(filename, grammar_dict):
    text = codecs.open(filename, 'r', 'utf8')
    for line in text:
        if line[0] == '#' or not line.strip(): # comment & blank lines
            pass
        else:
            parts = line.split()
            if parts[0] not in grammar_dict:
                grammar_dict[parts[0]] = []
                grammar_dict[parts[0]].append(' '.join(parts[1:]))
            else:
                grammar_dict[parts[0]].append(' '.join(parts[1:]))


# recursion, expand a non-terminal
# there will be cases maximum recursion depth exceeded
def expand_N(grammar_dict, token):
    if token in grammar_dict.keys(): # token in non-terminal
        expansions = grammar_dict[token]
        expansion = expansions[randint(0, len(expansions) - 1)]
        sentence = []
        for symbol in expansion.split():
            sentence += expand_N(grammar_dict, symbol)
        return sentence
    else: # token in a terminal
        return [token]



if __name__=='__main__':   #main function
    read_grammar(sys.argv[1], grammar_dict)

    terminals = expand_N(grammar_dict, 'ROOT')

    print ' '.join(terminals[:-1]) + terminals[-1]





    











