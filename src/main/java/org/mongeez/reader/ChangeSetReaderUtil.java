/*
 * Copyright 2011 SecondMarket Labs, LLC.
 * Copyright 2023 Hitesh Tarani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mongeez.reader;

import org.mongeez.commands.ChangeSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class ChangeSetReaderUtil {
    static void populateChangeSetResourceInfo(ChangeSet changeSet, Resource file) {
        changeSet.setFile(file.getFilename());
        if (file instanceof ClassPathResource) {
            changeSet.setResourcePath(((ClassPathResource) file).getPath());
        }
    }
}
