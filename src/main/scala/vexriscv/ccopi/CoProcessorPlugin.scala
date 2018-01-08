/*
 * The MIT License (MIT)
 * Copyright (c) 2017, Jens Nazarenus, Dominik Swierzy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package vexriscv.ccopi

import spinal.core._
import spinal.lib._
import vexriscv.plugin._
import vexriscv._
import Utilities._

/**
  * The co processor plugin for VexRiscv. Registers the computation units and
  * creates a `Stageable` for each function.
  *
  * @param compUnits
  */
class CoProcessorPlugin(compUnits : Seq[ComputationUnit]) extends Plugin[VexRiscv] {

  // Collect functions to create
  val functions = compUnits.map(u => u.functions.toList).flatten

  // Create singelton stageables
  val stageables = functions.map { f =>
    object stageable extends Stageable(Bool)
    f -> stageable
  }


  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    // Register at decoder service
    val decoder = pipeline.service(classOf[DecoderService])
    for((f, s) <- stageables) {
      decoder.addDefault(s, False)
      decoder.add(
        key = f.pattern.asMaskedLiteral,
        List(
          s -> True,
          REGFILE_WRITE_VALID -> True,
          BYPASSABLE_EXECUTE_STAGE -> False,
          BYPASSABLE_MEMORY_STAGE -> False,
          RS1_USE -> True,
          RS2_USE -> True
        )
      )
    }
  }

  def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    val cocpu = new CoProcessor(compUnits)

    execute plug new Area {
      import execute._

    }

    memory plug new Area {
      import memory._

    }
  }
}