package ImplicitExamples

import scala.language.implicitConversions

object StringConversion {
  implicit def stringWrapper(s: String) =
    new IndexedSeq[Char] {
      def length = s.length
      def apply(i: Int) = s.charAt(i)
    }
}

object Main extends App {
  import StringConversion.stringWrapper

  println(stringWrapper("abc123") exists(_.isDigit))
  println(stringWrapper("abc") exists(_.isDigit))
}