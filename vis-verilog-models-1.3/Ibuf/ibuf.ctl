# The 16 reachable states are:
#
# qAge| valid
# 012 | 012
#-----+------------
# 111 | 11-
# 110 | 1-1 100
# 100 | 1-1
# 011 | 11-
# 001 | 111 01-
# 000 | 00- -11
#
# They are characterized by the following invariant:

AG((qAge[0]=0 + qAge[1]=1 + qAge[2]=0) *
   (qAge[0]=1 + qAge[1]=0 + qAge[2]=1) *
   (qAge[0]=1 -> valid[0]=1) *
   (qAge[1]=1 -> valid[0]=1) *
   (qAge[2]=1 -> valid[1]=1) *
   (valid[0]=1 * valid[1]=0 -> qAge[0]=1) *
   (valid[0]=1 * valid[2]=0 -> qAge[1]=1) *
   (valid[1]=1 * valid[2]=0 -> qAge[2]=1));

# None of the bits can be functionally expressed in terms of the others,
# though the encoding is obviously redundant.
