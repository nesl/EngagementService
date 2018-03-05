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


import numpy as np

from observable_markov_model import ObservableMarkovModel


def initialize_uniform_pomdp_model(num_states, action_list, num_observables):
    alist = {a: np.ones((num_states, num_states)) / num_states for a in action_list}
    c = np.ones((num_observables, num_states)) / num_observables
    init = np.ones(num_states) / num_states
    return ObservableMarkovModel(alist, c, init)

def initialize_random_pomdp_model(num_states, action_list, num_observables):
    num_states_of_ones = np.ones(num_states)
    alist = {a: np.random.dirichlet(num_states_of_ones, size=num_states) for a in action_list}

    # `c` is a obserables-by-states matrix, and each column is summed to 1.
    c = np.random.dirichlet(np.ones(num_observables), size=num_states).T

    init = np.random.dirichlet(num_states_of_ones)
    return ObservableMarkovModel(alist, c, init)

def make_tableaus(xs, ys, m):
    """
    `make_tableaus()` generate 3 tableaus: alpha, beta, and N tableaus.

    Params:
      - xs: A list. `xs` captures a sequence of actions or "input symbols." `xs[t]` indicates the
                  action symbol at time `t`.
      - ys: A list. `ys` captures a sequence of observations or "output symbols." `ys[t]` indicates
                  the observation sybol at time `t`.
      - m: ObservableMarkovModel.

    Returns: (alpha, beta, N)
      - alpha: Alpha tableau
      - beta: Beta tableau
      - N: N tableau
    """
    slen = len(ys)  # sequence length
    alpha = np.zeros((slen, m.ns))
    beta  = np.zeros((slen, m.ns))
    gamma = np.zeros((slen, m.ns))
    N     = np.zeros((slen, 1))

    # Initialize:
    gamma[0:1, :] = m.init.T * m.c[ys[0]:ys[0]+1, :]
    N[0, 0] = 1 / np.sum(gamma[0:1,:])
    alpha[0:1, :] = N[0,0] * gamma[0:1, :]
    beta[slen-1:slen, :] = np.ones((1, m.ns))

    for i in range(1, slen):
        gamma[i:i+1, :] = m.c[ys[i]:ys[i]+1,:] * np.sum((m.alist[xs[i-1]].T*alpha[i-1:i,:].T), axis=0)
        N[i,0] = 1 / np.sum(gamma[i:i+1,:])
        alpha[i:i+1,:] = N[i,0]*gamma[i:i+1,:]
    for i in range(slen-1, 0, -1):
        beta[i-1:i] = N[i] * np.sum(m.alist[xs[i-1]] * (m.c[ys[i]:ys[i]+1,:] * beta[i:i+1,:]).T, axis=0)

    return alpha, beta, N


def state_estimates(xs, ys, m, tableaus=None):
    """
    TODO: not sure what the intention is
    Returns: A 2D numpy array whose size is (length of the sequence)-by-(number of states). This
             array captures the state estimation. Let's denote this array ret. `ret[t, s]` is the
             probability that the state is `s` at time `t`. Hence, the sum of each row is 1.
    """
    if tableaus is None:
        tableaus = make_tableaus(xs,ys,m)
    alpha, beta, N = tableaus
    return alpha * beta


def transition_estimates(xs, ys, m, tableaus=None):
    """
    TODO: not sure what the intention is
    """
    if tableaus is None:
        tableaus = make_tableaus(xs,ys,m)
    alpha,beta,N = tableaus
    result = np.zeros((m.ns,m.ns,len(ys)))
    for t in range(len(ys)-1):
        a = m.alist[xs[t]]
        result[:,:,t] = a*alpha[t:t+1,:]*m.c[ys[t+1]:ys[t+1]+1,:].T*beta[t+1:t+2,:].T*N[t+1,0]
    a = m.alist[xs[len(ys)-1]]
    result[:,:,len(ys)-1] = a*alpha[-1:,:]
    return result

def stateoutput_estimates(ys, num_observables, num_states, sestimate):
    """
    TODO: not sure what the intention is
    
    Params:
      - ys: A list. `ys` captures a sequence of observations or "output symbols." `ys[t]` indicates
                  the observation sybol at time `t`.
      - num_observables: An int.
      - num_states: An int.
      - sestimate: A 2D numpy array. TODO not sure what is `sestimate`. The size of `sestimate` is
                   (length of the sequence)-by-(number of states), and `sestimate[t, s]` means
                   probability that the state is `s` at time `t`. Please see state_estimates() for
                   more detail.
    Returns: A 3D numpy array whose size is (number of observables)-by-(number of states)-by-
             (length of the sequence). Haven't figured out the interpretation yet.
    """
    seq_len = len(ys)
    result = np.zeros((num_observables, num_states, seq_len))
    for t in range(seq_len):
        result[ys[t], :, t] = sestimate[t, :]
    return result

def improve_params(xs, ys, m, tableaus=None):
    """
    TODO: not sure what the intention is
    """
    if tableaus is None:
        tableaus = make_tableaus(xs,ys,m)
    estimates = state_estimates(xs,ys,m,tableaus=tableaus)
    trans_estimates = transition_estimates(xs,ys,m,tableaus=tableaus)
    sout_estimates = stateoutput_estimates(ys, m.os, m.ns, estimates)

    # Calculate the numbers of each input in the input sequence.
    nlist = [0]*m.inps
    for x in xs:
        nlist[x] += 1

    sstates = [np.zeros((m.ns,1)) for i in range(m.inps)]
    for t in range(len(ys)):
        sstates[xs[t]] += estimates[t:t+1,:].T/nlist[xs[t]]

    # Estimator for transition probabilities
    alist = {a: np.zeros_like(matrix) for a, matrix in m.alist.items()}
    for t in range(len(ys)):
        alist[xs[t]] += trans_estimates[:,:,t]/nlist[xs[t]]
    for i in range(m.inps):
        alist[i] = alist[i]/(sstates[i].T)
        np.putmask(alist[i],(np.tile(sstates[i].T==0,(m.ns,1))),m.alist[i])

    c = np.zeros_like(m.c)
    for t in range(len(ys)):
        x = xs[t]
        c += sout_estimates[:,:,t] / (nlist[x]*m.inps*sstates[x].T)

    # Set the output probabilities to the original model if
    # we have no state observation at all.
    sstatem = np.hstack(sstates).T
    mask = np.any(sstatem == 0,axis=0)
    np.putmask(c,(np.tile(mask,(m.os,1))),m.c)

    return alist, c

def get_likelihood(tableaus):
    alpha, beta, N = tableaus
    return np.product(1/N)

def get_log_likelihood(tableaus):
    alpha, beta, N = tableaus
    return -np.sum(np.log(N))
