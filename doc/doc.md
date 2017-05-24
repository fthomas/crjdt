# Vertical Move
This document describes how the vertical move operation works.
It changes the order of elements in an ordered list.

## Desired merge results
Vertical move operations can be modeled in several ways:

#### Decrease index

A vertical move operation can be modeled by defining, that the index of a specific element should be decreased.

Example:
* Alice: VerticalMove(2, up)
* Bob: VerticalMove(3, up)

When Alices operation is applied to the list in the first column of the graphic, the result is the list in the second column. The index of element 2 was decreased by one and therefore the index of element 1 increased. When Bobs operation is applied then, the result is the list in the third column:

![](img/decrease_index.png)

When however Bobs operation is applied to the list `1, 2, 3` and then Alices operation, the result will be `1, 2, 3`, so the order will be the same as in the original list.

Conclusion: When applying operations, the order matters.

#### Provide a target
A vertical move operation can be modeled by providing a target element, above which the moved element should end up.

Example:
* Alice: VerticalMove(2, above, 1)
* Bob: VerticalMove(3, above, 2)

![](img/provide_target.png)



| | Alice, then Bob: `3, 2, 1` | Bob then Alice: `2, 1, 3` |
| :------------- |
| 2 above 1 | ✓ | ✓ |
| 3 above 2 | ✓ | x |

#### Sorting operations
Test beispiele

#### Comparison

| | Decrease index: `2, 3, 1` | Provide a target: `3, 2, 1` |
| :------------- |
| 2 went up | ✓ | x |
| 3 went up | ✓ | ✓ |
| 2 above 1 | o | ✓ |
| 3 above 2 | x | ✓ |

Use cases
Reordering agenda items, tasks
I think intention is lost, when 3 is not above 2.


In the implementation I chose the



## Syntax
The method `moveVertical(targetExpr: Expr, beforeAfter: BeforeAfter)` is part of the possible operations for an expression. Expressions are used to construct a cursor to a specific element of the JSON. The method moves that element to a new position in the same list. The new position is described by providing a `targetExpr` which must be a different element in the same list.
Furthermore one can deside wether the element should be moved right before or after the target element by filling the parameter `beforeAfter` with either `Before` or `After`.

Example code:

`two.moveVertical(one, Before))`

Where `one` and `two` are

````
val children = doc.downField("children")
val two = children.iter.next.next
val one = children.iter.next
````

Applying this operation to a list `1, 2, 3` will result in `2, 1, 3`.

More usage examples and tests are in the file [`MoveVertical.scala`](https://github.com/Tamriel/crjdt/blob/master/modules/circe/src/test/scala/eu/timepit/crjdt/circe/MoveVertical.scala).

## Algorithm
Applying a vertical move operation is as simple as adjusting the next ref list of the parent of the moved element.
When two move operations are done concurrently however, the final list may differ depending on the order the operations were applied.

Example: Alice and Bob both start with the same list `1, 2, 3`. Both do a vertical move operation. Alice receives Bobs operation:

![](img/vertical_move_1.png)

She resets the order of elements to the order when Alice and Bob had the same state:

![](img/vertical_move_2.png)

Then she applies the two operations again, but in a specific order. The result is `3, 2, 1`:

![](img/vertical_move_3.png)

What was just shown in the example can be abstracted into:

One is done locally, and when the other one, done by a remote replica comes in,
That order is restored and we redo the two concurrent operations.

When an operation comes in which was done concurrently to an own op:
1. Reset the current order to the order before
2. Redo all operations since then in a specific order. The order does not matter in the first place, as long as all sides order in the same way and therefore all sides get the same result.

To be able to reset the order to an older order, we save the order before applying any operation. It is saved in the Map `orderArchive` inside the parent node with the counter of the operation id as the key.

When resetting the order, we get the order which has the counter of the incoming operation as its id. That is the order which was saved, before the local operation which is concurrent to the incoming operation was applied.

Before applying an operation we save the order in orderArchive.
It is a Map whose key is the lamport timestamp counter value.

To improve performance and save disk space, we don't save the
order before assign operations, since they don't change the order.
Now there might be this situation: Alice did an assign and then a
move op, while Bob did a move op. Now Bobs op comes in and
Alice resets her order to the order with counter value like the
incoming op. However, locally exists no such saved order, since
she has done an assign op at that count. Therefore she resets
to the next higher saved order.

This fix is implemented by getting all orders whose counter is
greater equals than the counter of the incoming op and then
choosing the earliest order of those:

## Performance improvements
