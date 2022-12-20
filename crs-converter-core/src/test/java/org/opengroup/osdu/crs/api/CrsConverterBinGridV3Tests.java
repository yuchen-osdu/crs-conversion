package org.opengroup.osdu.crs.api;

import static org.mockito.Mockito.lenient;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.crs.converter.CRSConverter;
import org.opengroup.osdu.crs.model.ConvertBinGridResponse;
import org.opengroup.osdu.crs.model.ConvertGeoJsonResponse;


@RunWith(MockitoJUnitRunner.class)
public class CrsConverterBinGridV3Tests {
	
	@Mock
	private CRSConverter crsConverter;

	@Mock
	private CrsConverterApiV3 crsConverterApi;
	
	@Test
	public void convertBinGridInValidRequest() {		
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		lenient().when(crsConverterApi.convertGeoJson(Mockito.any())).thenReturn(new ConvertGeoJsonResponse());
		lenient().when(crsConverterApi.convertBinGrid(Mockito.any(), httpServletRequest)).thenReturn(new ConvertBinGridResponse());
		
	}
	

}
