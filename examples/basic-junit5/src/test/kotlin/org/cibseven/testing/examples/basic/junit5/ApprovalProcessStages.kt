package org.cibseven.testing.examples.basic.junit5

import com.tngtech.jgiven.annotation.As
import com.tngtech.jgiven.annotation.BeforeStage
import com.tngtech.jgiven.annotation.Quoted
import org.cibseven.community.bpm.extension.jgiven.JGivenProcessStage
import org.cibseven.community.bpm.extension.jgiven.ProcessStage
import org.cibseven.testing.examples.basic.ApprovalProcessBean
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Expressions.APPROVE_REQUEST_TASK_LISTENER
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Expressions.DETERMINE_APPROVAL_STRATEGY
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Expressions.LOAD_APPROVAL_REQUEST
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Variables.APPROVAL_DECISION
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Variables.APPROVAL_STRATEGY
import org.cibseven.testing.examples.basic.BasicProcessApplication
import io.toolisticon.testing.jgiven.step
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat
import org.cibseven.bpm.engine.test.assertions.bpmn.BpmnAwareTests.externalTask
import org.cibseven.bpm.engine.test.mock.Mocks
import org.cibseven.bpm.engine.variable.Variables
import org.cibseven.bpm.engine.variable.Variables.putValue
import org.cibseven.community.mockito.DelegateExpressions.getJavaDelegateMock
import org.cibseven.community.mockito.DelegateExpressions.registerJavaDelegateMock

/**
 * Process stage.
 */
@JGivenProcessStage
class ApprovalProcessActionStage : ProcessStage<ApprovalProcessActionStage, ApprovalProcessBean>() {

  /**
   * mocks all expressions (delegates) of the process.
   */
  @BeforeStage
  fun mock_all_delegates() {
    registerJavaDelegateMock(DETERMINE_APPROVAL_STRATEGY)
    registerJavaDelegateMock(LOAD_APPROVAL_REQUEST)
    // register a real listener
    Mocks.register(APPROVE_REQUEST_TASK_LISTENER, BasicProcessApplication().approveRequestTaskListener())
  }

  /**
   * Starts process.
   */
  fun process_is_started_for_request(@Quoted approvalRequestId: String) = step {
    processInstanceSupplier = ApprovalProcessBean(camunda)
    processInstanceSupplier.start(approvalRequestId)
    assertThat(processInstanceSupplier.processInstance).isNotNull
    assertThat(processInstanceSupplier.processInstance).isStarted
  }

  /**
   * Determines of the approval strategy can be applied.
   */
  @As("\$approvalStrategy approval strategy can be applied")
  fun approval_strategy_can_be_applied(@Quoted approvalStrategy: String) = step {
    getJavaDelegateMock(DETERMINE_APPROVAL_STRATEGY).onExecutionSetVariables(putValue(APPROVAL_STRATEGY, approvalStrategy))
  }

  /**
   * Preloads the automatic approval with result.
   */
  fun automatic_approval_returns(approvalResult: String) = step {
    external_task_is_completed(
      externalTask().topicName,
      putValue(APPROVAL_DECISION, approvalResult),
    )
  }

}

/**
 * Assert stage.
 */
@JGivenProcessStage
class ApprovalProcessThenStage : ProcessStage<ApprovalProcessThenStage, ApprovalProcessBean>()
