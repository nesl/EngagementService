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

Also, to clarify, the proposed algorithm in the blog is to train a Partially Observable Markov
Chain (POMC) instead of POMDP, because it doesn't consider the reward mechanism.
"""


import numpy as np
from collections import Counter

from pomc import PartiallyObservableMarkovChain


def _column(np_1d_arr):
    return np.array([np_1d_arr]).T

def initialize_uniform_pomc(num_states, action_list, num_observables):
    """
    `initialize_uniform_pomc()` generates a PartiallyObservableMarkovChain, and all the parameters
    have the same weights.

    Params:
      - num_states: An int.
      - action_list: A list containing all the action symbols. There is no restriction on the
                     type of each action symbol, for example, both int and string work.
      - num_observables: An int.
    """
    alist = {a: np.ones((num_states, num_states)) / num_states for a in action_list}
    c = np.ones((num_observables, num_states)) / num_observables
    init = np.ones(num_states) / num_states
    return PartiallyObservableMarkovChain(alist, c, init)

def initialize_random_pomc(num_states, action_list, num_observables):
    """
    `initialize_random_pomc()` generates a PartiallyObservableMarkovChain, and all the parameters
    are drawn from Dirichlet distributions.

    Params:
      - num_states: An int.
      - action_list: A list containing all the action symbols. There is no restriction on the
                     type of each action symbol, for example, both int and string work.
      - num_observables: An int.
    """
    num_states_of_ones = np.ones(num_states)
    alist = {a: np.random.dirichlet(num_states_of_ones, size=num_states) for a in action_list}

    # `c` is a obserables-by-states matrix, and each column is summed to 1.
    c = np.random.dirichlet(np.ones(num_observables), size=num_states).T

    init = np.random.dirichlet(num_states_of_ones)
    return PartiallyObservableMarkovChain(alist, c, init)

def make_tableaus(xs, ys, m):
    """
    `make_tableaus()` generates 3 tableaus: alpha, beta, and N tableaus.

    Params:
      - xs: A list. `xs` stores a sequence of actions and `xs[t]` is the action symbol at time `t`.
            The original definition can be found in improve_params().
      - ys: A list. `ys` stores a sequence of observations and `ys[t]` is the observation symbol at
            time `t`. The original definition can be found in improve_params().
      - m: An PartiallyObservableMarkovChain.

    Returns: (alpha, beta, N)
      - alpha: A 2D numpy array. Alpha tableau. The size is (length of sequence)-by-
               (number of states).
      - beta: A 2D numpy array. Beta tableau. The size is (length of sequence)-by-
               (number of states).
      - N: A 1D numpy array. N tableau. The size is the same as the length of the sequence.
    """
    slen = len(ys)  # sequence length
    alpha = np.zeros((slen, m.ns))
    beta  = np.zeros((slen, m.ns))
    gamma = np.zeros((slen, m.ns))
    N     = np.zeros(slen)

    # Initialize:
    gamma[0, :] = m.init * m.c[ys[0], :]
    N[0] = 1. / np.sum(gamma[0, :])
    alpha[0, :] = N[0] * gamma[0, :]
    beta[slen-1, :] = 1.

    for i in range(1, slen):
        gamma[i, :] = m.c[ys[i], :] * np.sum(m.alist[xs[i-1]] * alpha[i-1, :], axis=1)
        N[i] = 1. / np.sum(gamma[i, :])
        alpha[i, :] = N[i] * gamma[i, :]

    for i in range(slen-1, 0, -1):
        beta[i-1, :] = N[i] * np.sum(m.alist[xs[i-1]] * _column(m.c[ys[i], :] * beta[i, :]), axis=0)

    return alpha, beta, N


def state_estimates(tableaus):
    """
    TODO: not sure what the intention is
    Params:
      - tableaus: A list of 3 tableaus, which are alpha, beta, and N. Please see `make_tableaus()`
                  for more detail.
    Returns: A 2D numpy array whose size is (length of the sequence)-by-(number of states). This
             array captures the state estimation. Let's denote this array `ret`. `ret[t, s]` is the
             probability that the state is `s` at time `t`. Hence, the sum of each row is 1.
    """
    alpha, beta, _ = tableaus
    return alpha * beta


def transition_estimates(xs, ys, m, tableaus):
    """
    TODO: not sure what the intention is
    Params:
      - xs: A list. `xs` stores a sequence of actions and `xs[t]` is the action symbol at time `t`.
            The original definition can be found in improve_params().
      - ys: A list. `ys` stores a sequence of observations and `ys[t]` is the observation symbol at
            time `t`. The original definition can be found in improve_params().
      - m: An PartiallyObservableMarkovChain.
      - tableaus: A list of 3 tableaus, which are alpha, beta, and N. Please see `make_tableaus()`
                  for more detail.
    Returns: A 3D numpy array whose size is (number of states)-by-(number of states)-by-
             (length of the sequence). TODO not sure exactly how to interpret. Let's denote this
             array `ret`. `ret[sa, sb, t]` (TODO) seems to be the probability of transiting from
             `sa` to `sb` at time `t`. P(sa, sb, | t) or P(sb | sa, t)?
    """
    alpha, beta, N = tableaus
    seq_len = len(ys)
    result = np.zeros((m.ns, m.ns, seq_len))
    for t in range(seq_len - 1):
        a = m.alist[xs[t]]
        result[:, :, t] = a * alpha[t, :] * _column(m.c[ys[t+1], :]) * _column(beta[t+1, :]) * N[t+1]
    
    a = m.alist[xs[-1]]
    result[:, :, -1] = a * alpha[-1:, :]
    return result


def stateoutput_estimates(ys, num_observables, num_states, sestimate):
    """
    TODO: not sure what the intention is
    
    Params:
      - ys: A list. `ys` stores a sequence of observations and `ys[t]` is the observation symbol at
            time `t`. The original definition can be found in improve_params().
      - num_observables: An int.
      - num_states: An int.
      - sestimate: A 2D numpy array. TODO not sure what is `sestimate`. The size of `sestimate` is
                   (length of the sequence)-by-(number of states), and `sestimate[t, s]` means
                   probability that the state is `s` at time `t`. Please see `state_estimates()`
                   for more detail.
    Returns: A 3D numpy array whose size is (number of observables)-by-(number of states)-by-
             (length of the sequence). Haven't figured out the interpretation yet.
    """
    seq_len = len(ys)
    result = np.zeros((num_observables, num_states, seq_len))
    for t in range(seq_len):
        result[ys[t], :, t] = sestimate[t, :]
    return result


def improve_params(xs, ys, m):
    """
    TODO: not sure what the intention is
    
    Params:
      - xs: A list. `xs` captures a sequence of actions or "input symbols." `xs[t]` indicates the
            action symbol at time `t`.
      - ys: A list. `ys` captures a sequence of observations or "output symbols." `ys[t]` indicates
            the observation symbol at time `t`.
      - m: An PartiallyObservableMarkovChain.

    Returns: (alist, c). `alist` and `c` are the state transition probability and observation
             probability. Please see `PartiallyObservableMarkovChain` class for more detail.
    """
    seq_len = len(ys)

    tableaus = make_tableaus(xs, ys, m)
    estimates = state_estimates(tableaus)
    trans_estimates = transition_estimates(xs, ys, m, tableaus)
    sout_estimates = stateoutput_estimates(ys, m.os, m.ns, estimates)

    # Calculate the numbers of each input in the input sequence.
    action_freq = Counter(xs)
    
    sstates = {a: np.zeros((m.ns, 1)) for a in m.action_list}
    for t in range(seq_len):
        x = xs[t]
        sstates[x] += estimates[t:t+1,:].T/action_freq[x]

    # Estimator for transition probabilities
    alist = {a: np.zeros_like(matrix) for a, matrix in m.alist.items()}
    for t in range(seq_len):
        alist[xs[t]] += trans_estimates[:,:,t]/action_freq[xs[t]]
    #TODO: the following should be for a in range(m.action_list):
    for i in range(m.num_actions):
        alist[i] = alist[i]/(sstates[i].T)
        np.putmask(alist[i],(np.tile(sstates[i].T==0,(m.ns,1))),m.alist[i])

    c = np.zeros_like(m.c)
    for t in range(seq_len):
        x = xs[t]
        c += sout_estimates[:,:,t] / (action_freq[x] * m.num_actions * sstates[x].T)

    # Set the output probabilities to the original model if we have no state observation at all.
    sstatem = np.hstack(sstates).T
    mask = np.array([any([sstates[a][i] == 0. for a in m.action_list]) for i in range(m.ns)])
    np.putmask(c,(np.tile(mask,(m.os,1))),m.c)

    return alist, c


def get_likelihood(tableaus):
    _, _, N = tableaus
    return np.product(1. / N)

def get_log_likelihood(tableaus):
    _, _, N = tableaus
    return -np.sum(np.log(N))
