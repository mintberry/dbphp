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
import string, re #regex

___author__ = 'Xiaochen Qi'
__date__ = 'Sep 26 2013'
__version__ = '1'

#regex for tokens, can cover most cases
with_punctuation = r'\s(\w+|\d{1,3}(\,\d{1,3})*|\d+\.\d+)[\.\,][\s"]'
# there can be more exceptions
exceptions_with_punctuation = r"""Mr\.|Ms\.|Mrs\.|Nov\.|Jan\.|Dec\.|Co\.|Inc\.|Dr\.|\s[A-Z]\.|
    Feb\.|Mar\.|Apr\.|May\.|Jun\.|Jul\.|Oct\.|Sep\.|
    """
common_punctuation = r'\!|\?|\"|\%|\$|\#'
with_not = r'n\'t'
years = r'\'\d+'
bracket = r'\(|\)'
number = r'\d+[\,\d{3}]*'
# TO DO: Dec. 10.|Ill.|U.S..

def replace_common(mat):
    old = mat.group(0)
    new = ' ' + old + ' '
    return new

def replace_not(mat):
    old = mat.group(0)
    new = ' ' + old
    return new

def replace_bracket(mat):
    old = mat.group(0)
    new = ('-LCB- ' if old=='(' else ' -RCB-')
    return new

def replace_fun(mat):
    old = mat.group(0)
    new = old
    if re.search(exceptions_with_punctuation,old) == None:
        new = old[0:len(old) - 2]+' '+old[len(old) - 2]+ (' ' if old[len(old) - 1]==' ' else ' '+
            old[len(old) - 1])+('\n' if old[len(old) - 2] == '.' else '')
    return new

def sub_text(filename):
    """Read a file and count the frequency of each word"""
    f = codecs.open(filename, 'r', 'utf8')
    text = f.read()
    # patterns
    pat_punctuation = re.compile(with_punctuation, re.M)
    pat_comma = re.compile(common_punctuation, re.M)
    pat_not = re.compile(with_not, re.M)
    pat_years = re.compile(years, re.M)
    pat_bracket = re.compile(bracket, re.M)

    # do the subs
    new_text = pat_comma.sub(replace_common, text)
    new_text = pat_punctuation.sub(replace_fun, new_text)
    new_text = pat_not.sub(replace_not, new_text)
    new_text = pat_years.sub(replace_not, new_text)
    new_text = pat_bracket.sub(replace_bracket, new_text)
    return new_text

if __name__=='__main__':   #main function
    new_text = sub_text(sys.argv[1])   #filename is first parameter
    f = open(sys.argv[2], 'w')
    f.write(new_text)
    f.close();




