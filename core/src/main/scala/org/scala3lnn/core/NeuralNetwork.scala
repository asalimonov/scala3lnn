package org.scala3lnn.core

import breeze.linalg._
import breeze.numerics.sigmoid

class NeuralNetwork(val inputNodes: Int, val hiddenNodes: Int, val outputNodes: Int,
                    wih: Option[DenseMatrix[Double]] = Option.empty, who: Option[DenseMatrix[Double]] = Option.empty) {

  val normalHidden = breeze.stats.distributions.Gaussian(0, math.pow(hiddenNodes, -0.5))
  val normalOutput = breeze.stats.distributions.Gaussian(0, math.pow(outputNodes, -0.5))

  val samplesHidden = normalHidden.sample(hiddenNodes * inputNodes)
  val samplesOutput = normalOutput.sample(outputNodes * hiddenNodes)

  var weightsInputHidden = wih.getOrElse(new DenseMatrix[Double](hiddenNodes, inputNodes, samplesHidden.toArray))
  var weightsHiddenOutput = who.getOrElse(new DenseMatrix[Double](outputNodes, hiddenNodes, samplesOutput.toArray))

  val activationFunc = (x: Double) => sigmoid(x)

  def query(inputList: Array[Double]): DenseMatrix[Double] = {
    val inputs = new DenseMatrix[Double](inputList.length, 1, inputList)

    val hiddenInputs = weightsInputHidden * inputs
    val hiddenOutputs : DenseMatrix[Double] = hiddenInputs.map(activationFunc)
    val finalInputs  = weightsHiddenOutput * hiddenOutputs
    finalInputs.map(activationFunc)
  }

  def train(inputList: Array[Double], targetList: Array[Double], learningRate: Double): Unit = {
    val inputs = DenseMatrix.create[Double](inputList.length, 1, inputList)
    val targets = DenseMatrix.create[Double](targetList.length, 1, targetList)

    val hiddenInputs = weightsInputHidden * inputs
    val hiddenOutputs = hiddenInputs.map(activationFunc)
    val finalInput = weightsHiddenOutput * hiddenOutputs
    val finalOutputs = finalInput.map(activationFunc)

    val outputErrors = targets - finalOutputs
    val hiddenErrors = weightsHiddenOutput.t * outputErrors

    val tmp1 = outputErrors *:* finalOutputs *:* finalOutputs.map(v => 1 - v)
    weightsHiddenOutput += learningRate * (tmp1 * hiddenOutputs.t)
    weightsInputHidden += learningRate * ((hiddenErrors *:* hiddenOutputs *:* hiddenOutputs.map(v => 1 - v)) * inputs.t)
  }
}
