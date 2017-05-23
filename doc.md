# Vertical Move
This document describes how the vertical move operation works.
It changes the order of elements in an ordered list.

## Syntax
The method `moveVertical(targetExpr: Expr, beforeAfter: BeforeAfter)` is part of the possible operations for an expression. Expressions are used to construct a cursor to a specific element of the JSON. The method moves that element to a new position in the same list. The new position is described by providing a `targetExpr` which must be a different element in the same list.
Furthermore one can deside wether the element should be moved right before or after the target element by filling the parameter `beforeAfter` with either `Before` or `After`.

Example code:

`two.moveVertical(one, Above))`

Where `two` and `one` are

````
val children = doc.downField("children")
val two = children.iter.next.next
val one = children.iter.next
````

Applying this operation to a list `1, 2, 3` will result in `2, 1, 3`.

More usage examples and tests are in the file [`MoveVertical.scala`](https://github.com/Tamriel/crjdt/blob/master/modules/circe/src/test/scala/eu/timepit/crjdt/circe/MoveVertical.scala).

## Algorithm
Applying a vertical move operation is as simple as adjusting the next ref list of the parent of the moved element.
But when two move operations are done concurrently, we have to ensure all sides end up in the same state.

Example: Alice and Bob start synchronized with the list `1, 2, 3`. Alice moves the `2` above the `1`, while Bob moves the `3` above the `2`. Alice receives Bob's operation:

![](img/vertical_move_1.png)

Alice resets her order of elements to the order, when Alice and Bob where in sync:

![](img/vertical_move_2.png)

Alice redoes the two operations:

![](img/vertical_move_3.png)

The algorithm is described below on a high level.

When a remote operation comes in, which was done concurrently to an own operation, do:
1. Reset the current element order to the order before the replicas diverged.
2. Redo all operations since then in a specific order. The order does not matter in the first place, as long as all sides order in the same way and therefore all sides get the same end result.

To be able to reset the order to an older order, the order is saved each time before an operation is applied. It is saved in the Map `orderArchive` inside the parent node with the counter of the operation id as the key.

How do we know to which order we shall reset?
We just use the counter of the incoming operation as the key for the Map. The order we get is the order which was saved, before the local operation, which was concurrent to the incoming operation, was applied.

## Performance improvements
To improve performance and save disk space, we don't save the
order before assign operations, since they don't change the order.
Furthermore we don't reset the order and redo operations when an  assign operation comes in.

Now there might be this situation: Alice did an assign and then a
move op, while Bob did a move op. Now Bobs op comes in and
Alice resets her order to the order with counter value like the
incoming op. However, locally exists no such saved order, since
she has done an assign op at that count. Therefore she resets
to the next higher saved order.

This fix is implemented by getting all orders whose counter is greater equals than the counter of the incoming op and then
choosing the earliest order of those.

## Desired merge results
There are several ways to model vertical movement operations.

**Decrease index**
Operation could be modeled like this:
* Alice: VerticalMove(2, up)
* Bob: VerticalMove(3, up)

![](img/decrease_index.png)

The first column shows the starting position.
The second column shows the order, after Alices operation was applied. The `2` moved one place up by decreasing its index, and therefore the `1` moved one down. In other words, they swapped places.
When then Bobs operation is applied, the result will be the third column.
To sum up, when the order of operations is *Alice, then Bob*, the result will be `2, 3, 1`.
When the order is however *Bob, then Alice*, the result will be `1, 2, 3`. With this order, no change did take place.

**Provide target**

![](img/provide_target.png)


**Ordering**

 use cases aus treenote
