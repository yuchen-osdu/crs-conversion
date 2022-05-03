package org.opengroup.osdu.crs.osducoreserviceclient.storage;

import org.opengroup.osdu.core.common.model.storage.Record;

public interface IStorageClient {
    Record getRecord(String record);
}
