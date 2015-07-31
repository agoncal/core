/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.maven.archetype;

import java.util.Iterator;

import javax.inject.Inject;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class ArchetypeRegistryTest
{

   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.addon:projects"),
            @AddonDeployment(name = "org.jboss.forge.addon:maven")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addClass(TestArchetypeCatalogFactory.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects")
               );

      return archive;
   }

   @Inject
   private ArchetypeCatalogFactoryRegistry archetypeRegistry;

   @Test
   public void testArchetypeCatalogFactory()
   {
      ArchetypeCatalogFactory archetypeCatalogFactory = archetypeRegistry
               .getArchetypeCatalogFactory("Test");
      Assert.assertNotNull(archetypeCatalogFactory);
      ArchetypeCatalog archetypes = archetypeCatalogFactory.getArchetypeCatalog();
      Assert.assertNotNull(archetypes);
      Assert.assertNotNull(archetypes.getArchetypes());
      Assert.assertEquals(1, archetypes.getArchetypes().size());
      Archetype expected = new Archetype();
      expected.setGroupId("groupId");
      expected.setArtifactId("artifactId");
      expected.setVersion("1.0.0");
      expected.setDescription("Description");
      Assert.assertEquals(expected, archetypes.getArchetypes().get(0));
   }

   @Test
   public void testHasArchetypeCatalogFactory()
   {
      Assert.assertTrue(archetypeRegistry.hasArchetypeCatalogFactories());
   }

   @Test
   public void testDuplicateArchetypeCatalogs()
   {
      TestArchetypeCatalogFactory factory = new TestArchetypeCatalogFactory();
      archetypeRegistry.addArchetypeCatalogFactory(factory);
      Iterator<ArchetypeCatalogFactory> archetypeCatalogFactories = archetypeRegistry.getArchetypeCatalogFactories()
               .iterator();
      Assert.assertTrue(archetypeCatalogFactories.hasNext());
      Assert.assertSame(factory, archetypeCatalogFactories.next());
      Assert.assertFalse(archetypeCatalogFactories.hasNext());
   }
}
