package org.opengroup.osdu.crs.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.opengroup.osdu.crs.api.CrsConverterApiV4;
import org.opengroup.osdu.crs.middleware.AuthenticationRequestFilter;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        CrsConverterApiV4.class,
        AuthenticationRequestFilter.class,
        AuthSecurityConfig.class})
@WebAppConfiguration
public class AuthSecurityConfigTest {
    private MockMvc mockMvc = null;

    @MockBean
    private CrsConverterApiV4 crsCatalogApi;

    @MockBean
    AuthenticationRequestFilter authenticationRequestFilter;

    @MockBean
    HandlerExceptionResolver handlerExceptionResolver;
    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testConfigureHttpSecurity() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isNotFound())
            .andReturn();
    }
}