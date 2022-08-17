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
import org.mockito.Mockito;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ZendeskSplitTest {

  @Test
  public void testInputSplitWithNonEmptyTableName() throws IOException, InterruptedException {
    ZendeskSplit zendeskSplit = new ZendeskSplit("domain", "object");
    Assert.assertEquals("domain", zendeskSplit.getSubdomain());
    Assert.assertEquals("object", zendeskSplit.getObject());
  }

  @Test
  public void testInputSplitWithEmptyValues() throws IOException, InterruptedException {
   ZendeskSplit zendeskSplit = new ZendeskSplit();
   Assert.assertNull(zendeskSplit.getSubdomain());
   Assert.assertNull(zendeskSplit.getObject());
  }

  @Test
  public void testReadFields() throws IOException {
    ZendeskSplit zendeskSplit = new ZendeskSplit("domain", "object");
    ObjectInputStream objectInputStream = Mockito.mock(ObjectInputStream.class);
    Mockito.when(objectInputStream.readUTF()).thenReturn("Utf");
    zendeskSplit.readFields(objectInputStream);
    Assert.assertEquals("Utf", zendeskSplit.getSubdomain());
    Assert.assertEquals("Utf", zendeskSplit.getObject());
  }

  @Test
  public void testWrite() throws IOException {
    ZendeskSplit zendeskSplit = new ZendeskSplit("domain", "object");
    DataOutput dataOutput = Mockito.mock(DataOutput.class);
    zendeskSplit.write(dataOutput);
    Assert.assertEquals("domain", zendeskSplit.getSubdomain());
    Assert.assertEquals("object", zendeskSplit.getObject());
  }

  @Test
  public void testGetLocations() throws IOException, InterruptedException {
    Assert.assertEquals(String[].class, new ZendeskSplit("domain", "object").getLocations().
      getClass());
    Assert.assertEquals(0, (new ZendeskSplit("domain", "object")).getLocations().length);
  }

  @Test(expected = NullPointerException.class)
  public void testWriteWithNullData() throws IOException {
    DataOutput dataOutput = null;
    String object = "";
    String domain = "";
    ZendeskSplit zendeskSplit = new ZendeskSplit(domain, object);
    zendeskSplit.write(dataOutput);
  }

  @Test(expected = NullPointerException.class)
  public void testRead() throws IOException {
    DataInput dataInput = null;
    String object = "";
    String domain = "";
    ZendeskSplit zendeskSplit = new ZendeskSplit(domain, object);
    zendeskSplit.readFields(dataInput);
  }

  @Test
  public void testGetLength() throws IOException {
    ZendeskSplit zendeskSplit = new ZendeskSplit("domain", "object");
    Assert.assertEquals(0, zendeskSplit.getLength());
  }
}


