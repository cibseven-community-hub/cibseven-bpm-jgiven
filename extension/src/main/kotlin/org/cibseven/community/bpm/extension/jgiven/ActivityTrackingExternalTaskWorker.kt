package org.cibseven.community.bpm.extension.jgiven

import org.cibseven.bpm.engine.ExternalTaskService
import org.cibseven.bpm.engine.externaltask.LockedExternalTask
import org.cibseven.bpm.engine.variable.VariableMap
import org.cibseven.bpm.engine.variable.Variables

/**
 * Custom worker that is called directly. It will track the activities it was called for.
 */
class ActivityTrackingExternalTaskWorker(
  val externalTaskService: ExternalTaskService,
  val workerName: String = "dummy",
  val topicName: String,
  val activities: MutableList<String> = mutableListOf(),
  val variablesToSet: VariableMap = Variables.createVariables()
) : (LockedExternalTask) -> Unit {

  override fun invoke(task: LockedExternalTask) {
    if (task.topicName == topicName) {
      externalTaskService.complete(
        task.id,
        workerName,
        variablesToSet
      )
      activities.add(task.activityId)
    }
  }
}
