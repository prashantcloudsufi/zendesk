/*
 * Copyright © 2022 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.cdap.plugin.zendesk.source.batch;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZendeskInputFormatProviderTest {

  @Test
  public void testGetInputFormatClassName() {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "2019-01-01T23:01:01Z",
      "2019-01-01T23:01:01Z",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "{}");
    ZendeskInputFormatProvider zendeskInputFormatProvider = new ZendeskInputFormatProvider(config, null
                                                     , null, "cdap.zendesk.plugin.name");
    String className = zendeskInputFormatProvider.getInputFormatClassName();
    Assert.assertEquals("io.cdap.plugin.zendesk.source.batch.ZendeskInputFormat", className);
  }

  @Test
  public void testGetInputFormatConfiguration() {
    ZendeskBatchSourceConfig config = new ZendeskBatchSourceConfig(
      "reference",
      "email@test.com",
      "apiToken",
      "subdomain",
      "Groups",
      "",
      "2019-01-01T23:01:01Z",
      "2019-01-01T23:01:01Z",
      "satisfactionRatingsScore",
      20,
      300,
      300,
      "https://%s.zendesk.com/api/v2/%s",
      "{}");
    List<String> objects = new ArrayList<>();
    objects.add("object1");
    ZendeskInputFormatProvider zendeskInputFormatProvider = new ZendeskInputFormatProvider(config, objects,
                                                                                           null,
                                                                                           "cdap.zendesk.plugin.name");
    Map<String, String> map = zendeskInputFormatProvider.getInputFormatConfiguration();
    Assert.assertEquals(4, map.size());
    Assert.assertEquals("[\"object1\"]", map.get("cdap.zendesk.objects"));
  }
}
