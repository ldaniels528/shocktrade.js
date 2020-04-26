package com.shocktrade.webapp.vm

import com.shocktrade.webapp.vm.VirtualMachine.VmProcess
import com.shocktrade.webapp.vm.dao.VirtualMachineDAO
import com.shocktrade.webapp.vm.proccesses.cqm.ContestQualificationModule
import com.shocktrade.webapp.vm.proccesses.cqm.dao.QualificationDAO
import org.scalatest.funspec.AsyncFunSpec

import scala.concurrent.ExecutionContextExecutor
import scala.scalajs.concurrent.JSExecutionContext

/**
 * Virtual Machine Test
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachineTest() extends AsyncFunSpec {
  implicit override val executionContext: ExecutionContextExecutor = JSExecutionContext.queue

  describe(classOf[VirtualMachine].getSimpleName) {

    it("should orchestrate the contest life-cycle") {
      implicit val vmDAO: VirtualMachineDAO = VirtualMachineDAO()
      implicit val cqmDAO: QualificationDAO = QualificationDAO()

      val cqm = new ContestQualificationModule()
      val vm = new VirtualMachine()
      val outcome = for {
        opCodes <- cqm.run()
        _ = vm.enqueue(opCodes: _*)
        results <- vm.invokeAll()
      } yield results

      outcome.map { results =>
        results foreach { case VmProcess(code, result, runTime) =>
          info(s"[${runTime.toMillis} ms] $code => ${result.orNull}")
        }
        assert(results == results)
      }
    }

  }

}
