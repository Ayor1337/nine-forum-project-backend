package com.ayor.service;

public interface DataRepairService {

    String initializeMissingRelatedRecords();

    String rebuildSearchIndex();
}
