package org.analogweb.oval;

import static org.analogweb.oval.OvalPluginModuleConfig.PLUGIN_MESSAGE_RESOURCE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertValid;

import org.analogweb.Invocation;
import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.core.AbstractInvocationInterceptor;
import org.analogweb.core.AbstractInvocationProcessor;
import org.analogweb.util.AnnotationUtils;
import org.analogweb.util.ClassUtils;
import org.analogweb.util.ReflectionUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;
import org.analogweb.util.logging.Markers;

/**
 * {@link AssertValid}が付与された対象のオブジェクトに対する
 * 検証({{@link Validator#validate(Object)})を行う{@link AbstractInvocationProcessor}
 * の実装です。検証に適合しない、且つエントリポイントメソッドの引数に
 * {@link ConstraintViolations}が存在する場合は、引数に検証結果
 * ({@link ConstraintViolations})を設定します。(設定される引数は
 * １つのみです。)存在しない場合は{@link ConstraintViolationException}
 * が投げられます。
 * @author snowgoose
 */
public class OvalInvocationProcessor extends AbstractInvocationInterceptor {

    private static final Log log = Logs.getLog(OvalInvocationProcessor.class);

    @Override
    public Object onInvoke(Invocation invocation, InvocationMetadata metadata) {
		Method method = ReflectionUtils.getMethodQuietly(metadata
				.getInvocationClass(), metadata.getMethodName(), metadata
				.getArgumentTypes());
		InvocationArguments args = invocation.getInvocationArguments();
        List<Object> targets = findValidationTargets(method, metadata, args);
        if (targets.isEmpty()) {
            return invocation.invoke();
        }
        final List<ConstraintViolation> violations = new LinkedList<ConstraintViolation>();
        Validator validator = getValidator();
        for (Object validationTarget : targets) {
            log.log(PLUGIN_MESSAGE_RESOURCE, Markers.VARIABLE_ACCESS, "DOVV000001",
                    validationTarget);
            List<ConstraintViolation> verificationResult = validator.validate(validationTarget);
            logValidationResult(validationTarget, verificationResult);
            violations.addAll(verificationResult);
        }
        if (violations.isEmpty() == false) {
            int indexOfViolations = findIndexOfViolations(metadata);
            if (indexOfViolations == -1) {
                throw new ConstraintViolationException(
                        new ConstraintViolations<ConstraintViolation>() {
                            @Override
                            public Collection<ConstraintViolation> all() {
                                return violations;
                            }
                        });
            } else {
				args.putInvocationArgument(
						indexOfViolations,
						new ConstraintViolations<ConstraintViolation>() {
							@Override
							public Collection<ConstraintViolation> all() {
								return violations;
							}
						});
            }
        }
        return invocation.invoke();
    }

    private void logValidationResult(Object validationTarget,
            List<ConstraintViolation> verificationResult) {
        if (log.isDebugEnabled(Markers.VARIABLE_ACCESS)) {
            if (verificationResult.isEmpty()) {
                log.log(PLUGIN_MESSAGE_RESOURCE, Markers.VARIABLE_ACCESS, "DOVV000002",
                        validationTarget);
            } else {
                log.log(PLUGIN_MESSAGE_RESOURCE, Markers.VARIABLE_ACCESS, "DOVV000003",
                        validationTarget, verificationResult.size());
            }
        }
    }

    protected List<Object> findValidationTargets(Method method, InvocationMetadata metadata,
            InvocationArguments args) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<Object> values = args.asList();
        List<Object> instances = new ArrayList<Object>();
        for (int index = 0, limit = parameterAnnotations.length; index < limit; index++) {
            if (validateNesessary(parameterAnnotations[index])) {
                instances.add(values.get(index));
            }
        }
        return instances;

    }

    @SuppressWarnings("unchecked")
    private boolean validateNesessary(Annotation[] parameterAnnotations) {
        AssertValid assertValid = AnnotationUtils.findAnnotation(AssertValid.class,
                parameterAnnotations);
        if (assertValid != null) {
            return true;
        }
        Class<Annotation> valid = (Class<Annotation>) ClassUtils
                .forNameQuietly("javax.validation.Valid");
        if (valid != null) {
            Annotation a = AnnotationUtils.findAnnotation(valid, parameterAnnotations);
            return (a != null);
        }
        return false;
    }

    protected int findIndexOfViolations(InvocationMetadata metadata) {
        String cName = ConstraintViolations.class.getCanonicalName();
        Class<?>[] argTypes = metadata.getArgumentTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (argTypes[i].getCanonicalName().equals(cName)) {
                return i;
            }
        }
        return -1;
    }

    protected Validator getValidator() {
        return new Validator();
    }

}
