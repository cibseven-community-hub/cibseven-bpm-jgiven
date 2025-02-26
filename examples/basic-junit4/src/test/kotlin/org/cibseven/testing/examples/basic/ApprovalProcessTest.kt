package org.cibseven.testing.examples.basic

import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.junit.ScenarioTest
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Elements
import org.cibseven.testing.examples.basic.ApprovalProcessBean.Expressions
import io.toolisticon.testing.jgiven.AND
import io.toolisticon.testing.jgiven.GIVEN
import io.toolisticon.testing.jgiven.THEN
import io.toolisticon.testing.jgiven.WHEN
import org.cibseven.bpm.engine.test.Deployment
import org.cibseven.bpm.engine.test.ProcessEngineRule
import org.cibseven.bpm.engine.variable.Variables.putValue
import org.cibseven.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.time.Period
import java.util.*


@Deployment(resources = [ApprovalProcessBean.RESOURCE])
open class ApprovalProcessTest : ScenarioTest<ApprovalProcessActionStage, ApprovalProcessActionStage, ApprovalProcessThenStage>() {

  companion object {
    @get: ClassRule
    @JvmStatic
    val processEngineRule: ProcessEngineRule = TestCoverageProcessEngineRuleBuilder.create().build()
  }

  @get:Rule
  val localRule: ProcessEngineRule = processEngineRule

  @ScenarioState
  val camunda = localRule.processEngine

  @Test
  fun `should deploy`() {
    THEN
      .process_is_deployed(ApprovalProcessBean.KEY)
  }

  @Test
  fun `should start asynchronously`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
    WHEN
      .process_is_started_for_request(approvalRequestId)
    THEN
      .process_waits_in(Elements.START)
  }

  @Test
  fun `should wait for automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)

    WHEN
      .process_continues()

    THEN
      .process_waits_in(Elements.SERVICE_AUTO_APPROVE)
      .AND
      .external_task_exists("approve-request")

  }

  @Test
  fun `should automatic approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()

    WHEN
      .automatic_approval_returns(Expressions.ApprovalDecision.APPROVE)

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_APPROVED)

  }


  @Test
  fun `should automatically reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.AUTOMATIC)
      .AND
      .process_continues()

    WHEN
      .automatic_approval_returns(Expressions.ApprovalDecision.REJECT)

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.SERVICE_AUTO_APPROVE, Elements.END_REJECTED)

  }

  @Test
  fun `should manually reject`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .AND
      .process_continues()
      .AND
      .process_waits_in(Elements.USER_APPROVE_REQUEST)
      .AND
      .task_priority_is_between(10, 30)
      .AND
      .task_has_follow_up_date_after(Period.ofDays(1))

    WHEN
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.REJECT),
        isAsyncAfter = true
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.END_REJECTED)

  }

  @Test
  fun `should manually approve`() {

    val approvalRequestId = UUID.randomUUID().toString()

    GIVEN
      .process_is_deployed(ApprovalProcessBean.KEY)
      .AND
      .process_is_started_for_request(approvalRequestId)
      .AND
      .approval_strategy_can_be_applied(Expressions.ApprovalStrategy.MANUAL)
      .AND
      .process_continues()
      .AND
      .process_waits_in(Elements.USER_APPROVE_REQUEST)

    WHEN
      .task_is_completed_with_variables(
        putValue(ApprovalProcessBean.Variables.APPROVAL_DECISION, Expressions.ApprovalDecision.APPROVE),
        isAsyncAfter = true
      )

    THEN
      .process_is_finished()
      .AND
      .process_has_passed(Elements.END_APPROVED)

  }
}
