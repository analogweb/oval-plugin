package org.analogweb.oval;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.analogweb.ModulesBuilder;
import org.junit.Before;
import org.junit.Test;

public class OvalPluginModuleConfigTest {

    private OvalPluginModuleConfig config;
    private ModulesBuilder builder;

    @Before
    public void setUp() throws Exception {
        config = new OvalPluginModuleConfig();
        builder = mock(ModulesBuilder.class);
    }

    @Test
    public void test() {
        when(builder.addInvocationInterceptorClass(OvalInvocationProcessor.class))
                .thenReturn(builder);
        ModulesBuilder actual = config.prepare(builder);
        assertThat(actual, is(builder));
        verify(builder).addInvocationInterceptorClass(OvalInvocationProcessor.class);
    }

}
