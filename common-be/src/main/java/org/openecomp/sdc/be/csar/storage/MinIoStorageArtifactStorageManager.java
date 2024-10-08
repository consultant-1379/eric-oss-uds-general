/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.csar.storage;

import static org.openecomp.sdc.common.errors.Messages.EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import java.io.InputStream;
import java.util.Map;
import lombok.Getter;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.Credentials;
import org.openecomp.sdc.be.csar.storage.MinIoStorageArtifactStorageConfig.EndPoint;
import org.openecomp.sdc.be.csar.storage.exception.ArtifactStorageException;
import org.openecomp.sdc.common.CommonConfigurationManager;
import commonapplogging.src.main.java.org.openecomp.sdc.common.log.wrappers.Logger;

public class MinIoStorageArtifactStorageManager implements ArtifactStorageManager {

    private static final Logger LOGGER = Logger.getLogger(MinIoStorageArtifactStorageManager.class);
    private static final String ENDPOINT = "endpoint";
    private static final String CREDENTIALS = "credentials";
    private static final String TEMP_PATH = "tempPath";
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";
    @Getter
    private final MinIoStorageArtifactStorageConfig storageConfiguration;
    private final MinioClient minioClient;

    public MinIoStorageArtifactStorageManager() {
        this.storageConfiguration = readMinIoStorageArtifactStorageConfig();
        this.minioClient = initMinioClient();
    }

    //for testing only
    MinIoStorageArtifactStorageManager(final ArtifactStorageConfig storageConfiguration) {
        this.storageConfiguration = (MinIoStorageArtifactStorageConfig) storageConfiguration;
        this.minioClient = initMinioClient();
    }

    @Override
    public ArtifactInfo persist(final String vspId, final String versionId, final ArtifactInfo uploadedArtifactInfo) {
        final MinIoArtifactInfo minioObjectTemp = (MinIoArtifactInfo) uploadedArtifactInfo;
        LOGGER.debug("PERSIST - bucket: '{}', object: '{}'", minioObjectTemp.getBucket(), minioObjectTemp.getObjectName());
        try {
            // Get information of an object.
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioObjectTemp.getBucket())
                    .object(minioObjectTemp.getObjectName())
                    .build()
            );

        } catch (final Exception e) {
            LOGGER.security("Failed to retrieve uploaded artifact with bucket '{}' and name '{}' while persisting", minioObjectTemp.getBucket(),
                minioObjectTemp.getObjectName(), e);
            throw new ArtifactStorageException(
                String.format("Failed to retrieve uploaded artifact with bucket '%s' and name '%s' while persisting",
                    minioObjectTemp.getBucket(), minioObjectTemp.getObjectName()), e);
        }
        return new MinIoArtifactInfo(vspId, versionId);
    }

    @Override
    public ArtifactInfo upload(final String bucketName, final String versionId, final InputStream fileToUpload) {
        LOGGER.info("In artifactInfo.upload, bucket name is: " + bucketName);
        try {
            // Make bucket if not exist.
            final boolean found = bucketExists(bucketName);

            if (!found) {
                // Make a new bucket ${vspId} .
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                LOGGER.info("Bucket '{}' already exists.", bucketName);
            }

            put(bucketName, versionId, fileToUpload);

        } catch (final Exception e) {
            LOGGER.security("Failed to upload artifact - bucket: '{}', object: '{}' | Category: WARN", bucketName, versionId, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }

        return new MinIoArtifactInfo(bucketName, versionId);
    }

    @Override
    public void put(final String bucketName, final String name, final InputStream fileToUpload) {
        LOGGER.debug("BEGIN -> PUT - bucket: '{}', object: '{}'", bucketName, name);
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(name)
                    .stream(fileToUpload, -1, storageConfiguration.getUploadPartSize())
                    .build()
            );
        } catch (final Exception e) {
            LOGGER.security("Failed to put - bucket: '{}', object: '{}' | Category: WARN", bucketName, name, e);
            throw new ArtifactStorageException("Failed to upload artifact", e);
        }
        LOGGER.debug("SUCCESS -> PUT - bucket: '{}', object: '{}'", bucketName, name);
    }

    @Override
    public boolean isEnabled() {
        return storageConfiguration != null && storageConfiguration.isEnabled();
    }

    @Override
    public InputStream get(final ArtifactInfo artifactInfo) {
        final MinIoArtifactInfo minioObject = (MinIoArtifactInfo) artifactInfo;
        return get(minioObject.getBucket(), minioObject.getObjectName());
    }

    @Override
    public InputStream get(final String bucketID, final String objectID) {
        LOGGER.debug("GET - bucket: '{}', object: '{}'", bucketID, objectID);
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketID)
                .object(objectID)
                .build());
        } catch (final Exception e) {
            LOGGER.security("Failed to get - bucket: '{}', object: '{}'", bucketID, objectID, e);
            throw new ArtifactStorageException("Failed to get Object", e);
        }
    }

    @Override
    public void delete(final ArtifactInfo artifactInfo) {
        final MinIoArtifactInfo minioObject = (MinIoArtifactInfo) artifactInfo;
        LOGGER.debug("DELETE - bucket: '{}', object: '{}'", minioObject.getBucket(), minioObject.getObjectName());
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioObject.getBucket())
                .object(minioObject.getObjectName())
                .bypassGovernanceMode(true)
                .build());
        } catch (final Exception e) {
            LOGGER.error("Failed to delete - bucket: '{}', object: '{}'", minioObject.getBucket(), minioObject.getObjectName(), e);
            throw new ArtifactStorageException(String.format("Failed to delete '%s'", minioObject.getObjectName()), e);
        }

    }

    @Override
    public void delete(final String vspId) {
        LOGGER.debug("DELETE VSP - bucket: '{}'", vspId);
        if (!bucketExists(vspId)) {
            LOGGER.debug("VSP '{}' bucket was not found while trying to delete it", vspId);
            return;
        }
        final var listObjects = minioClient.listObjects(ListObjectsArgs.builder().bucket(vspId).build());
        listObjects.forEach(itemResult -> {
            Item versionId;
            try {
                versionId = itemResult.get();
            } catch (final Exception e) {
                LOGGER.security("Failed to get versionId for VSP - bucket: '{}' | Category: WARN", vspId, e);
                throw new ArtifactStorageException(String.format("Failed to delete VSP '%s'", vspId), e);
            }
            delete(new MinIoArtifactInfo(vspId, versionId.objectName()));
        });
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(vspId).build());
        } catch (final Exception e) {
            LOGGER.error("Failed to delete VSP - bucket: '{}'", vspId, e);
            throw new ArtifactStorageException(String.format("Failed to delete VSP '%s'", vspId), e);
        }
    }

    private boolean bucketExists(final String vspId) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(vspId).build());
        } catch (final Exception e) {
            LOGGER.security("An unexpected error occurred while checking for vsp '%s'| Category: WARN", vspId);
            throw new ArtifactStorageException(String.format("An unexpected error occurred while checking for vsp '%s'", vspId), e);
        }
    }

    @Override
    public boolean exists(final String vspId) {
        LOGGER.debug("exists - bucket: '{}'", vspId);
        return bucketExists(vspId);
    }

    private MinIoStorageArtifactStorageConfig readMinIoStorageArtifactStorageConfig() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        final Map<String, Object> endpoint = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, ENDPOINT, null);
        final Map<String, Object> creds = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, CREDENTIALS, null);
        final String tempPath = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, TEMP_PATH, null);
        final int uploadPartSize = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "uploadPartSize", 50_000_000);
        final String bucketName = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "bucketName", null);


        if (endpoint == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(ENDPOINT));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(ENDPOINT));
        }
        if (creds == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(CREDENTIALS));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(CREDENTIALS));
        }
        if (tempPath == null) {
            LOGGER.error(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(TEMP_PATH));
            throw new ArtifactStorageException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING.formatMessage(TEMP_PATH));
        }
        LOGGER.info("ArtifactConfig.endpoint: '{}'", endpoint);
        LOGGER.info("ArtifactConfig.credentials: '{}'", creds);
        LOGGER.info("ArtifactConfig.tempPath: '{}'", tempPath);
        LOGGER.info("ArtifactConfig.uploadPartSize: '{}'", uploadPartSize);


        final String host = (String) endpoint.getOrDefault("host", null);
        final int port = (int) endpoint.getOrDefault("port", 0);
        final boolean secure = (boolean) endpoint.getOrDefault("secure", false);

        final String accessKey = (String) creds.getOrDefault("accessKey", null);
        final String secretKey = (String) creds.getOrDefault("secretKey", null);

        return new MinIoStorageArtifactStorageConfig
            (true, new EndPoint(host, port, secure), new Credentials(accessKey, secretKey), tempPath, uploadPartSize, bucketName);
    }

    private MinioClient initMinioClient() {
        final EndPoint storageConfigurationEndPoint = storageConfiguration.getEndPoint();
        final Credentials storageConfigurationCredentials = storageConfiguration.getCredentials();

        return MinioClient.builder()
            .endpoint(storageConfigurationEndPoint.getHost(), storageConfigurationEndPoint.getPort(), storageConfigurationEndPoint.isSecure())
            .credentials(storageConfigurationCredentials.getAccessKey(), storageConfigurationCredentials.getSecretKey())
            .build();
    }

}
