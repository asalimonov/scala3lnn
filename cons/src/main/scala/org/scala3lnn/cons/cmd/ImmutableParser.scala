package org.scala3lnn.cons.cmd

import java.io.File

import scopt.OptionParser

object ImmutableParser {

  def getParser(): OptionParser[Config] ={
    new scopt.OptionParser[Config]("Scala 3-layer neural network") {

      cmd("train").action((_, c) => c.copy(mode = "train"))
        .text("train is a command.")
          .children(
            opt[File]('f', "file").required().valueName("<file.toml>")
              .action((x,c) => c.copy(config = x))
              .text("config is required configuration toml file"),
            opt[Unit]("debug").hidden().action((_,c) => c.copy(debug = true)).text("Enable debug mode.")
          )

      cmd("query").action((_, c) => c.copy(mode = "query"))
        .text("query is a command.")
        .children(
          opt[File]('f', "file").required().valueName("<file.toml>")
            .action((x,c) => c.copy(config = x))
            .text("config is required configuration toml file"),
          opt[String]("query").valueName("k1=v1,k2=v2...").action( (x, c) =>
            c.copy(query = x) ).text("string query with sequence of bytes (0 - 255) which delimited by comma (,)"),
          opt[Unit]("debug").hidden().action((_,c) => c.copy(debug = true)).text("Enable debug mode.")
        )
    }
  }

}
