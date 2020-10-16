/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Stanislav Menshykov
 * Created: 3/28/16  10:42 AM
 */
package com.comcast.xconf.thucydides.util.dcm;

import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UploadRepositoryUtils {
    private static final String UPLOAD_REPOSITORY_URL = "dcm/uploadRepository";

    public static String defaultName = "uploadRepositoryName";

    public static void doCleanup() throws Exception {
        GenericTestUtils.deleteEntities(UPLOAD_REPOSITORY_URL, UploadRepository.class);
    }

    public static UploadRepository createAndSaveDefaultUploadRepository() throws Exception {
        UploadRepository result = createDefaultUploadRepository();
        HttpClient.post(GenericTestUtils.buildFullUrl(UPLOAD_REPOSITORY_URL), result);

        return result;
    }

    public static UploadRepository createAndSaveUploadRepository(String name) throws IOException {
        UploadRepository uploadRepository = createDefaultUploadRepository();
        uploadRepository.setId(UUID.randomUUID().toString());
        uploadRepository.setName(name);
        HttpClient.post(GenericTestUtils.buildFullUrl(UPLOAD_REPOSITORY_URL), uploadRepository);

        return uploadRepository;
    }

    public static List<UploadRepository> createAndSaveUploadRepositoryList() throws Exception {
        return Lists.newArrayList(
               createAndSaveUploadRepository("uploadRepository1"),
               createAndSaveUploadRepository("uploadRepository2")
        );
    }

    public static UploadRepository createDefaultUploadRepository() {
        UploadRepository result = new UploadRepository();
        result.setId(UUID.fromString("1-2-3-4-5").toString());
        result.setName(defaultName);
        result.setDescription("description");
        result.setUrl("http://text.com");

        return result;
    }
}
