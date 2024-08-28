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

import lombok.Getter;
import org.openecomp.sdc.common.CommonConfigurationManager;



@Getter
public class MinIoArtifactInfo implements ArtifactInfo {

    private final String bucket;
    private final String objectName;
    private byte[] artifactBytes;
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";


    public MinIoArtifactInfo(final String bucket, final String objectName) {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        final String bucketName = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "bucketName", "default-uds-bucket");
        this.bucket = bucketName;
        this.objectName = objectName;
    }


    @Override
    public String getInfo() {
        return String.format("bucket: %s\n"
            + "object: %s", bucket, objectName);

    }

    @Override
    public byte[] getBytes() {
        return artifactBytes;
    }

    @Override
    public void setBytes(final byte[] artifactBytes) {
        this.artifactBytes = artifactBytes;
    }
}
