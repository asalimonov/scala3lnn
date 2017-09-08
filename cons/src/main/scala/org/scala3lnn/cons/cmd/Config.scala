package org.scala3lnn.cons.cmd

import java.io.File

case class Config (mode: String = "", config: File = new File("."), query: String = "", debug: Boolean = false){

}
