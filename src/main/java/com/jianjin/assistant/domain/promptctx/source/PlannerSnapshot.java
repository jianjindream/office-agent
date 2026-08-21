package com.jianjin.assistant.domain.promptctx.source;

public class PlannerSnapshot {
    private String taskId;
    private String query;
    private String status;
    private String phase;
    private int totalSteps;
    private int currentStep;
    private int interruptedAt;
    private String nextStepName;
    private String nextStepTool;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
    public int getInterruptedAt() { return interruptedAt; }
    public void setInterruptedAt(int interruptedAt) { this.interruptedAt = interruptedAt; }
    public String getNextStepName() { return nextStepName; }
    public void setNextStepName(String nextStepName) { this.nextStepName = nextStepName; }
    public String getNextStepTool() { return nextStepTool; }
    public void setNextStepTool(String nextStepTool) { this.nextStepTool = nextStepTool; }
}
