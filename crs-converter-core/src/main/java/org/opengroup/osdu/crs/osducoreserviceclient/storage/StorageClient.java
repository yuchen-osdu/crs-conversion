package org.opengroup.osdu.crs.osducoreserviceclient.storage;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.model.storage.StorageException;
import org.opengroup.osdu.core.common.storage.IStorageFactory;
import org.opengroup.osdu.core.common.storage.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageClient implements IStorageClient{

    @Autowired
    private DpsHeaders headers;
    @Autowired
    IStorageFactory factory;

    @Override
    public Record getRecord(String record) {
        try {
            IStorageService service = this.factory.create(headers);
            Record upsertRecords = service.getRecord(record);
            return upsertRecords;
        } catch (StorageException e) {
            String error = e.getHttpResponse().getBody();
            throw new AppException(e.getHttpResponse().getResponseCode(), "Error getting record",
                    "An unexpected error occurred when getting record: " + error, e);
        }
    }
}

