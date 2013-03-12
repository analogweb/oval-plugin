package org.analogweb.oval;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.exception.ValidationFailedException;

import org.analogweb.ContainerAdaptor;
import org.analogweb.Invocation;
import org.analogweb.InvocationArguments;
import org.analogweb.InvocationMetadata;
import org.analogweb.annotation.On;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

public class OvalInvocationProcessorTest {

	private OvalInvocationProcessor processor;
	private InvocationMetadata metadata;
	private InvocationArguments args;
	private Invocation invocation;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		processor = new OvalInvocationProcessor();
		metadata = mock(InvocationMetadata.class);
		invocation = mock(Invocation.class);
		args = mock(InvocationArguments.class);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOnInvoke() throws Exception {
		Method method = EntryPointInstance.class.getMethod("doAnything",
				Bean.class, ConstraintViolations.class);
		when(metadata.getArgumentTypes())
				.thenReturn(method.getParameterTypes());
		when(metadata.getInvocationClass()).thenReturn(
				(Class) EntryPointInstance.class);
		when(metadata.getMethodName()).thenReturn("doAnything");
		when(invocation.getInvocationArguments()).thenReturn(args);
		when(args.asList())
				.thenReturn(Arrays.asList((Object) new Bean(), null));
		processor.onInvoke(invocation, metadata);
		ArgumentCaptor<ConstraintViolations> violations = ArgumentCaptor
				.forClass(ConstraintViolations.class);
		verify(args).putInvocationArgument(eq(1), violations.capture());
		ConstraintViolations<?> actual = violations.getValue();
		assertThat(actual.all().size(), is(1));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOnInvokeWithoutAssertValid() throws Exception {
		Method method = EntryPointInstance.class.getMethod("doNothing",
				String.class);
		when(metadata.getArgumentTypes())
				.thenReturn(method.getParameterTypes());
		when(metadata.getInvocationClass()).thenReturn(
				(Class) EntryPointInstance.class);
		when(metadata.getMethodName()).thenReturn("doNothing");
		when(invocation.getInvocationArguments()).thenReturn(args);
		when(args.asList()).thenReturn(Collections.emptyList());
		// nothing to do.
		processor.onInvoke(invocation, metadata);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOnInvokeWithViolationWithoutConstraintViolations()
			throws Exception {
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
		Method method = EntryPointInstance.class.getMethod("doSomething",
				Bean.class);
		when(metadata.getArgumentTypes())
				.thenReturn(method.getParameterTypes());
		when(metadata.getInvocationClass()).thenReturn(
				(Class) EntryPointInstance.class);
		when(metadata.getMethodName()).thenReturn("doSomething");
		when(invocation.getInvocationArguments()).thenReturn(args);
		when(args.asList()).thenReturn(Arrays.asList((Object) new Bean()));
		processor.onInvoke(invocation, metadata);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOnInvokeWithMethodViolationWithoutConstraintViolations()
			throws Exception {
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
		Method method = EntryPointInstance.class.getMethod("doSomethingElse",
				Bean.class);
		when(metadata.getArgumentTypes())
				.thenReturn(method.getParameterTypes());
		when(metadata.getInvocationClass()).thenReturn(
				(Class) EntryPointInstance.class);
		when(metadata.getMethodName()).thenReturn("doSomething");
		when(invocation.getInvocationArguments()).thenReturn(args);
		when(args.asList()).thenReturn(Arrays.asList((Object) new Bean()));
		processor.onInvoke(invocation, metadata);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testOnInvokeWithContainerAdaptor() throws Exception {
		Validator validator = new Validator() {
			@Override
			public List<ConstraintViolation> validate(Object validatedObject)
					throws IllegalArgumentException, ValidationFailedException {
				// Custom validator always return empty violations.
				return Collections.emptyList();
			}
		};
		ContainerAdaptor container = mock(ContainerAdaptor.class);
		when(container.getInstanceOfType(Validator.class))
				.thenReturn(validator);
		processor.setModulesContainerAdaptor(container);

		Method method = EntryPointInstance.class.getMethod("doAnything",
				Bean.class, ConstraintViolations.class);
		when(metadata.getArgumentTypes())
				.thenReturn(method.getParameterTypes());
		when(metadata.getInvocationClass()).thenReturn(
				(Class) EntryPointInstance.class);
		when(metadata.getMethodName()).thenReturn("doAnything");
		when(invocation.getInvocationArguments()).thenReturn(args);
		when(args.asList())
				.thenReturn(Arrays.asList((Object) new Bean(), null));
		processor.onInvoke(invocation, metadata);
		ArgumentCaptor<ConstraintViolations> violations = ArgumentCaptor
				.forClass(ConstraintViolations.class);
		verify(args).putInvocationArgument(eq(1), violations.capture());
		ConstraintViolations<?> actual = violations.getValue();
		assertThat(actual.all().size(), is(0));
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
		@AssertValid
		public String doSomethingElse(Bean b) {
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
