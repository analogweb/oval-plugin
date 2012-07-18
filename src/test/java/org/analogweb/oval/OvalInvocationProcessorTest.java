package org.analogweb.oval;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotNull;

import org.analogweb.Invocation;
import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.annotation.On;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OvalInvocationProcessorTest {

    private OvalInvocationProcessor processor;
    private Invocation invocation;
    private InvocationMetadata metadata;
    private InvocationArguments args;

    @Before
    public void setUp() throws Exception {
        processor = new OvalInvocationProcessor();
        invocation = mock(Invocation.class);
        metadata = mock(InvocationMetadata.class);
        args = mock(InvocationArguments.class);
    }

    @Test
    public void testOnInvoke() throws Exception {
        Method method = EntryPointInstance.class.getMethod("doAnything", Bean.class,
                ConstraintViolations.class);
        when(metadata.getArgumentTypes()).thenReturn(method.getParameterTypes());
        when(args.asList()).thenReturn(Arrays.asList((Object)new Bean(), null));
        processor.onInvoke(method, invocation,metadata, args);
        ArgumentCaptor<ConstraintViolations> violations = ArgumentCaptor.forClass(ConstraintViolations.class);
        verify(args).putInvocationArgument(eq(1), violations.capture());
        ConstraintViolations<?> actual = violations.getValue();
        assertThat(actual.all().size(),is(1));
    }

    @On
    public static class EntryPointInstance {
        @On
        public String doSomething(@AssertValid Bean b) {
            return "Do something.";
        }

        @On
        public String doAnything(@AssertValid Bean b, ConstraintViolations violations) {
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
