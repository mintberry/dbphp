#!/usr/bin/env python

"""
COMPUTATIONAL LINGUISTICS

assignment 1, code base is python tips from Sravana Reddy
"""

from __future__ import division          #integer division
from collections import defaultdict
import random
import codecs          #to read and write unicode
import sys        #for command-line args
import string
import math

___author__ = 'Xiaochen Qi'
__date__ = 'Sep 26 2013'
__version__ = '1'



def count_words(filename):
    """Read a file and count the frequency of each word"""
    text = codecs.open(filename, 'r', 'utf8')
    countsdict = defaultdict(int)
    
    for line in text:
        #ignore punctuation
        exclude = set(string.punctuation)
        line = ''.join(ch for ch in line if ch not in exclude)
        #split along whitespace. To split along any other symbol, line.split(','), etc.
        words = line.split()
        
        for word in words:
            countsdict[word.lower()]+=1
            
    return countsdict

if __name__=='__main__':   #main function
    wordcounts = count_words(sys.argv[1])   #filename is first parameter

    """type-token ratio"""
    type_count = len(wordcounts.keys())
    token_count = sum(wordcounts.values())
    print 'Number of types: ',(str(type_count)),',','Number of tokens: ',(str(token_count)),',','Ratio: ',str(round((token_count/type_count),2))

    """entropy"""
    probabilities = [freq/token_count for freq in wordcounts.values()]
    print 'Entropy of words: ',round(-sum(probability*math.log(probability,2) for probability in probabilities),2),'bits'

    """Word length distribution"""
    typelendict = defaultdict(int)
    tokenlendict = defaultdict(int)
    tokenlist = []
    for word in wordcounts.keys():
        typelendict[len(word)]+=1
    for key, value in wordcounts.iteritems():
        for x in range(value):
            tokenlist.append(key)
    for word in tokenlist:
        tokenlendict[len(word)]+=1
    print 'type length distribution'
    for key, value in typelendict.iteritems():
        print key,' ',value/type_count
    print 'token length distribution'
    for key, value in tokenlendict.iteritems():
        print key,' ',value/token_count

    """Word frequencies against rank"""
    print 'Word frequencies against rank'
    sorted_freq = sorted(probabilities, reverse=True)
    for index, freq in enumerate(sorted_freq):
        print index+1, ' ', freq



