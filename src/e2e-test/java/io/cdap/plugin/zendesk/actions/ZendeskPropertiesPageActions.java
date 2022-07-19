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

package io.cdap.plugin.zendesk.actions;

import com.google.cloud.bigquery.TableResult;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cdap.e2e.pages.locators.CdfPluginPropertiesLocators;
import io.cdap.e2e.utils.BigQueryClient;
import io.cdap.e2e.utils.ElementHelper;
import io.cdap.e2e.utils.PluginPropertyUtils;
import io.cdap.e2e.utils.SeleniumHelper;
import io.cdap.plugin.tests.hooks.TestSetupHooks;
import io.cdap.plugin.utils.enums.Subdomains;
import io.cdap.plugin.zendesk.locators.ZendeskPropertiesPage;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zendesk batch source - Properties page - Actions.
 */
public class ZendeskPropertiesPageActions {
  private static final Logger logger = LoggerFactory.getLogger(ZendeskPropertiesPageActions.class);
  private static Gson gson = new Gson();

  private static List<String> bigQueryrows = new ArrayList<>();

  static {
    SeleniumHelper.getPropertiesLocators(ZendeskPropertiesPage.class);
  }

  public static void configureSubdomains(List<String> subdomainList) {
    List<Subdomains> tables = new ArrayList<>();

    for (String table : subdomainList) {
      tables.add(Subdomains.valueOf(table));
    }
    fillsubdomainsInSubdomainSpecificationSection(tables);
  }

  public static void fillsubdomainsInSubdomainSpecificationSection(List<Subdomains> subdomainList) {
    for (int i = 0; i < subdomainList.size() - 1; i++) {
      ElementHelper.clickOnElement(ZendeskPropertiesPage.addRowButtonInSubdomainsField.get(i));
    }
    for (int i = 0; i < subdomainList.size(); i++) {
      logger.info("Fill subdomain as: " + subdomainList.get(i).value);
      ElementHelper.sendKeys(ZendeskPropertiesPage.subdomainsInputs.get(i), subdomainList.get(i).value);
    }
  }

  public static void selectDropdowWithMultipleOptionsForObjectsToPull(List<String> tablesList) {
    int objectsToPull = tablesList.size();
    ZendeskPropertiesPage.objectDropdownForMultiObjectsToPull.click();

    for (int i = 0; i < objectsToPull; i++) {
      if (tablesList.get(i).contains("Groups")) {
        continue;
      }
      if (objectsToPull == 1 && !tablesList.get(i).contains("Groups")) {
        ZendeskPropertiesPage.selectOptionGroups.click();
      }
      logger.info("Select checkbox option: " + tablesList.get(i));
      ElementHelper.selectCheckbox(ZendeskPropertiesPage.locateObjectCheckBoxInMultiObjectsSelector(tablesList.get(i)));
    }
    ElementHelper.clickUsingActions(CdfPluginPropertiesLocators.pluginPropertiesPageHeader);
  }

  public static void selectDropdowWithMultipleOptionsForObjectsToSkip(List<String> tablesList) {
    int objectsToPull = tablesList.size();

    ZendeskPropertiesPage.objectDropdownForMultiObjectsToSkip.click();

    for (int i = 0; i < objectsToPull; i++) {
      logger.info("Select checkbox option: " + tablesList.get(i));
      ElementHelper.selectCheckbox(ZendeskPropertiesPage.locateObjectCheckBoxInMultiObjectsSelector(tablesList.get(i)));
    }
    ElementHelper.clickUsingActions(CdfPluginPropertiesLocators.pluginPropertiesPageHeader);
  }

  public static void verifyIfRecordCreatedInSinkForSingleObjectIsCorrect(String expectedOutputFile)
    throws IOException, InterruptedException {

    List<String> expectedOutput = new ArrayList<>();
    PluginPropertyUtils.addPluginProp(expectedOutputFile, Paths.get(TestSetupHooks.class.getResource
      ("/" + PluginPropertyUtils.pluginProp(expectedOutputFile)).getPath()).toString());

    try (BufferedReader bf1 = Files.newBufferedReader(Paths.get(PluginPropertyUtils.pluginProp(expectedOutputFile)))) {
      String line;
      while ((line = bf1.readLine()) != null) {
        expectedOutput.add(line);
      }
    }
    getBigQueryTableData(TestSetupHooks.bqTargetDataset, TestSetupHooks.bqTargetTable);
    for (int row = 0; row < bigQueryrows.size(); row++) {
      Assert.assertTrue(ZendeskPropertiesPageActions.compareValueOfBothResponses(expectedOutput.get(row),
                                                                                 bigQueryrows.get(row)));
    }
  }

  public static void verifyIfRecordCreatedInSinkForMultipleObjectsAreCorrect(String expectedOutputFile) throws IOException,
    InterruptedException {

    List<String> expectedOutput = new ArrayList<>();
    PluginPropertyUtils.addPluginProp(expectedOutputFile, Paths.get(TestSetupHooks.class.getResource
      ("/" + PluginPropertyUtils.pluginProp(expectedOutputFile)).getPath()).toString());

    try (BufferedReader bf1 = Files.newBufferedReader(Paths.get(PluginPropertyUtils.pluginProp(expectedOutputFile)))) {
      String line;
      while ((line = bf1.readLine()) != null) {
        expectedOutput.add(line);
      }
    }
    List<String> bigQueryDatasetTables = new ArrayList<>();
    TableResult tablesSchema = ZendeskPropertiesPageActions.getTableNamesFromDataSet("ZendeskAutomation_Multi1");
    tablesSchema.iterateAll().forEach(value -> bigQueryDatasetTables.add(value.get(0).getValue().toString()));

    for (int table = 0; table < bigQueryDatasetTables.size(); table++) {
      getBigQueryTableData("ZendeskAutomation_Multi1", bigQueryDatasetTables.get(table));
    }

    for (int row = 0; row < bigQueryrows.size(); row++) {
      Assert.assertTrue(ZendeskPropertiesPageActions.compareValueOfBothResponses(expectedOutput.get(row),
                                                                                 bigQueryrows.get(row)));
    }
  }

  public static void validateOutputRecordsInOutputFolderIsEqualToExpectedOutputFile(String outputFolder
    , String expectedOutputFile) throws IOException {

    List<String> expectedOutput = new ArrayList<>();
    try (BufferedReader bf1 = Files.newBufferedReader(Paths.get(PluginPropertyUtils.pluginProp(expectedOutputFile)))) {
      String line;
      while ((line = bf1.readLine()) != null) {
        expectedOutput.add(line);
      }

      List<Path> partFiles = Files.walk(Paths.get(PluginPropertyUtils.pluginProp(outputFolder)))
        .filter(Files::isRegularFile)
        .filter(file -> file.toFile().getName().startsWith("part-r")).collect(Collectors.toList());

      for (Path partFile : partFiles) {
        try (BufferedReader bf = Files.newBufferedReader(partFile.toFile().toPath())) {
          String line1;
          int index = 0;
          while ((line1 = bf.readLine()) != null) {
            System.out.println("line1:" + line1);

            if (!compareValueOfBothResponses(expectedOutput.get(index), line1)) {
              Assert.fail("Output records are not equal to expected output");
            }
            index++;
          }
        }
      }
    }
  }

  static boolean compareValueOfBothResponses(String zendeskResponse, String bigQueryResponse) {
    Type type = new TypeToken<Map<String, Object>>() {
    }.getType();

    Map<String, Object> zendeskResponseInmap = gson.fromJson(zendeskResponse, type);
    Map<String, Object> bigQueryResponseInMap = gson.fromJson(bigQueryResponse, type);
    MapDifference<String, Object> mapDifference = Maps.difference(zendeskResponseInmap, bigQueryResponseInMap);

    return mapDifference.areEqual();
  }

  public static List<String> getBigQueryTableData(String dataset, String table)
    throws IOException, InterruptedException {
    String projectId = PluginPropertyUtils.pluginProp("projectId");
    String selectQuery = "SELECT TO_JSON(t) FROM `" + projectId + "." + dataset + "." + table + "` AS t";
    TableResult result = BigQueryClient.getQueryResult(selectQuery);
    result.iterateAll().forEach(value -> bigQueryrows.add(value.get(0).getValue().toString()));

    return bigQueryrows;
  }

  public static TableResult getTableNamesFromDataSet(String bqTargetDataset) throws IOException, InterruptedException {
    String projectId = PluginPropertyUtils.pluginProp("projectId");
    String selectQuery = "SELECT table_name FROM `" + projectId + "." + bqTargetDataset +
      "`.INFORMATION_SCHEMA.TABLES ";

    return BigQueryClient.getQueryResult(selectQuery);
  }
}

