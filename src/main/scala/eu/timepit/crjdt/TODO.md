# TODO

Fig. 8:
- NEXT1,2,3,4
- KEYS1,2,3
- VAL1,2,3

Fig. 9:
- APPLY-LOCAL
- APPLY-REMOTE
- SEND
- RECV
- YIELD

Fig. 10:
- DESCEND
- CHILD-GET
- CHILD-MAP
- CHILD-LIST
- CHILD-REG
- PRESENCE1
- PRESENCE2
- ADD-ID1
  - addId(ctx, k_tag, id, mut) adds id to the presence set pres(k) in ctx
  - untag(k_tag) == k
  - mut != DeleteM
- ADD-ID2
  - addId(ctx, k_tag, id, DeleteM) == ctx
  - a delete mutation does not alter the presence set of ctx
- ASSIGN
- EMPTY-MAP
- EMPTY-LIST
- INSERT1
- INSERT2

Fig. 11:
- DELETE
...
