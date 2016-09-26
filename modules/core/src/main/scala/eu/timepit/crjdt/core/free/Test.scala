package eu.timepit.crjdt.core.free

import eu.timepit.crjdt.core.free.syntax._

object Test {
  /*
  val list = v("list")
    val eggs = v("eggs")
    val cmd = (doc := `{}`) `;`
        (let(list) = doc.downField("shopping").iter) `;`
        list.insert("eggs") `;`
        (let(eggs) = list.next) `;`
        eggs.insert("milk") `;`
        list.insert("cheese")
   */

  for {
    _ <- doc := `{}`
    list = doc.downField("shopping").iter
    _ <- list.insert("eggs")
    eggs = list.next
    _ <- eggs.insert("milk")
    _ <- list.insert("cheese")
  } yield ()
}
