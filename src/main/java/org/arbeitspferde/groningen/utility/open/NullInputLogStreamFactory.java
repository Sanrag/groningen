/* Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.arbeitspferde.groningen.utility.open;
import com.google.inject.Singleton;

import org.arbeitspferde.groningen.utility.InputLogStream;
import org.arbeitspferde.groningen.utility.InputLogStreamFactory;

import java.io.InputStream;

/**
 * A simple factory that does NOT yet create {@link InputLogStream} instances.
 */
@Singleton
public class NullInputLogStreamFactory implements InputLogStreamFactory {

  @Override
  public InputLogStream forStream(InputStream inputStream) {
    throw new RuntimeException("Not implemented.");
  }
}
