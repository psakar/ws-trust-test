/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jbossws.jbws5248;

import static org.junit.Assert.*;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * WS-Trust test case
 * This is basically the Apache CXF STS demo (from distribution samples)
 * ported to jbossws-cxf for running over JBoss Application Server.
 *
 * Requires installed unlimited cryptography policy !
 */
public class WSTrustTestCase extends JBWS5248IT {

  @Before
  public void before() throws Exception {
    addSecurityDomain();
    deployer.deploy(nameSts);
    deployer.deploy(name);
  }

  @After
  public void after() throws Exception {
    try {
      deployer.undeploy(name);
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    try {
      deployer.undeploy(nameSts);
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    try {
      removeSecurityDomain();
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  @Test
  public void testSTSinformationProgrammaticallyProvided() throws Exception {
    Bus bus = BusFactory.newInstance().createBus();
    try {
      BusFactory.setThreadDefaultBus(bus);

      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = service.getPort(ServiceIface.class);

      QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
      QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
      setupWsseAndSTSClient(proxy, bus, serviceStsURL + "?wsdl", stsServiceName, stsPortName);

      assertEquals("WS-Trust Hello World!", proxy.sayHello());
    } finally {
      bus.shutdown(true);
    }
  }

  /**
   * WS-Trust test with the STS information coming from EPR specified in service endpoint contract policy
   */
  @Test
  public void testUsingEPR() throws Exception {
    Bus bus = BusFactory.newInstance().createBus();
    try {
      BusFactory.setThreadDefaultBus(bus);

      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = service.getPort(ServiceIface.class);

      setupWsse(proxy, bus);

      assertEquals("WS-Trust Hello World!", proxy.sayHello());
    } finally {
      bus.shutdown(true);
    }
  }
}
