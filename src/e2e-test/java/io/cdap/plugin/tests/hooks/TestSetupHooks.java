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

package io.cdap.plugin.tests.hooks;

import com.google.cloud.bigquery.BigQueryException;
import io.cdap.e2e.utils.BigQueryClient;
import io.cdap.e2e.utils.PluginPropertyUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import stepsdesign.BeforeActions;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents Test Setup and Clean up hooks.
 */
public class TestSetupHooks {
  public static String bqTargetDataset = StringUtils.EMPTY;
  public static String bqTargetTable = StringUtils.EMPTY;
  public static String bqSourceDataset;
  public static String bqSourceTable;
  private static boolean firstFileSinkTestFlag = true;
  private static String fileSinkOutputFolder = StringUtils.EMPTY;

  @Before(order = 4, value = "@BQ_SINK")
  public static void setTempTargetBQDataset() {
    bqTargetDataset = "TestSN_dataset" + RandomStringUtils.randomAlphanumeric(10);
    BeforeActions.scenario.write("BigQuery Target dataset name: " + bqTargetDataset);
  }

  @Before(order = 5, value = "@BQ_SINK")
  public static void setTempTargetBQTable() {
    bqTargetTable = "TestSN_table" + RandomStringUtils.randomAlphanumeric(10);
    BeforeActions.scenario.write("BigQuery Target table name: " + bqTargetTable);
  }

  @Before(order = 6, value = "@BQ_SOURCE")
  public static void setTempSourceBQDataset() {
    bqSourceDataset = "TestSN_dataset" + RandomStringUtils.randomAlphanumeric(10);
    BeforeActions.scenario.write("BigQuery Source table name: " + bqSourceDataset);
  }

  @Before(order = 7, value = "@BQ_SOURCE")
  public static void setTempSourceBQTable() {
    bqSourceTable = "TestSN_table" + RandomStringUtils.randomAlphanumeric(10);
    BeforeActions.scenario.write("BigQuery Source table name: " + bqSourceTable);
  }

  @After(order = 1, value = "@BQ_SINK_CLEANUP")
  public static void deleteTempTargetBQTable() throws IOException, InterruptedException {
    try {
      BigQueryClient.dropBqQuery(bqTargetDataset, bqTargetTable);
      BeforeActions.scenario.write("BigQuery Target table: " + bqTargetTable + " is deleted successfully");
      bqTargetTable = StringUtils.EMPTY;
    } catch (BigQueryException e) {
      if (e.getMessage().contains("Not found: Table")) {
        BeforeActions.scenario.write("BigQuery Target Table: " + bqTargetTable + " does not exist");
      } else {
        Assert.fail(e.getMessage());
      }
    }
  }
  @Before(order = 1, value = "@FILE_SINK_TEST")
  public static void setFileSinkAbsolutePath() {

    if (firstFileSinkTestFlag) {

      PluginPropertyUtils.addPluginProp("userTestOutputFile", Paths.get(TestSetupHooks.class.getResource
        ("/" + PluginPropertyUtils.pluginProp("userTestOutputFile")).getPath()).toString());

      fileSinkOutputFolder = PluginPropertyUtils.pluginProp("filePluginOutputFolder");
      firstFileSinkTestFlag = false;
    }
    PluginPropertyUtils.addPluginProp("filePluginOutputFolder", Paths.get("target/" + fileSinkOutputFolder + "/"
       + (new SimpleDateFormat("yyyyMMdd-HH-mm-ssSSS").format(new Date()))).toAbsolutePath().toString());
  }
}
