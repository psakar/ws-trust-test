package org.jboss.jbossws.jbws5248;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.webservices.deployer.RemoteDeployer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public abstract class JBWS5248IT {

  @ArquillianResource
  protected static Deployer deployer;

  static final String name = "jaxws-samples-wsse-policy-trust"; //jaxws-samples-wsse-policy-trust

  static final String nameSts = "jaxws-samples-wsse-policy-trust-sts"; //jaxws-samples-wsse-policy-trust-sts

  static final String clientName = name + "-client"; //jaxws-samples-wsse-policy-trust-client.jar

  static final String namePicketLinkSts = "jaxws-samples-wsse-policy-trustPicketLink-sts"; //

  static final String serviceURL = "http://" + "localhost" + ":8080/" + name + "/SecurityService";

  static final String serviceStsURL = "http://" + "localhost" + ":8080/" + nameSts + "/SecurityTokenService";

  static final String servicePicketLinkStsURL = "http://" + "localhost" + ":8080/" + namePicketLinkSts
      + "/PicketLinkSTS";

  @Deployment(name = name, managed = false, testable = false)
  static WebArchive createDeployment() throws Exception {

    String resourcePath = "src/test/webapp";
    WebArchive archive = ShrinkWrap
        .create(WebArchive.class, name + ".war")
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF", "wsdl/SecurityService.wsdl"),
            "wsdl/SecurityService.wsdl")
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF", "wsdl/SecurityService_schema1.xsd"),
            "wsdl/SecurityService_schema1.xsd")

        // cxf impl required to extend STS impl
        .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))

        .addAsResource(new File(resourcePath + "/WEB-INF", "servicestore.jks"))
        .addAsResource(new File(resourcePath + "/WEB-INF", "serviceKeystore.properties"))

        .addClass(ServiceIface.class).addClass(ServiceImpl.class).addClass(SayHello.class)
        .addClass(SayHelloResponse.class).addClass(ServerCallbackHandler.class);

    archive.as(ZipExporter.class).exportTo(new File("/tmp", archive.getName()), true);
    return archive;
  }

  /*
    <!-- jaxws-samples-wsse-policy-trust -->
       <classes dir="${tests.output.dir}/test-classes">
          <include name="org/jboss/test/ws/jaxws/samples/wsse/policy/trust/ServiceIface.class"/>
          <include name="org/jboss/test/ws/jaxws/samples/wsse/policy/trust/ServiceImpl.class"/>
          <include name="org/jboss/test/ws/jaxws/samples/wsse/policy/jaxws/Say*.class"/>
          <include name="org/jboss/test/ws/jaxws/samples/wsse/policy/trust/ServerCallbackHandler.class"/>
       </classes>
    */

  @Deployment(name = nameSts, managed = false, testable = false)
  static WebArchive createDeploymentSts() throws Exception {

    String resourcePath = "src/test/webapp";
    WebArchive archive = ShrinkWrap
        .create(WebArchive.class, nameSts + ".war")
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF", "web.xml"))
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF", "jboss-web.xml"))
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF", "wsdl/ws-trust-1.4-service.wsdl"),
            "wsdl/ws-trust-1.4-service.wsdl")

        // cxf impl required to extend STS impl
        .setManifest(
            new StringAsset("Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client, org.apache.cxf.impl annotations\n"))

        .addAsResource(new File(resourcePath + "/WEB-INF", "stsstore.jks"))
        .addAsResource(new File(resourcePath + "/WEB-INF", "stsKeystore.properties"))

        .addClass(SampleSTS.class).addClass(STSCallbackHandler.class);

    archive.as(ZipExporter.class).exportTo(new File("/tmp", archive.getName()), true);
    return archive;
  }

  @Deployment(name = namePicketLinkSts, managed = false, testable = false)
  static WebArchive createPicketLinkSTSDeployment() throws Exception {
    String resourcePath = "src/test/webapp";
    WebArchive archive = ShrinkWrap
        .create(WebArchive.class, namePicketLinkSts + ".war")
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF/", "jboss-web.xml"))
        .addAsWebInfResource(new File(resourcePath + "/WEB-INF/", "wsdl/PicketLinkSTS.wsdl"), "wsdl/PicketLinkSTS.wsdl")

        .setManifest(
            new StringAsset("Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.picketlink\n"))

        .addAsResource(new File(resourcePath + "/WEB-INF", "stsstore.jks"))
        .addAsResource(new File(resourcePath + "/WEB-INF", "stsKeystore.properties"))
        .addAsResource(new File(resourcePath + "/WEB-INF", "picketlink-sts.xml"))

        .addClass(PicketLinkSTService.class).addClass(STSCallbackHandler.class);

    archive.as(ZipExporter.class).exportTo(new File("/tmp", archive.getName()), true);
    return archive;
  }

  /* @Deployment(name = clientName)
   static WebArchive createClientDeployment() {
     //String resourcePath="src/test/client";
     WebArchive archive = ShrinkWrap.create(WebArchive.class, clientName + ".jar")
         .addAsManifestResource("META-INF/clientKeystore.properties")
         .addAsManifestResource("META-INF/clientstore.jks")
         ;
     return archive;
   }
  */

  static final String SECURITY_DOMAIN_NAME = "JBossWS-trust-sts";

  static RemoteDeployer deployerDeprecated = new RemoteDeployer();

  void addSecurityDomain() throws Exception {
    Map<String, String> authenticationOptions = new HashMap<String, String>();
    authenticationOptions.put("usersProperties",
        new File("src/test/webapp/WEB-INF/jbossws-users.properties").getAbsolutePath());
    authenticationOptions.put("rolesProperties",
        new File("src/test/webapp/WEB-INF/jbossws-roles.properties").getAbsolutePath());
    authenticationOptions.put("unauthenticatedIdentity", "anonymous");

    deployerDeprecated.addSecurityDomain(SECURITY_DOMAIN_NAME, authenticationOptions);

    /*
    [{
    "operation" => "composite",
    "address" => [],
    "steps" => [
        {
            "operation" => "add",
            "address" => [
                ("subsystem" => "security"),
                ("security-domain" => "JBossWS-trust-sts")
            ]
        },
        {
            "operation" => "add",
            "address" => [
                ("subsystem" => "security"),
                ("security-domain" => "JBossWS-trust-sts"),
                ("authentication" => "classic")
            ]
        },
        {
            "operation" => "add",
            "address" => [
                ("subsystem" => "security"),
                ("security-domain" => "JBossWS-trust-sts"),
                ("authentication" => "classic"),
                ("login-module" => "UsersRoles")
            ],
            "code" => "UsersRoles",
            "flag" => "required",
            "operation-headers" => {"allow-resource-service-restart" => true},
            "module-options" => [
                ("usersProperties" => "/home/development/jbossqe/JBEAP-6.2.0.ER7/build/stack-cxf/modules/testsuite/cxf-tests/src/test/resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties"),
                ("unauthenticatedIdentity" => "anonymous"),
                ("rolesProperties" => "/home/development/jbossqe/JBEAP-6.2.0.ER7/build/stack-cxf/modules/testsuite/cxf-tests/src/test/resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties")
            ]
        }
    ]
    }]     */

  }

  void removeSecurityDomain() throws Exception {
    deployerDeprecated.removeSecurityDomain(SECURITY_DOMAIN_NAME);
  }

  void setupWsseAndSTSClient(ServiceIface proxy, Bus bus, String stsWsdlLocation, QName stsService, QName stsPort) {
    Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
    ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
    ctx.put(SecurityConstants.SIGNATURE_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(SecurityConstants.ENCRYPT_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
    ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
    STSClient stsClient = new STSClient(bus);
    if (stsWsdlLocation != null) {
      stsClient.setWsdlLocation(stsWsdlLocation);
      stsClient.setServiceQName(stsService);
      stsClient.setEndpointQName(stsPort);
    }
    Map<String, Object> props = stsClient.getProperties();
    props.put(SecurityConstants.USERNAME, "alice");
    props.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
    props.put(SecurityConstants.ENCRYPT_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    props.put(SecurityConstants.ENCRYPT_USERNAME, "mystskey");
    props.put(SecurityConstants.STS_TOKEN_USERNAME, "myclientkey");
    props.put(SecurityConstants.STS_TOKEN_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    props.put(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO, "true");
    ctx.put(SecurityConstants.STS_CLIENT, stsClient);
  }

  void setupWsse(ServiceIface proxy, Bus bus) {
    Map<String, Object> ctx = ((BindingProvider) proxy).getRequestContext();
    ctx.put(SecurityConstants.CALLBACK_HANDLER, new ClientCallbackHandler());
    ctx.put(SecurityConstants.SIGNATURE_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(SecurityConstants.ENCRYPT_PROPERTIES,
        Thread.currentThread().getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(SecurityConstants.SIGNATURE_USERNAME, "myclientkey");
    ctx.put(SecurityConstants.ENCRYPT_USERNAME, "myservicekey");
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.USERNAME), "alice");
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.CALLBACK_HANDLER), new ClientCallbackHandler());
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_PROPERTIES), Thread.currentThread()
        .getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.ENCRYPT_USERNAME), "mystskey");
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USERNAME), "myclientkey");
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_PROPERTIES), Thread.currentThread()
        .getContextClassLoader().getResource("META-INF/clientKeystore.properties"));
    ctx.put(appendIssuedTokenSuffix(SecurityConstants.STS_TOKEN_USE_CERT_FOR_KEYINFO), "true");
  }

  private String appendIssuedTokenSuffix(String prop) {
    return prop + ".it";
  }
}
