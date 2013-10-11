#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 3, viterbi and forward
"""

from __future__ import division          #integer division
from collections import defaultdict
import random
import codecs          #to read and write unicode
import sys        #for command-line args
import string
from math import *

___author__ = 'Xiaochen Qi'
__date__ = 'Oct 10 2013'
__version__ = '1'

# class for a table entry
class Cell:
    p_viterbi = 0.0
    p_forward = 0.0
    tag_viterbi = ""
    def __init__(self):
        pass

# language model
emission_dict = defaultdict()
transition_dict = defaultdict()

# read model from file
def read_model(filename, model_dict):
    text = codecs.open(filename, 'r', 'utf8')
    for line in text:
        parts = line.split()
        model_dict[parts[1]+'|'+parts[0]] = float(parts[2])

def read_observation(filename):
    f = codecs.open(filename, 'r', 'utf8')
    text = f.read()
    tokens = text.split()
    return tokens

# return p_viterbi, p_forward, tag_viterbi
def probabilities(idx, tag, token, last_dict):
    if idx == 0:
        # last_dict is None
        key_emission = token + '|' + tag
        key_transition = tag + '|#'
        p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
        p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
        p_forward = p_viterbi = p_emission * p_transition
        return (p_viterbi, p_forward, '')
    else:
        key_emission = token + '|' + tag
        max_p = 0.0
        sum_p = 0.0
        best_tag = ''
        for last_tag in last_dict.keys():
            key_transition = tag + '|' + last_tag
            p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
            p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
            sum_p += last_dict[last_tag].p_forward * p_emission * p_transition
            p_viterbi = last_dict[last_tag].p_viterbi * p_emission * p_transition
            if p_viterbi > max_p:
                max_p = p_viterbi
                best_tag = last_tag

        return (max_p, sum_p, best_tag)

# return p_viterbi, p_forward, tag_viterbi, using logs
def probabilities_log(idx, tag, token, last_dict):
    if idx == 0:
        # last_dict is None
        key_emission = token + '|' + tag
        key_transition = tag + '|#'
        p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
        p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
        p_forward = p_emission * p_transition
        if p_forward == 0.0:
            p_viterbi = -sys.maxint
        else:
            p_viterbi = log(p_forward, 2)
        return (p_viterbi, p_forward, '')
    else:
        key_emission = token + '|' + tag
        max_p = -sys.maxint
        sum_p = 0.0
        best_tag = ''
        for last_tag in last_dict.keys():
            key_transition = tag + '|' + last_tag
            p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
            p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0

            # forward
            sum_p += last_dict[last_tag].p_forward * p_emission * p_transition
            
            # viterbi
            if p_transition == 0.0 or p_emission == 0.0 or last_dict[last_tag].p_viterbi == -sys.maxint:
                p_viterbi = -sys.maxint
            else:
                p_viterbi = last_dict[last_tag].p_viterbi + log(p_emission, 2) + log(p_transition, 2)
            
            if p_viterbi > max_p:
                max_p = p_viterbi
                best_tag = last_tag

        return (max_p, sum_p, best_tag)


if __name__=='__main__':   #main function
    # get all inputs
    read_model(sys.argv[1], emission_dict)
    read_model(sys.argv[2], transition_dict)

    tokens = read_observation(sys.argv[3])

    # for key in emission_dict.keys():
    #     print key  + ' ' + str(emission_dict[key])

    # executing while building the table
    table = []
    total_forwards = []
    for idx, token in enumerate(tokens):
        token_dict = defaultdict()
        #token_dict['#token'] = tokens[idx]
        for conditional_tag in transition_dict.keys():
            tag = conditional_tag[0:conditional_tag.find('|')]
            if tag not in token_dict.keys():
                token_dict[tag] = Cell()
        
        # calc
        total_forward = 0.0
        for tag in token_dict.keys():
            p_tuple = probabilities_log(idx, tag, token, table[idx - 1] if idx > 0 else None)

            # non-scaled forward values, can be very small
            token_dict[tag].p_forward = p_tuple[1]
            token_dict[tag].p_viterbi = p_tuple[0]
            token_dict[tag].tag_viterbi = p_tuple[2]



            # scaled forward value
            total_forward += p_tuple[1]
        total_forwards.append(total_forward)
        for tag in token_dict.keys():
            token_dict[tag].p_forward /= total_forward



        table.append(token_dict)

    # reverse visit the table for result
    tag_seq = []
    p_total = 0.0
    last_tag = ''
    for idx, token_dict in enumerate(reversed(table)):
        if idx == 0:
            # unscaled forward
            # p_total = sum(cell.p_forward for cell in token_dict.values())

            # scaled forward, log space
            p_total = sum(log(total_forward, 2) for total_forward in total_forwards)

            max_p = -sys.maxint
            best_tag = ''
            for tag in token_dict.keys():
                if token_dict[tag].p_viterbi > max_p:
                    max_p = token_dict[tag].p_viterbi
                    best_tag = tag
            last_tag = best_tag
            tag_seq.insert(0, best_tag)
            last_tag = best_tag = token_dict[best_tag].tag_viterbi
            tag_seq.insert(0, best_tag)
        else:
            last_tag = best_tag = token_dict[last_tag].tag_viterbi
            tag_seq.insert(0, best_tag)
            

    print 'tag sequence: ' + ' '.join(tag_seq)
    print 'probability of observation: ' + str(p_total)



    











