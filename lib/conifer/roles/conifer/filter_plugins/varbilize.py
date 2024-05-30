from __future__ import absolute_import
import re
import json
"""
 Conifer conifer_scrub() and conifer_pluck() filters to interrogate
 variables that have not been defined.
"""

class Varbilize:

  def __init__(self, regexp):
    '''
    regexp - regular expression string
    '''
    self.re_filter = re.compile('({0})'.format(regexp))

  def scrub(self, d):
    '''
    Remove keys from dictionary `d` whose value is None or matches regex `re_filter`.
    These are variables the user has not defined, removed from the dictionary
    to force templates to fail with `undefined` exceptions.
    The inverse of pluck().

    Given,

    { 'a':
      { 'b': None,
        'c': 1
      }
    }

    returns,

    {'a':
      {'
        c': 1
      }
    }
    '''
    if isinstance(d, dict):
      print("Input d IS a dictionary!")
      print(d)
      for k in d.keys():
        if isinstance(d[k], dict):
          print("Found a subdictionary!")
          print(d[k])
          self.scrub(d[k])
          if len(d[k]) == 0:
            del d[k]
        elif d[k] is None or (isinstance(d[k], basestring) and self.re_filter.match(d[k])):
          print("Found none or string")
          print(d[k])
          del d[k]
        else:
          print("Found non-dictionary value!")
          print(d[k])
    else:
      print("Input d is NOT a dictionary!")
      print(d)

    # check again!
    print("What about now???")
    if isinstance(d, dict):
      print("Input d IS a dictionary!")
    else:
      print("Input d is NOT a dictionary!")

    return json.dumps(d)

  def pluck(self, id, od = {}):
    '''
    Create and return a dictionary taken from dictionary `d` whose value is
    None or matches regex `re_filter`. These are variables the user has not
    defined.
    The inverse of scrub().

    Given,

    { 'a':
      { 'b': None,
        'c': 1
      }
    }

    returns,

    {'a':
      {
        'b': None
      }
    }
    '''
    for k in id.keys():
      if isinstance(id[k], dict):
        od[k] = {}
        self.pluck(id[k], od[k])
        if len(od[k]) == 0:
          del od[k]
      elif id[k] is None or (isinstance(id[k], basestring) and self.re_filter.match(id[k])):
        od[k] = id[k]

    return json.dumps(od)


def conifer_pluck(a, term = '.*'):
  vb = Varbilize(term)
  return vb.pluck(a)

def conifer_scrub(a, term = '.*'):
  vb = Varbilize(term)
  return vb.scrub(a)

class FilterModule(object):
  '''
  custom jinja2 filters
  '''

  def filters(self):
    return {
      'conifer_scrub': conifer_scrub,
      'conifer_pluck': conifer_pluck,
    }
