package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._

object Figure2 {
  doc.downField("colors").downField("blue") := "#0000ff"
  doc.downField("colors").downField("red") := "#ff0000"
  doc.downField("colors") := `{}`
  doc.downField("colors").downField("green") := "#00ff00"
}
