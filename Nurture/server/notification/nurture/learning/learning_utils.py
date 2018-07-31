def argmax_dict(d):
    idx = None
    val = -1e100
    for k in d:
        if d[k] > val:
            idx, val = k, d[k]
    return idx


def max_dict_val(d):
    return max([d[k] for k in d])
