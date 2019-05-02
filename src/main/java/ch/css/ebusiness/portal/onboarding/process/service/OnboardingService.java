package ch.css.ebusiness.portal.onboarding.process.service;

import ch.css.ebusiness.portal.onboarding.api.model.*;
import ch.css.ebusiness.portal.onboarding.process.ViewName;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OnboardingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingService.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public OnboardingService(final RuntimeService runtimeService, final TaskService taskService, final HistoryService historyService) {

        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    public String start() {

        final ProcessInstance newProcessInstance = runtimeService.startProcessInstanceByKey("onboarding");
        LOGGER.info("Started new onboarding instance '{}'", newProcessInstance.getId());
        return newProcessInstance.getId();
    }

    public Optional<OnboardingInfo> info(final String onboardingId) {

        final Optional<ProcessInstance> runningProcessInstance = getRunningInstance(onboardingId);
        if (runningProcessInstance.isPresent()) {
            final OnboardingInfo info = new OnboardingInfo();
            info.setEnded(false);
            info.setId(onboardingId);
            info.setVariables(runtimeService.getVariables(onboardingId));
            final Optional<ViewName> currentStep = determineCurrentViewName(onboardingId);
            currentStep.ifPresent(viewName -> info.setCurrentStep(viewName.getViewId()));
            return Optional.of(info);
        }
        return Optional.empty();

    }

    private Optional<OnboardingInfo> info(final String onboardingId, boolean withHistory) {
        final Optional<OnboardingInfo> activeInfo = info(onboardingId);
        if (activeInfo.isPresent()){
            return activeInfo;
        } else if (!withHistory){
            return Optional.empty();
        }
        return history(onboardingId);
    }

    private Optional<OnboardingInfo> history(final String onboardingId) {

        final Optional<HistoricProcessInstance> historyInstance = getHistoryInstance(onboardingId);
        if (historyInstance.isPresent()) {
            final OnboardingInfo info = new OnboardingInfo();
            info.setEnded(true);
            info.setId(onboardingId);
            final Optional<ViewName> currentStep = determineCurrentViewName(onboardingId);
            currentStep.ifPresent(viewName -> info.setCurrentStep(viewName.getViewId()));
            return Optional.of(info);
        }
        return Optional.empty();

    }

    private Optional<ProcessInstance> getRunningInstance(final String onboardingId) {

        final ProcessInstance runningInstance = runtimeService.createProcessInstanceQuery().processInstanceId(onboardingId).singleResult();
        return Optional.ofNullable(runningInstance);
    }

    private Optional<HistoricProcessInstance> getHistoryInstance(final String onboardingId) {

        final HistoricProcessInstance runningInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(onboardingId).singleResult();
        return Optional.ofNullable(runningInstance);
    }

    private Optional<ViewName> determineCurrentViewName(final String onboardingId) {

        final Optional<Task> activeUserTask = getActiveUserTask(onboardingId);
        return activeUserTask.flatMap(task -> ViewName.fromId(task.getTaskDefinitionKey()));
    }

    private Optional<Task> getActiveUserTask(final String onboardingId) {

        return taskService.createTaskQuery().active().processInstanceId(onboardingId).list()
                .stream().findFirst(); // es kann nur einen geben (oder keinen)
    }

    public Optional<OnboardingInfo> userValidationFinished(final String onboardingId, final UserValidation userValidation) {

        final ProcessInstance runningInstance = getRunningInstance(onboardingId).orElseThrow(() -> new OnboardingNotFoundException(onboardingId));
        ensureProcessIsWaitingAtUserTask(ViewName.USER_VALIDATION, runningInstance);
        final Map<String, Object> variables = new HashMap<>();
        variables.put("partnerNo", userValidation.getPartnerNo());
        variables.put("birthdate", userValidation.getBirthdate());
        final String taskId = getActiveUserTask(onboardingId).orElseThrow(IllegalStateException::new).getId();
        taskService.complete(taskId, variables);
        return info(onboardingId);
    }

    public Optional<OnboardingInfo> registrationOptionChosen(final String onboardingId, final RegistrationOptionWrapper option) {

        final ProcessInstance runningInstance = getRunningInstance(onboardingId).orElseThrow(() -> new OnboardingNotFoundException(onboardingId));
        ensureProcessIsWaitingAtUserTask(ViewName.REGISTRATION_OPTION, runningInstance);
        final Map<String, Object> variables = new HashMap<>();
        variables.put("registrationType", option.getOption().getType());
        final String taskId = getActiveUserTask(onboardingId).orElseThrow(IllegalStateException::new).getId();
        taskService.complete(taskId, variables);
        return info(onboardingId);
    }

    public Optional<OnboardingInfo> documentCodeEntered(final String onboardingId, final DocumentCode documentCode) {

        final ProcessInstance runningInstance = getRunningInstance(onboardingId).orElseThrow(() -> new OnboardingNotFoundException(onboardingId));
        // TODO: validate documentCode
        ensureProcessIsWaitingAtUserTask(ViewName.DOCUMENT_CODE, runningInstance);
        final Map<String, Object> variables = new HashMap<>();
        variables.put("ahvNumber", documentCode.getAhvNumber());
        variables.put("documentNumber", documentCode.getDocumentNumber());

        final String taskId = getActiveUserTask(onboardingId).orElseThrow(IllegalStateException::new).getId();
        taskService.complete(taskId, variables);
        return info(onboardingId, true);
    }

    public Optional<OnboardingInfo> activationLetterConfirmed(String onboardingId, ActivationLetterCode activationLetterCode) {

        final ProcessInstance runningInstance = getRunningInstance(onboardingId).orElseThrow(() -> new OnboardingNotFoundException(onboardingId));
        // TODO: validate activation letter
        ensureProcessIsWaitingAtUserTask(ViewName.ACTIVATION_LETTER, runningInstance);
        final Map<String, Object> variables = new HashMap<>();
        variables.put("activationCode", activationLetterCode.getActivationCode());
        final String taskId = getActiveUserTask(onboardingId).orElseThrow(IllegalStateException::new).getId();
        taskService.complete(taskId, variables);
        return info(onboardingId);
    }

    private void ensureProcessIsWaitingAtUserTask(final ViewName expectedViewName, final ProcessInstance processInstance) {

        final Optional<ViewName> currentView = determineCurrentViewName(processInstance.getProcessInstanceId());
        if (currentView.isPresent()) {
            final ViewName current = currentView.get();
            if (current != expectedViewName) {
                // TODO: check if expected view is before current -> we do not want to skip tasks
                moveProcessToView(processInstance, expectedViewName, current);
            }
        } else {
            throw new IllegalStateException("Process ended?");
        }
    }

    private void moveProcessToView(final ProcessInstance processInstance, final ViewName expectedViewName, final ViewName currentView) {

        runtimeService.createProcessInstanceModification(processInstance.getProcessInstanceId())//
                .startBeforeActivity(expectedViewName.getViewId())//
                .cancelAllForActivity(currentView.getViewId())//
                .execute();
    }

}
