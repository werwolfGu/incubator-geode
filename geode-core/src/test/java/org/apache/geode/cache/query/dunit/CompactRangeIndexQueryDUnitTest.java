/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.cache.query.dunit;

import static org.apache.geode.internal.cache.execute.DistributedRegionFunctionExecutionDUnitTest.region;
import static java.rmi.activation.ActivationGroup.getSystem;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheException;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.QueryTestUtils;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.cache.query.data.Portfolio;
import org.apache.geode.cache.query.data.PortfolioPdx;
import org.apache.geode.cache.query.internal.ResultsSet;
import org.apache.geode.cache.query.internal.index.IndexManager;
import org.apache.geode.cache.query.internal.index.IndexManager.TestHook;
import org.apache.geode.cache30.CacheSerializableRunnable;
import org.apache.geode.test.dunit.Assert;
import org.apache.geode.test.dunit.AsyncInvocation;
import org.apache.geode.test.dunit.DistributedTestUtils;
import org.apache.geode.test.dunit.Host;
import org.apache.geode.test.dunit.Invoke;
import org.apache.geode.test.dunit.SerializableRunnable;
import org.apache.geode.test.dunit.VM;
import org.apache.geode.test.dunit.Wait;
import org.apache.geode.test.dunit.cache.internal.JUnit4CacheTestCase;
import org.apache.geode.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.geode.test.junit.categories.DistributedTest;

@Category(DistributedTest.class)
public class CompactRangeIndexQueryDUnitTest extends JUnit4CacheTestCase {

  VM vm0;

  @Override
  public final void postSetUp() throws Exception {
    getSystem();
    Invoke.invokeInEveryVM(new SerializableRunnable("getSystem") {
      public void run() {
        getSystem();
      }
    });
    Host host = Host.getHost(0);
    vm0 = host.getVM(0);
  }

  @Test
  public void multipleNotEqualsClausesOnAPartitionedRegionShouldReturnCorrectResults() throws Exception {
    Cache cache = getCache();
    Region region = cache.createRegionFactory(RegionShortcut.PARTITION).create("portfolios");
    int numMatching = 10;
    QueryService qs = cache.getQueryService();
    qs.createIndex("statusIndex", "p.status", "/portfolios p");
    for (int i = 0; i < numMatching * 2; i++) {
      PortfolioPdx p = new PortfolioPdx(i);
      if (i < numMatching) {
        p.status = "1";
      }
      region.put("KEY-"+ i, p);
    }

    Query q = qs.newQuery("select * from /portfolios p where p.pk <> '0' and p.status <> '0' and p.status <> '1' and p.status <> '2'");
    SelectResults rs = (SelectResults) q.execute();
    assertEquals( numMatching, rs.size());
  }

}
