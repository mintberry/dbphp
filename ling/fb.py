#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 4, forward-backward
"""

from __future__ import division          #integer division
from collections import defaultdict
import random
import codecs          #to read and write unicode
import sys        #for command-line args
import string
import operator
from random import *
from math import *

___author__ = 'Xiaochen Qi'
__date__ = 'Oct 17 2013'
__version__ = '1'

# threshold
likelihood = 0.0001
scaler_threashold = 0.000001
scaler = 1.0

def prod(factors):
    return reduce(operator.mul, factors, 1)

# class for a table entry
class Cell:
    p_forward = 0.0
    p_backward = 0.0
    def __init__(self):
        pass

# language model
emission_dict = defaultdict()
transition_dict = defaultdict()

# read model from file and random init
def read_model(filename, model_dict):
    text = codecs.open(filename, 'r', 'utf8')
    given_dict = defaultdict()
    for line in text:
        parts = line.split()
        model_dict[parts[1]+'|'+parts[0]] = uniform(0.1, 9.9)
        if parts[0] not in given_dict.keys():
            given_dict[parts[0]] = model_dict[parts[1]+'|'+parts[0]]
        else:
            given_dict[parts[0]] += model_dict[parts[1]+'|'+parts[0]]

    for condition in model_dict.keys():
        model_dict[condition] /= given_dict[condition[condition.find('|')+1:]]

def read_observation(filename):
    f = codecs.open(filename, 'r', 'utf8')
    # text = f.read()
    lines = []
    for line in f:
        tokens = line.split()
        lines.append(tokens)
    return lines

def prob_from_model(model, key):
    return model[key] if key in model.keys() else 0.0

# return p_forward
def forward(idx, tag, token, last_dict):
    if idx == 0:
        # last_dict is None
        key_emission = token + '|' + tag
        key_transition = tag + '|#'
        p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
        p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
        p_forward = p_emission * p_transition
        return p_forward
    else:
        key_emission = token + '|' + tag
        sum_p = 0.0
        for last_tag in last_dict.keys():
            key_transition = tag + '|' + last_tag
            p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
            p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
            sum_p += last_dict[last_tag].p_forward * p_emission * p_transition

        return sum_p

# return p_backward
def backward(tag, token, last_dict):
    # idx > 0
    sum_p = 0.0
    for last_tag in last_dict.keys():
        key_transition = last_tag + '|' + tag
        key_emission = token + '|' + last_tag
        p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
        p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
        sum_p += last_dict[last_tag].p_backward * p_emission * p_transition

    return sum_p

def forward_backward(tokens, emission_dict, transition_dict):
    # executing while building the table
    scalers_forward = []
    scalers_backward = []
    table = []
    for idx, token in enumerate(tokens):
        token_dict = defaultdict()
        for conditional_tag in transition_dict.keys():
            tag = conditional_tag[0:conditional_tag.find('|')]
            if tag not in token_dict.keys():
                token_dict[tag] = Cell()
        
        # compute forward
        for tag in token_dict.keys():
            token_dict[tag].p_forward = forward(idx, tag, token, table[idx - 1] if idx > 0 else None)

        if sum(cell.p_forward for cell in token_dict.values()) < scaler_threashold:
            scalers_forward.append(scaler)
        else:
            scalers_forward.append(1.0)

        for tag in token_dict.keys():
            token_dict[tag].p_forward *= scalers_forward[idx]


        table.append(token_dict)

    # compute backward
    for idx, token_dict in enumerate(reversed(table)):
        if idx == 0:
            for cell in token_dict.values():
                cell.p_backward = 1.0
            scalers_backward.append(1.0);
        else:
            last_dict = table[len(table) - idx]
            for tag in token_dict.keys():
                token_dict[tag].p_backward = backward(tag, tokens[len(table) - idx], last_dict)

            if sum(cell.p_backward for cell in token_dict.values()) < scaler_threashold:
                scalers_backward.append(scaler)
            else:
                scalers_backward.append(1.0)

            for tag in token_dict.keys():
                token_dict[tag].p_backward *= scalers_backward[idx]

            

    # reverse visit the table for result
    # p_total = 0.0
    # p_total = log(sum(cell.p_forward for cell in table[-1].values()), 2) - sum(log(scaler, 2) for scaler in scalers_forward)

    # print 'probability of observation(forward): 2 ^ ' + str(p_total)

    # total backward
    # p_total = 0.0
    # for tag in table[0].keys():
    #     key_transition = tag + '|' + '#'
    #     key_emission = tokens[0] + '|' + tag
    #     p_transition = transition_dict[key_transition] if key_transition in transition_dict.keys() else 0.0
    #     p_emission = emission_dict[key_emission] if key_emission in emission_dict.keys() else 0.0
    #     p_total += table[0][tag].p_backward * p_transition * p_emission
    # p_total = log(p_total, 2) - sum(log(scaler, 2) for scaler in scalers_backward)
    # print 'probability of observation(backward): 2 ^ ' + str(p_total)

    p_total = sum(cell.p_forward for cell in table[-1].values())
    # print 'probability of observation: ' + str(p_total)

    return (table, scalers_forward, scalers_backward, p_total)


def expectation(tokens, table, scalers_forward, scalers_backward, emission_dict, transition_dict, total, emission_count, transition_count):
    # emission_count = defaultdict()
    # transition_count = defaultdict()
    for idx, token_dict in enumerate(table):
        token = tokens[idx]
        # emission
        for tag in token_dict.keys():
            emission_key = token + '|' + tag
            if emission_key not in emission_count.keys():
                emission_count[emission_key] = token_dict[tag].p_forward / prod(scalers_forward[:idx+1]) * token_dict[tag].p_backward /\
                prod(scalers_backward[:len(scalers_backward) - idx]) / total
            else:
                emission_count[emission_key] += token_dict[tag].p_forward / prod(scalers_forward[:idx+1]) * token_dict[tag].p_backward /\
                prod(scalers_backward[:len(scalers_backward) - idx]) / total


        # transition
        if idx != 0:
            last_dict = table[idx - 1]
            for tag in token_dict.keys():
                for last_tag in last_dict.keys():
                    transition_key = tag + '|' + last_tag
                    emission_key = tokens[idx] + '|' + tag
                    if transition_key not in transition_count.keys():
                        transition_count[transition_key] = last_dict[last_tag].p_forward / prod(scalers_forward[:idx]) * token_dict[tag].p_backward /\
                            prod(scalers_backward[:len(scalers_backward) - idx]) * prob_from_model(emission_dict, emission_key) * \
                            prob_from_model(transition_dict, transition_key) / total
                    else:
                        transition_count[transition_key] += last_dict[last_tag].p_forward / prod(scalers_forward[:idx]) * token_dict[tag].p_backward /\
                            prod(scalers_backward[:len(scalers_backward) - idx]) * prob_from_model(emission_dict, emission_key) * \
                            prob_from_model(transition_dict, transition_key) / total
        else:
            # start with #
            for tag in token_dict.keys():
                transition_key = tag + '|#'
                emission_key = tokens[idx] + '|' + tag
                if transition_key not in transition_count.keys():
                    transition_count[transition_key] = token_dict[tag].p_backward / prod(scalers_backward[:len(scalers_backward) - idx]) * \
                    prob_from_model(transition_dict, transition_key) * prob_from_model(emission_dict, emission_key) / total
                else:
                    transition_count[transition_key] += token_dict[tag].p_backward / prod(scalers_backward[:len(scalers_backward) - idx]) * \
                    prob_from_model(transition_dict, transition_key) * prob_from_model(emission_dict, emission_key) / total


def new_model(count_dict):
    given_dict = defaultdict()
    for condition in count_dict.keys():
        given = condition[condition.find('|')+1:]
        if given not in given_dict.keys():
            given_dict[given] = count_dict[condition]
        else:
            given_dict[given] += count_dict[condition]

    for condition in count_dict.keys():
        count_dict[condition] /= given_dict[condition[condition.find('|')+1:]]

    return dict(count_dict)

def write_model(filename, new_dict):
    text = codecs.open(filename, 'w', 'utf8')
    for condition in new_dict.keys():
        text.write(condition[condition.find('|') + 1:] + ' ' + condition[0:condition.find('|')] + ' ' + str(new_dict[condition]) + '\n')

    text.close()


if __name__=='__main__':   #main function
    # get all inputs
    read_model(sys.argv[1], emission_dict)
    read_model(sys.argv[2], transition_dict)

    lines = read_observation(sys.argv[3])

    # add while
    emission_count = defaultdict()
    transition_count = defaultdict()
    last_likelihood = 0.0

    while True:
        emission_count.clear()
        transition_count.clear()
        cur_likelihood = 0.0
        for line in lines:
            # forward-backward for each word
            (table, scalers_forward, scalers_backward, total) = forward_backward(line, emission_dict, transition_dict)
            cur_likelihood += log(total, 2)
            # em a new model, yield two dicts(counts)
            expectation(line, table, scalers_forward, scalers_backward, emission_dict, transition_dict, total,\
                emission_count, transition_count)


        # for condition in transition_count:
        #     print condition + ' ' + str(transition_count[condition])

        emission_dict = new_model(emission_count)
        transition_dict = new_model(transition_count)

        print cur_likelihood
        if fabs(cur_likelihood - last_likelihood) < likelihood:
            break

        last_likelihood = cur_likelihood

    write_model('emit-2state.txt', emission_dict)                        
    write_model('trans-2state.txt', transition_dict)


    # kongo = codecs.open('kikongo-UDHR.txt', 'r', 'utf8')
    # kongo2 = codecs.open('kikongo-UDHR-words.txt', 'w', 'utf8')
    # tkongo = kongo.read()
    # exclude = set(string.punctuation)
    # tkongo = ' '.join(ch for ch in tkongo.split() if ch not in exclude)
    # word_dict = defaultdict()
    # for word in tkongo.split():
    #     if word.lower() not in word_dict.keys():
    #         word_dict[word.lower()] = 0
    #         kongo2.write(' '.join(word.lower()) + '\n')

    # kongo2.close()
    # kongo.close()






    











