import Val.Str
import syntax._

class Figure6 {
  (doc := `{}`) `;`
    (let(v("list")) = doc.downField("shopping").iter) `;`
    v("list").insert(Str("eggs")) `;`
    (let(v("eggs")) = v("list").next) `;`
    v("eggs").insert(Str("milk")) `;`
    v("list").insert(Str("cheese"))
}
