package com.imooc.seckill.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;

    public ValidationResult validate(Object bean) {
        final ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (!constraintViolationSet.isEmpty()) {
            validationResult.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation -> {
                String errorMsg = constraintViolation.getMessage();
                String wrongPropertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(wrongPropertyName, errorMsg);
            });
        }
        return validationResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
