"""
The following code is copied from Daniel's Notebook, and the post is titled "Training a POMDP (with
Python)". We changed the code based on our need.

The link of the original post:
https://danielmescheder.wordpress.com/2011/12/05/training-a-pomdp-with-python/

Note the blog uses the following terms:
  - input sequence (xs): Each input symbol in input sequence refers to an action in POMDP
                         terminology
  - output sequence (ys): Each output symbol in output sequence refers to an observation
                          in POMDP terminology
"""

class ObservableMarkovModel(object):
    def __init__(self, alist, c, init):
        """
        Params:
          - alist: A dictionary of 2D numpy arrays. `alist` stores the probability of state
                   transitions. `alist[a][s1, s2]` captures the probability of P(s2 | s1, a),
                   where `s1` is the current state, `a` is the performed action, and `s2` is
                   the next state. The structure of alist is the following: alist is a dictionary,
                   whose keys are the action symbols, and the values are the 2D numpy array. It is
                   expected to be a square matrix and the width and the height are equal to number
                   of states.
          - c: A 2D numpy array. `c` captures the observation probability. `c[o, s]` captures the
               probability of P(o | s), where `o` is the observation, and `s` is the state.
          - init: A 1D numpy array. `init` captures the initial state distribution probability.
                  `init[s]` means the probability that the agent starts with the state `s`.
        """
        self.alist = alist
        self.c = c
        self.ns = c.shape[1]
        self.os = c.shape[0]
        self.inps = len(alist)
        self.init = init
