/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.maven;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.projects.MavenBuildSystem;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MavenMultiModuleProviderTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).addBeansXML();
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private MavenBuildSystem buildSystem;

   @Test
   public void testInjectionNotNull()
   {
      Assert.assertNotNull(projectFactory);
   }

   @Test
   public void testCreateNestedProject() throws Exception
   {
      Project parentProject = projectFactory.createTempProject(buildSystem);
      Assert.assertNotNull(parentProject);

      parentProject.getFacet(PackagingFacet.class).setPackagingType("pom");

      MetadataFacet metadata = parentProject.getFacet(MetadataFacet.class);
      metadata.setProjectName("parent");
      metadata.setProjectGroupName("com.project.parent");

      DirectoryResource subProjectDir = parentProject.getRoot().reify(DirectoryResource.class).getChildDirectory("sub");
      projectFactory.createProject(subProjectDir, buildSystem);

      MavenFacet mavenFacet = parentProject.getFacet(MavenFacet.class);
      List<String> modules = mavenFacet.getModel().getModules();
      Assert.assertFalse(modules.isEmpty());
      Assert.assertEquals("sub", modules.get(0));
   }

   @Test
   public void testCreateNestedProjectWithParentThatHasInheritedVersion() throws Exception
   {
      Project parentProject = projectFactory.createTempProject(buildSystem);
      Assert.assertNotNull(parentProject);

      MetadataFacet metadata = parentProject.getFacet(MetadataFacet.class);
      metadata.setProjectName("parent");
      metadata.setProjectGroupName("com.project.parent");
      parentProject.getFacet(PackagingFacet.class).setPackagingType("pom");

      DirectoryResource intermediateProjectDir = parentProject.getRoot().reify(DirectoryResource.class)
               .getChildDirectory("intermediate");
      Project intermediateProject = projectFactory.createProject(intermediateProjectDir, buildSystem);

      MavenFacet parentMavenFacet = parentProject.getFacet(MavenFacet.class);
      Model parentModel = parentMavenFacet.getModel();
      List<String> modules = parentModel.getModules();
      Assert.assertFalse(modules.isEmpty());
      Assert.assertEquals("intermediate", modules.get(0));

      intermediateProject.getFacet(MetadataFacet.class).setProjectVersion("");
      intermediateProject.getFacet(PackagingFacet.class).setPackagingType("pom");

      DirectoryResource subProjectDir = intermediateProject.getRoot().reify(DirectoryResource.class)
               .getChildDirectory("sub");
      Project subProject = projectFactory.createProject(subProjectDir, buildSystem);

      MavenFacet intermediateMavenFacet = intermediateProject.getFacet(MavenFacet.class);
      Model intermediateModel = intermediateMavenFacet.getModel();
      Assert.assertEquals(parentModel.getGroupId(), intermediateModel.getParent().getGroupId());
      List<String> intermediateModules = intermediateModel.getModules();
      Assert.assertFalse(intermediateModules.isEmpty());
      Assert.assertEquals("sub", intermediateModules.get(0));

      String version = subProject.getFacet(MetadataFacet.class).getProjectVersion();
      Assert.assertEquals(parentProject.getFacet(MetadataFacet.class).getProjectVersion(), version);
      Model subModel = subProject.getFacet(MavenFacet.class).getModel();
      Assert.assertEquals(parentModel.getGroupId(), subModel.getParent().getGroupId());

   }
}
