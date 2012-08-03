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

package org.arbeitspferde.groningen;

import com.google.common.base.Preconditions;

import org.arbeitspferde.groningen.config.PipelineScoped;

/**
 * Id used to uniquely identify pipelines
 */
@PipelineScoped
public class PipelineId implements Comparable<PipelineId> {
  private final String stringRepresentation;

  public PipelineId(String stringRepresentation) {
    Preconditions.checkNotNull(stringRepresentation);

    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof PipelineId)) {
      return false;
    } else {
      return stringRepresentation.equals(((PipelineId) obj).stringRepresentation);
    }
  }

  @Override
  public int hashCode() {
    return stringRepresentation.hashCode();
  }

  @Override
  public int compareTo(PipelineId o) {
    return stringRepresentation.compareTo(o.stringRepresentation);
  }
}
