package org.jboss.forge.addon.parser.xml.resources;

/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class XMLResourceTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML();
   }

   @Inject
   private ResourceFactory factory;

   @Test
   public void testXMLResourceCreation() throws Exception
   {
      Node node = XMLParser.parse("<test/>");
      XMLResource resource = factory.create(XMLResource.class, File.createTempFile("forge", ".xml"));
      resource.createNewFile();
      resource.setContents(node);
      Assert.assertEquals("test", resource.getXmlSource().getName());
   }

   @Test
   public void testJavaResourceCreationSpecialized() throws Exception
   {
      Node node = XMLParser.parse("<test/>");
      XMLResource resource = factory.create(XMLResource.class, File.createTempFile("forge", ".xml"));
      resource.createNewFile();
      resource.setContents(node);

      Resource<File> newResource = factory.create(resource.getUnderlyingResourceObject());

      Assert.assertThat(newResource, is(instanceOf(XMLResource.class)));
      Assert.assertEquals(resource, newResource);
   }

   @Test
   public void testXHTMLResourceCreation() throws Exception
   {
      Node node = XMLParser.parse("<test/>");
      XMLResource resource = factory.create(XMLResource.class, File.createTempFile("forge", ".xhtml"));
      Assert.assertNotNull(resource);
      resource.createNewFile();
      resource.setContents(node);
      Assert.assertEquals("test", resource.getXmlSource().getName());
   }

}
