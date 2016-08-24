import Val.Str
import syntax._

class Figure6 {
  val list = v("list")
  val eggs = v("eggs")
  (doc := `{}`) `;`
    (let(list) = doc.downField("shopping").iter) `;`
    list.insert(Str("eggs")) `;`
    (let(eggs) = list.next) `;`
    eggs.insert(Str("milk")) `;`
    list.insert(Str("cheese"))
}
