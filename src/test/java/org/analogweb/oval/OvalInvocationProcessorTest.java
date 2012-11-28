package org.analogweb.oval;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotNull;

import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.RequestContext;
import org.analogweb.annotation.On;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

public class OvalInvocationProcessorTest {

    private OvalInvocationProcessor processor;
    private InvocationMetadata metadata;
    private InvocationArguments args;
    private RequestContext request;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        processor = new OvalInvocationProcessor();
        metadata = mock(InvocationMetadata.class);
        args = mock(InvocationArguments.class);
        request = mock(RequestContext.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOnInvoke() throws Exception {
        Method method = EntryPointInstance.class.getMethod("doAnything", Bean.class,
                ConstraintViolations.class);
        when(metadata.getArgumentTypes()).thenReturn(method.getParameterTypes());
        when(args.asList()).thenReturn(Arrays.asList((Object) new Bean(), null));
        processor.onInvoke(method, metadata, args);
        ArgumentCaptor<ConstraintViolations> violations = ArgumentCaptor
                .forClass(ConstraintViolations.class);
        verify(args).putInvocationArgument(eq(1), violations.capture());
        ConstraintViolations<?> actual = violations.getValue();
        assertThat(actual.all().size(), is(1));
    }

    @Test
    public void testOnInvokeWithoutAssertValid() throws Exception {
        Method method = EntryPointInstance.class.getMethod("doNothing", String.class);
        when(metadata.getArgumentTypes()).thenReturn(method.getParameterTypes());
        when(args.asList()).thenReturn(Collections.emptyList());
        // nothing to do.
        processor.onInvoke(method, metadata, args);
    }

    @Test
    public void testOnInvokeWithViolationWithoutConstraintViolations() throws Exception {
        thrown.expect(new BaseMatcher<ConstraintViolationException>() {
            @Override
            public boolean matches(Object arg0) {
                if (arg0 instanceof ConstraintViolationException) {
                    ConstraintViolations<ConstraintViolation> v = ((ConstraintViolationException) arg0)
                            .violations();
                    assertThat(v.all().size(), is(1));
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description arg0) {
            }
        });
        Method method = EntryPointInstance.class.getMethod("doSomething", Bean.class);
        when(metadata.getArgumentTypes()).thenReturn(method.getParameterTypes());
        when(args.asList()).thenReturn(Arrays.asList((Object) new Bean()));
        processor.onInvoke(method, metadata, args);
    }

    @Test
    // TODO 
    @Ignore
    public void testProcessException() {
        ConstraintViolations<ConstraintViolation> violations = new ConstraintViolations<ConstraintViolation>() {
            @Override
            public Collection<ConstraintViolation> all() {
                // TODO 
                return null;
            }
        };
        ConstraintViolationException ex = new ConstraintViolationException(violations);
        processor.processException(ex, request, args, metadata);
    }

    @On
    public static class EntryPointInstance {

        @On
        public String doNothing(String b) {
            return "Do nothing.";
        }

        @On
        public String doSomething(@AssertValid Bean b) {
            return "Do something.";
        }

        @On
        public String doAnything(@AssertValid Bean b,
                ConstraintViolations<ConstraintViolation> violations) {
            return "Do anything.";
        }
    }

    public static class Bean {
        @NotNull
        private String name;
        @Min(value = 0)
        private int age;
    }

}
