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
package org.apache.geode.distributed.internal;

import static org.apache.geode.distributed.ConfigurationProperties.*;
import static org.apache.geode.distributed.internal.DistributionConfig.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;

import org.apache.geode.internal.logging.GemFireLevel;
import org.apache.geode.internal.logging.LogWriterImpl;
import org.apache.geode.security.templates.SamplePostProcessor;
import org.apache.geode.security.templates.SampleSecurityManager;
import org.apache.geode.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class DistributionConfigGetSecurityPropsTest {

  @Rule
  public TestName testName = new TestName();

  @Test
  public void shouldReturnMultipleSecurityProps() {
    Properties props = new Properties();
    props.put(SECURITY_MANAGER, SampleSecurityManager.class.getName());
    props.put(SECURITY_POST_PROCESSOR, SamplePostProcessor.class.getName());
    DistributionConfig config = new DistributionConfigImpl(props);

    Properties securityProps = config.getSecurityProps();

    assertThat(securityProps).containsOnlyKeys(SECURITY_MANAGER, SECURITY_POST_PROCESSOR);
    assertThat(securityProps).containsAllEntriesOf(props);
  }

  @Test
  public void shouldConvertSecurityLogLevel() {
    String value = "config";
    Properties props = new Properties();
    props.put(SECURITY_LOG_LEVEL, value);
    DistributionConfig config = new DistributionConfigImpl(props);

    Properties securityProps = config.getSecurityProps();

    String logLevelCode = String.valueOf(LogWriterImpl.levelNameToCode(value));
    assertThat(securityProps).containsOnlyKeys(SECURITY_LOG_LEVEL);
    assertThat(securityProps.getProperty(SECURITY_LOG_LEVEL)).isEqualTo(logLevelCode);
  }

  @Test
  public void shouldNotReturnNonSecurityProps() {
    Properties props = new Properties();
    props.put(ACK_WAIT_THRESHOLD, 2);
    props.put(NAME, this.testName.getMethodName());
    DistributionConfig config = new DistributionConfigImpl(props);

    Properties securityProps = config.getSecurityProps();

    assertThat(securityProps).isEmpty();
  }

  @Test
  public void shouldReturnOnlySecurityProps() {
    Properties props = new Properties();
    props.put(ACK_WAIT_THRESHOLD, 2);
    props.put(NAME, this.testName.getMethodName());
    props.put(SECURITY_MANAGER, SampleSecurityManager.class.getName());
    DistributionConfig config = new DistributionConfigImpl(props);

    Properties securityProps = config.getSecurityProps();

    assertThat(securityProps).containsOnlyKeys(SECURITY_MANAGER);
    assertThat(securityProps.getProperty(SECURITY_MANAGER)).isEqualTo(SampleSecurityManager.class.getName());
  }

  @Test
  public void shouldReturnCustomSecurityProps() {
    String customKey = "security-username";
    String customValue = this.testName.getMethodName();
    Properties props = new Properties();
    props.put(customKey, customValue);
    DistributionConfig config = new DistributionConfigImpl(props);

    Properties securityProps = config.getSecurityProps();

    assertThat(securityProps).containsOnlyKeys(customKey);
    assertThat(securityProps.getProperty(customKey)).isEqualTo(customValue);
  }

  @Test
  public void shouldAllowDistributionConfigKeys() {
    Properties props = new Properties();

    props.setProperty(SECURITY_CLIENT_ACCESSOR_NAME, SECURITY_CLIENT_ACCESSOR_NAME_VALUE);
    props.setProperty(SECURITY_CLIENT_ACCESSOR_PP_NAME, DEFAULT_SECURITY_CLIENT_ACCESSOR_PP); // default
    props.setProperty(SECURITY_CLIENT_AUTH_INIT_NAME, SECURITY_CLIENT_AUTH_INIT_NAME_VALUE);
    props.setProperty(SECURITY_CLIENT_AUTHENTICATOR_NAME, SECURITY_CLIENT_AUTHENTICATOR_NAME_VALUE);
    addProperties(getClientExtraProperties(), props);

    props.setProperty(SECURITY_LOG_FILE_NAME, SECURITY_LOG_FILE_NAME_VALUE);
    props.setProperty(SECURITY_LOG_LEVEL_NAME, SECURITY_LOG_LEVEL_NAME_VALUE);

    props.setProperty(SECURITY_PEER_AUTH_INIT_NAME, SECURITY_PEER_AUTH_INIT_NAME_VALUE);
    props.setProperty(SECURITY_PEER_AUTHENTICATOR_NAME, SECURITY_PEER_AUTHENTICATOR_NAME_VALUE);
    props.setProperty(SECURITY_PEER_VERIFYMEMBER_TIMEOUT_NAME, String.valueOf(DEFAULT_SECURITY_PEER_VERIFYMEMBER_TIMEOUT)); // default
    addProperties(getPeerExtraProperties(), props);

    DistributionConfig config = new DistributionConfigImpl(props);

    String logLevelCode = String.valueOf(LogWriterImpl.levelNameToCode(SECURITY_LOG_LEVEL_NAME_VALUE));

    Properties securityProps = config.getSecurityProps();
    assertThat(securityProps).containsOnlyKeys(
      SECURITY_CLIENT_ACCESSOR_NAME,
      SECURITY_CLIENT_ACCESSOR_PP_NAME,
      SECURITY_CLIENT_AUTH_INIT_NAME,
      SECURITY_CLIENT_AUTHENTICATOR_NAME,
      SECURITY_LOG_FILE_NAME,
      SECURITY_LOG_LEVEL_NAME,
      SECURITY_PEER_AUTH_INIT_NAME,
      SECURITY_PEER_AUTHENTICATOR_NAME,
      SECURITY_PEER_VERIFYMEMBER_TIMEOUT_NAME);
    assertThat(securityProps.getProperty(SECURITY_CLIENT_ACCESSOR_NAME)).isEqualTo(SECURITY_CLIENT_ACCESSOR_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_CLIENT_ACCESSOR_PP_NAME)).isEqualTo(DEFAULT_SECURITY_CLIENT_ACCESSOR_PP);
    assertThat(securityProps.getProperty(SECURITY_CLIENT_AUTH_INIT_NAME)).isEqualTo(SECURITY_CLIENT_AUTH_INIT_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_CLIENT_AUTHENTICATOR_NAME)).isEqualTo(SECURITY_CLIENT_AUTHENTICATOR_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_LOG_FILE_NAME)).isEqualTo(SECURITY_LOG_FILE_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_LOG_LEVEL_NAME)).isEqualTo(logLevelCode);
    assertThat(securityProps.getProperty(SECURITY_PEER_AUTH_INIT_NAME)).isEqualTo(SECURITY_PEER_AUTH_INIT_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_PEER_AUTHENTICATOR_NAME)).isEqualTo(SECURITY_PEER_AUTHENTICATOR_NAME_VALUE);
    assertThat(securityProps.getProperty(SECURITY_PEER_VERIFYMEMBER_TIMEOUT_NAME)).isEqualTo(String.valueOf(DEFAULT_SECURITY_PEER_VERIFYMEMBER_TIMEOUT));
  }

  private Properties getPeerExtraProperties() {
    Properties p = new Properties();
    // TODO: add hydra style peer extra props
    //p.setProperty(convertSecurityPrm(key), value);
    return p;
  }

  private Properties getClientExtraProperties() {
    Properties p = new Properties();
    // TODO: add hydra style client extra props
    //p.setProperty(convertSecurityPrm(key), value);
    return p;
  }

  private String convertSecurityPrm(String prmName) {
    return DistributionConfig.SECURITY_PREFIX_NAME + convertPrm(prmName);
  }

  private String convertPrm(String prmName) {
    prmName = prmName.substring(prmName.indexOf("-") + 1, prmName.length());
    StringBuffer buf = new StringBuffer();
    char[] chars = prmName.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (Character.isUpperCase(chars[i])) {
        if (i != 0) {
          buf.append("-");
        }
        buf.append(Character.toLowerCase(chars[i]));
      } else {
        buf.append(chars[i]);
      }
    }
    return buf.toString();
  }

  private Properties addProperties(Properties src, Properties dst) {
    assertThat(dst).isNotNull();
    if (src == null) {
      return dst;
    } else {
      for (Iterator i = src.keySet().iterator(); i.hasNext();) {
        String key = (String)i.next();
        dst.setProperty(key, src.getProperty(key));
      }
    }
    return dst;
  }

  private static final String SECURITY_PEER_AUTH_INIT_NAME_VALUE = "org.apache.geode.security.templates.UserPasswordAuthInit.create";
  private static final String SECURITY_PEER_AUTHENTICATOR_NAME_VALUE = "org.apache.geode.security.templates.DummyAuthenticator.create";
  private static final String SECURITY_CLIENT_AUTH_INIT_NAME_VALUE = "org.apache.geode.security.templates.UserPasswordAuthInit.create";
  private static final String SECURITY_CLIENT_AUTHENTICATOR_NAME_VALUE = "org.apache.geode.security.templates.DummyAuthenticator.create";
  private static final String SECURITY_PEER_VERIFYMEMBER_TIMEOUT_NAME_VALUE = "";
  private static final String SECURITY_LOG_FILE_NAME_VALUE = "/security.log";
  private static final String SECURITY_LOG_LEVEL_NAME_VALUE = GemFireLevel.INFO.getName();
  private static final String SECURITY_CLIENT_ACCESSOR_NAME_VALUE = "org.apache.geode.security.templates.XmlAuthorization.create";
  private static final String SECURITY_CLIENT_ACCESSOR_PP_NAME_VALUE = "";
}
