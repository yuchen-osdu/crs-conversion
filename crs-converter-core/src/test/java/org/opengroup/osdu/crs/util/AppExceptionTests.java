package org.opengroup.osdu.crs.util;

import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppExceptionTests {

    @Test
    public void constructorTest() {
        AppException exception = new AppException(200, "unknown error", "this error occurred:");
        assertNotNull(exception);

        AppError error = exception.getError();
        assertNotNull(error);

        assertEquals(200, error.getCode());
        assertEquals("unknown error", error.getReason());
        assertEquals("this error occurred:", error.getMessage());
    }

    @Test
    public void testUnauthorized() {
        String debuggingInfo = "dummy debuggingInfo";
        AppException exception = AppException.createUnauthorized(debuggingInfo);
        assertNotNull(exception);

        AppError error = exception.getError();
        assertNotNull(error);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, error.getCode());
        assertEquals("Unauthorized", error.getReason());
        assertEquals("The user is not authorized to perform this action", error.getMessage());
        assertEquals(debuggingInfo, error.getDebuggingInfo());
    }
}
