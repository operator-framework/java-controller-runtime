// Copyright 2020 The Operator-SDK Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.fabric8.controller.controller_runtime;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Controllers is a set of commonly used utility functions for constructing controller. */
public class Controllers {

    private static final Logger log = LoggerFactory.getLogger(Controllers.class);

    /**
     * Named thread factory for constructing controller, useful when debugging dumping status of
     * controller worker threads. e.g. for a controller named `foo`, its threads will be named
     * `foo-1`, `foo-2`...
     *
     * @param controllerName the controller name
     * @return the thread factory
     */
    public static ThreadFactory namedControllerThreadFactory(String controllerName) {
        return new ThreadFactoryBuilder().setNameFormat(controllerName + "-%d").build();
    }
}



