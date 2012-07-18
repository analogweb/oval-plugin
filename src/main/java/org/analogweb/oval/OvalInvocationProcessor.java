package org.analogweb.oval;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertValid;

import org.analogweb.Invocation;
import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.core.AbstractInvocationProcessor;
import org.analogweb.util.AnnotationUtils;
import org.analogweb.util.ClassUtils;
import org.analogweb.util.logging.Log;
import org.analogweb.util.logging.Logs;

public class OvalInvocationProcessor extends AbstractInvocationProcessor {
    
    private static final Log log = Logs.getLog(OvalInvocationProcessor.class);

    @Override
    public Object onInvoke(Method method, Invocation invocation, InvocationMetadata metadata,
            InvocationArguments args) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] argTypes = metadata.getArgumentTypes();
        final List<ConstraintViolation> violations = new LinkedList<ConstraintViolation>();
        int indexOfViolations = indexOfViolations(argTypes);
        List<Object> argList = args.asList();
        for (int index = 0, limit = argTypes.length; index < limit; index++) {
            if(nesessaryValidation(parameterAnnotations[index])){
                log.info("valiate!");
                Validator validator = getValidator();
                violations.addAll(validator.validate(argList.get(index)));
            }
        }
        // TODO none of indexOfViolations.
        args.putInvocationArgument(indexOfViolations, new ConstraintViolations<ConstraintViolation>() {
            @Override
            public Collection<ConstraintViolation> all() {
                return violations;
            }
        });
        return super.onInvoke(method, invocation, metadata, args);
    }

    private int indexOfViolations(Class<?>[] argTypes) {
        String cName = ConstraintViolations.class.getCanonicalName();
        for(int i = 0;i < argTypes.length;i++){
            if(argTypes[i].getCanonicalName().equals(cName)){
                return i;
            }
        }
        return -1;
    }

    private Validator getValidator() {
        return new Validator();
    }

    @SuppressWarnings("unchecked")
    private boolean nesessaryValidation(Annotation[] parameterAnnotations) {
        AssertValid assertValid = AnnotationUtils.findAnnotation(AssertValid.class, parameterAnnotations);
        if(assertValid != null){
            return true;
        }
        Class<Annotation> valid = (Class<Annotation>) ClassUtils.forNameQuietly("javax.validation.Valid");
        if(valid != null){
            Annotation a = AnnotationUtils.findAnnotation(valid, parameterAnnotations);
            return (a != null);
        }
        return false;
    }


}
