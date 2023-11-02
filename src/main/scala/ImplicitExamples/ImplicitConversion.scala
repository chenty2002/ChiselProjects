package ImplicitExamples

import scala.language.implicitConversions


class MyInt(t: Int) {
  def getInt: Int = t + 1
}

class MyClass(myInt: MyInt) {
  def getMyInt: MyInt = myInt
}

object ImplicitConversion extends App {
  implicit def int2MyInt2(t: Int): MyInt = new MyInt(t+2)
  implicit def myInt2MyClass(myInt: MyInt): MyClass = new MyClass(myInt)

  println(1.getInt)
}

object ImplicitArgument extends App {
//  implicit val str: String = "Implicit Arg"

  def print(explicitArg: String)
           (implicit implicitArg: String): Unit = {
    println(s"Using $explicitArg and $implicitArg")
  }

//  print("Explicit Arg")
//  // -> Using Explicit Arg and Implicit Arg
//  print("Explicit Arg")()
//  // -> Using Explicit Arg and Default Implicit Arg
//  print("Explicit Arg")("Explicitly Supplied Arg")
//  // -> Using Explicit Arg and Explicitly Supplied Arg
//
//  def printImplicit()(implicit arg: String = "Default Implicit Arg"): Unit = {
//    println(s"Using $arg")
//  }
//
//  printImplicit()
//  // -> Using Implicit Arg
//  printImplicit()()
//  // -> Using Default Implicit Arg
//  printImplicit()("Explicitly Supplied Arg")
//  // -> Using Explicitly Supplied Arg
//
//  def printOnlyImplicit(implicit arg: String = "Default Implicit Arg"): Unit = {
//    println(s"Using $arg")
//  }
//
//  printOnlyImplicit
//  printOnlyImplicit()
//  printOnlyImplicit("Explicitly Supplied Arg")
//
//
//  def printImplicitly() = {
//    println(s"Using ${implicitly[String]}")
//  }
//
//  printImplicitly()
//  // -> Using Implicit Arg
//
//  def printImplicitly2() = {
//    println(s"Using ${implicitly[String]("Explicitly Supplied Arg")}")
//  }
//
//  printImplicitly2()
//  // -> Using Explicitly Supplied Arg
//
//  def printImplicitly3()(implicit arg: String) = {
//    println(s"Using ${implicitly[String](str)}")
//  }
//
//  printImplicitly3()
//  // -> Using Explicitly Supplied Arg
}