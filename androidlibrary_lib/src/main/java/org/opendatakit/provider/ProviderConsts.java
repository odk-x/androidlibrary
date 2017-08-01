/*
 * Copyright (C) 2015 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.provider;

/**
 * This is probably dead, but still used in services.submissions.provider.SubmissionProvider
 */
@SuppressWarnings("unused")
public final class ProviderConsts {
  /**
   * Submission Provider
   */

  // for XML formatted submissions
  @SuppressWarnings("WeakerAccess")
  public static final String XML_SUBMISSION_AUTHORITY = "org.opendatakit.provider.submission.xml";
  // the full content provider prefix
  @SuppressWarnings("unused")
  public static final String XML_SUBMISSION_URL_PREFIX = "content://" + XML_SUBMISSION_AUTHORITY;

  /**
   * Do not instantiate this class
   */
  private ProviderConsts() {
  }
}
