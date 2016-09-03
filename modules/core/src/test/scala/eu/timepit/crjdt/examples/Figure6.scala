package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure6 extends Properties("Figure6") {
  property("Doc") = secure {
    val list = v("list")
    val eggs = v("eggs")
    val cmd = (doc := `{}`) `;`
        (let(list) = doc.downField("shopping").iter) `;`
        list.insert("eggs") `;`
        (let(eggs) = list.next) `;`
        eggs.insert("milk") `;`
        list.insert("cheese")
    println(ReplicaState.empty("").applyCmd(cmd).context)
    true
  }
}
