#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 2, Authorship Attribution, code base is assignment1
"""

from __future__ import division          #integer division
from collections import defaultdict
import random
import codecs          #to read and write unicode
import sys        #for command-line args
import string
from math import *

___author__ = 'Xiaochen Qi'
__date__ = 'Oct 03 2013'
__version__ = '1'

# inputs
paper_dir = 'federalist/'
Hamilton_files = [1, 6, 7, 8, 9, 11, 12, 13, 15, 16, 17, 21, 22, 23, 24, 25, 26, 27, 28, 29]
Madison_files = [10, 14, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 58]
test_files = [9, 11, 12, 47, 48, 58]
unk_files = [49, 50, 51, 52, 53, 54, 55, 56, 57, 62, 63]

# unigram
Hamilton_unigram = defaultdict(int)
Madison_unigram = defaultdict(int)

Hamilton_unigram['<UNK>'] = 1
Madison_unigram['<UNK>'] = 1

# bigram
unigram_weight = 0.2 # predefined unigram weight
Hamilton_pairs = defaultdict(int)
Madison_pairs = defaultdict(int)
Hamilton_bigram = defaultdict(float)
Madison_bigram = defaultdict(float)

# prob of author
P_Hamilton = 0.78
P_Madison = 0.22

# remove all punctuations
def punctuation_stripper(filename):
    """Read a file and count the frequency of each word"""
    f = codecs.open(filename, 'r', 'utf8')
    text = f.read()
    # remove first line
    text = text[text.find('\n')+1:]
    exclude = set(string.punctuation)
    new_text = ''.join(ch for ch in text if ch not in exclude)

    return new_text

# train a file, frequency is stored to a dict
def train_unigram(filename, dict_unigram):
    str_file = punctuation_stripper(filename)
    words = str_file.split()
    for word in words:
        dict_unigram[word.lower()]+=1 #lower or not?

# count a file for pairs
def count_bigram(filename, pairs):
    str_file = punctuation_stripper(filename)
    words = str_file.split()

    last_word = '<s>'
    for word in words:
        pairs[last_word.lower() + ' ' + word.lower()]+=1 #lower or not?
        last_word = word

def train_bigram(pairs, dict_unigram, dict_bigram):
    freq_unigram = freq_dict(dict_unigram)
    for pair in pairs.keys():
        p = 0.0
        if pair[0:pair.find(' ')] == '<s>':
            p = dict_unigram[pair[pair.find(' ') + 1:]] / sum(dict_unigram.values())
        else:
            p = pairs[pair] / dict_unigram[pair[0:pair.find(' ')]]
        # should interpolate while training?
        dict_bigram[pair] = (1 - unigram_weight) * p + unigram_weight * freq_by_word(freq_unigram, pair[pair.find(' ') + 1:])

# normalize a frequency dict
def freq_dict(dict_unigram):
    new_dict = defaultdict(float)
    token_count = sum(dict_unigram.values())
    for key in dict_unigram.keys():
        new_dict[key] = dict_unigram[key]/token_count
    return new_dict

# find frequency by key
def freq_by_word(dict_unigram, word):
    if word not in dict_unigram.keys():
        return dict_unigram['<UNK>']
    else:
        return dict_unigram[word]

# calc ppl of a file with a given model
def ppl_unigram(filename, dict_unigram):
    str_file = punctuation_stripper(filename)
    words = str_file.split()
    model = freq_dict(dict_unigram)
    cross_entropy = sum(-log(freq_by_word(model, word.lower()), 2)/len(words) for word in words)
    ppl = pow(2, cross_entropy)
    return ppl

# calc ppl of a file with bigram model
def ppl_bigram(filename, dict_unigram, dict_bigram, P_author):
    str_file = punctuation_stripper(filename)
    words = str_file.split()
    model = freq_dict(dict_unigram)
    cross_entropy = 0
    last_word = '<s>'
    for word in words:
        freq_word = 0
        if (last_word + ' ' + word.lower()) not in dict_bigram.keys():
            freq_word = freq_by_word(model, word.lower())
        else:
            freq_word = dict_bigram[last_word + ' ' + word.lower()]
        cross_entropy += (-log(freq_word, 2)/len(words))
        last_word = word.lower()
    ppl = pow(2, cross_entropy)

    log_P = len(words) * log(1 / ppl, 2) + log(P_author, 2)
    return (ppl, log_P)

if __name__=='__main__':   #main function
    
    # train unigram for Hamilton
    for name in Hamilton_files:
        if name not in test_files:
            train_unigram(paper_dir + str(name) + '.txt', Hamilton_unigram)
    # train unigram for Madison
    for name in Madison_files:
        if name not in test_files:
            train_unigram(paper_dir + str(name) + '.txt', Madison_unigram)

    # ppl by unigram model
    print 'unigram ppl'
    for name in test_files:
        ppl_Hamilton = ppl_unigram(paper_dir + str(name) + '.txt', Hamilton_unigram)
        ppl_Madison = ppl_unigram(paper_dir + str(name) + '.txt', Madison_unigram)
        print str(name) + '.txt: Hamilton:' + str(ppl_Hamilton)+ ' Madison:'+str(ppl_Madison)

    # bigram pairs
    for name in Hamilton_files:
        if name not in test_files:
            count_bigram(paper_dir + str(name) + '.txt', Hamilton_pairs)

    for name in Madison_files:
        if name not in test_files:
            count_bigram(paper_dir + str(name) + '.txt', Madison_pairs)

    # bigram model
    train_bigram(Hamilton_pairs, Hamilton_unigram, Hamilton_bigram)
    train_bigram(Madison_pairs, Madison_unigram, Madison_bigram)

    # ppl by bigram model
    print 'bigram ppl'
    for name in test_files:
        ppl_Hamilton = ppl_bigram(paper_dir + str(name) + '.txt', Hamilton_unigram, Hamilton_bigram, P_Hamilton)
        ppl_Madison = ppl_bigram(paper_dir + str(name) + '.txt', Madison_unigram, Madison_bigram, P_Madison)
        print str(name) + '.txt: Hamilton:' + str(ppl_Hamilton[0])+ ' Madison:'+str(ppl_Madison[0])
    
    #
    # retrain a bigram model with unigram weight = 0.2
    # clear all dicts
    Hamilton_pairs.clear()
    Hamilton_unigram.clear()
    Hamilton_bigram.clear()
    Madison_pairs.clear()
    Madison_unigram.clear()
    Madison_bigram.clear()
    Hamilton_unigram['<UNK>'] = 1
    Madison_unigram['<UNK>'] = 1

    # we need unigram for bigram
    for name in Hamilton_files:
        train_unigram(paper_dir + str(name) + '.txt', Hamilton_unigram)
        count_bigram(paper_dir + str(name) + '.txt', Hamilton_pairs)
    for name in Madison_files:
        train_unigram(paper_dir + str(name) + '.txt', Madison_unigram)
        count_bigram(paper_dir + str(name) + '.txt', Madison_pairs)

    # bigram model
    train_bigram(Hamilton_pairs, Hamilton_unigram, Hamilton_bigram)
    train_bigram(Madison_pairs, Madison_unigram, Madison_bigram)

    # ppl by bigram model
    print 'bigram ppl for unknown-author essays'
    for name in unk_files:
        ppl_Hamilton = ppl_bigram(paper_dir + str(name) + '.txt', Hamilton_unigram, Hamilton_bigram, P_Hamilton)
        ppl_Madison = ppl_bigram(paper_dir + str(name) + '.txt', Madison_unigram, Madison_bigram, P_Madison)
        print str(name) + '.txt: Hamilton:' + str(ppl_Hamilton[0])+ ' Madison:'+str(ppl_Madison[0])
        print str(name) + '.txt: given Hamilton:' + str(ppl_Hamilton[1])+ ' given Madison:'+str(ppl_Madison[1])










