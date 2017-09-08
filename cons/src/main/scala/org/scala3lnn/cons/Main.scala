package org.scala3lnn.cons;

import java.io.{File, IOException, PrintWriter}

import com.moandjiezana.toml.{Toml, TomlWriter}
import com.typesafe.scalalogging.Logger
import org.scala3lnn.cons.cmd._
import org.scala3lnn.core.NeuralNetwork
import org.slf4j.LoggerFactory

import scala.io.Source

object Main extends App {

    private val parser = ImmutableParser.getParser()
    private val logger = Logger(LoggerFactory.getLogger(this.getClass))

    def getTomlConfig(config: File): Either[String, Toml] = {
        try {
            Right(new Toml().read(config))
        }
        catch {
            case ex: IOException => Left(s"Cannot parse configuration file: ${ex.getMessage}")
        }
    }

    def train(config: File) : Int = {
        logger.debug("train command")

        require(config != null, "Configuration file is mandatory")

        def trainNeuralNetwork(c: Toml): NeuralNetwork = {
            val inputNodes = c.getLong("nn.inputNodes").toInt
            val hiddenNodes = c.getLong("nn.hiddenNodes").toInt
            val outputNodes = c.getLong("nn.outputNodes").toInt
            val learningRate = c.getDouble("nn.learningRate")

            val dir = config.getParent
            val inFile = c.getString("files.in")

            val nn = new NeuralNetwork(inputNodes, hiddenNodes, outputNodes)

            val lines = Source.fromFile(new File(s"${dir}/${inFile}")).getLines()
            for (line <- lines) {
                val allValues = line.split(',')
                val inputs = allValues.drop(1).map(_.toInt).map(_.toDouble / 255 * 0.99 + 0.01)
                val target = Array.fill[Double](outputNodes)(0.01)
                target(allValues(0).toInt) = 0.99
                nn.train(inputs, target, learningRate)
            }
            nn
        }

        getTomlConfig(config) match {
            case Left(msg) => {
                logger.error(msg)
                return 1
            }
            case Right(c) => {
                logger.trace(c.toString)
                try {

                    val dir = config.getParent
                    val nn = trainNeuralNetwork(c)
                    val outFile = c.getString("files.out")
                    val overwrite = c.getBoolean("files.overwrite", false)

                    val trained = new Trained()
                    trained.nn.put("inputNodes", nn.inputNodes.asInstanceOf[AnyRef])
                    trained.nn.put("hiddenNodes", nn.hiddenNodes.asInstanceOf[AnyRef])
                    trained.nn.put("outputNodes", nn.outputNodes.asInstanceOf[AnyRef])

                    val tomlFile = new File(s"${dir}/${outFile}")
                    val outDir = tomlFile.getParent
                    val mxFile0 = tomlFile.getName + ".wih.csv"
                    val mxFile1 = tomlFile.getName + ".who.csv"

                    trained.files.put("wih", mxFile0)
                    trained.files.put("who", mxFile1)

                    val tomlWriter = new TomlWriter();
                    if(!tomlFile.exists() || overwrite) {
                        breeze.linalg.csvwrite(new File(s"$outDir/$mxFile0"), nn.weightsInputHidden, separator = ',')
                        breeze.linalg.csvwrite(new File(s"$outDir/$mxFile1"), nn.weightsHiddenOutput, separator = ',')

                        new PrintWriter(s"${dir}/${outFile}") {
                            write(tomlWriter.write(trained))
                            close
                        }
                        logger.info(s"Neural network was trained and stored in ${tomlFile.getAbsoluteFile}")
                    }
                    else {
                        logger.info(s"Couldn't save results, overwriting is not allowed")
                    }

                }
                catch {
                    case ex: Exception => {
                        logger.error(s"Cannot train neural network: ${ex.getClass} ${ex.getMessage}")
                        return 1
                    }
                }

            }
        }
        return 0
    }

    def query(cfgFile: File, query: String) : Int = {
        logger.debug("query command")

        require(cfgFile != null, "Configuration file is mandatory")
        require(query != null && query.length > 3, "Query string cannot be les 3 symbols")

        getTomlConfig(cfgFile) match {
            case Left(msg) => {
                logger.error(msg)
                return 1
            }
            case Right(c) => {
                logger.trace(c.toString)

                try{
                    val wihFile = c.getString("files.wih")
                    val whoFile = c.getString("files.who")
                    val inputNodes = c.getLong("nn.inputNodes").toInt
                    val hiddenNodes = c.getLong("nn.hiddenNodes").toInt
                    val outputNodes = c.getLong("nn.outputNodes").toInt

                    val outDir = cfgFile.getParent
                    val wihMatrix = breeze.linalg.csvread(new File(s"$outDir/$wihFile"), ',')
                    val whoMatrix = breeze.linalg.csvread(new File(s"$outDir/$whoFile"), ',')

                    val nn = new NeuralNetwork(inputNodes, hiddenNodes, outputNodes, Option(wihMatrix), Option(whoMatrix))

                    val inputs = query.split(',').map(_.toInt).map(_.toDouble / 255 * 0.99 + 0.01)

                    val result = nn.query(inputs)

                    logger.info(s"Result:\n${result.toArray.mkString("\n")}")

                } catch {
                    case ex: Exception => {
                        logger.error(s"Cannot create a query to neural network: ${ex.getClass} ${ex.getMessage}")
                        1
                    }
                }
            }
        }
        0
    }

    val retVal = parser.parse(args, Config()) match {
        case Some(config) =>
          config.mode match {
              case "train" => train(config.config)
              case "query" => query(config.config, config.query)
              case _ => println(s"${config.mode} is not implemented")
          }
        case None =>
        {
            println("Incorrect arguments")
        }
    }

    retVal match {
        case i: Int => System.exit(i)
        case o =>
            {
                logger.warn(s"Unknown result: ${o.toString}")
                System.exit(1)
            }
    }
}
