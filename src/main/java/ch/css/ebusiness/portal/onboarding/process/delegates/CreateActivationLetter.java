package ch.css.ebusiness.portal.onboarding.process.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateActivationLetter implements JavaDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateActivationLetter.class);

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        LOGGER.info("Called Delegate: " + this.getClass().getName());
    }
}
