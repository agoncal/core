/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 * <p/>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.resource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.io.Files;

/**
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class FileResourceTest
{
   @Deployment
   @AddonDependencies
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).addBeansXML();
   }

   @Inject
   private ResourceFactory resourceFactory;

   @Test
   public void testAppendableOutputStream() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      FileResource<?> fileResource = resourceFactory.create(file).reify(FileResource.class);
      Assert.assertNotNull(fileResource);
      Assert.assertNull(fileResource.reify(DirectoryResource.class));
      try (OutputStream os = fileResource.getResourceOutputStream(false))
      {
         os.write("HELLO".getBytes());
         os.flush();
      }
      Assert.assertEquals("HELLO", fileResource.getContents());
      try (OutputStream os = fileResource.getResourceOutputStream(true))
      {
         os.write(" WORLD".getBytes());
         os.flush();
      }
      Assert.assertEquals("HELLO WORLD", fileResource.getContents());
      try (OutputStream os = fileResource.getResourceOutputStream())
      {
         os.write("GOODBYE".getBytes());
         os.flush();
      }
      Assert.assertEquals("GOODBYE", fileResource.getContents());
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDirectoryResourceReifyShouldRetunNullForFiles() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      FileResource<?> fileResource = resourceFactory.create(FileResource.class, file);
      Assert.assertNotNull(fileResource);
      Assert.assertNull(fileResource.reify(DirectoryResource.class));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testResourceOutputStream() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      FileResource<?> fileResource = resourceFactory.create(FileResource.class, file);
      try (OutputStream os = fileResource.getResourceOutputStream())
      {
         os.write("CONTENT".getBytes());
         os.flush();
      }
      String fileContent = Files.readFirstLine(file, Charset.defaultCharset());
      Assert.assertEquals("CONTENT", fileContent);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testFileResourceGetChildReturnsNull() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      FileResource<?> fileResource = resourceFactory.create(FileResource.class, file);
      Assert.assertNull(fileResource.getChild("foo"));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testDirectoryResourceResolveShouldBeTheSameForChild() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      FileResource<?> fileResource = resourceFactory.create(FileResource.class, file);
      FileResource<?> parentResource = resourceFactory.create(FileResource.class, file.getParentFile());
      Assert.assertNotNull(fileResource);
      Assert.assertNotNull(parentResource);
      List<Resource<?>> children = parentResource.resolveChildren(file.getName());
      Assert.assertEquals(1, children.size());
      Assert.assertEquals(fileResource, children.get(0));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testMoveFileResource() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();

      File dest = File.createTempFile("newFileResourcetest", ".tmp");
      dest.deleteOnExit();

      FileResource<?> src = resourceFactory.create(FileResource.class, file);
      FileResource<?> newFile = resourceFactory.create(FileResource.class, dest);

      src.moveTo(newFile);
      Assert.assertNotNull(dest);
      Assert.assertTrue(dest.isFile());
      Assert.assertEquals(src.getUnderlyingResourceObject().getAbsolutePath(), dest.getAbsolutePath());
      Assert.assertTrue(src.exists());
   }

   @Test
   public void testMoveFileResourceToDirectory() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      file.createNewFile();

      File folder = OperatingSystemUtils.createTempDir();
      folder.deleteOnExit();
      folder.mkdir();

      FileResource<?> src = resourceFactory.create(file).reify(FileResource.class);
      DirectoryResource folderResource = resourceFactory.create(DirectoryResource.class, folder);
      src.moveTo(folderResource);

      Assert.assertNotNull(src);
      Assert.assertTrue(src.getUnderlyingResourceObject().getAbsolutePath().contains(folder.getName()));
      Assert.assertTrue(src.exists());
      Assert.assertFalse(src.isDirectory());
   }

   @Test
   public void testMoveDirectoryResourceToDirectory() throws IOException
   {
      File folder = OperatingSystemUtils.createTempDir();
      folder.deleteOnExit();
      folder.mkdir();

      File folder2 = OperatingSystemUtils.createTempDir();
      folder2.deleteOnExit();
      folder2.mkdir();

      DirectoryResource folderResource = resourceFactory.create(DirectoryResource.class, folder);
      DirectoryResource folderResource2 = resourceFactory.create(DirectoryResource.class, folder2);
      folderResource.moveTo(folderResource2);

      String absolutePathDirMoved = folderResource.getUnderlyingResourceObject().getAbsolutePath();
      Assert.assertEquals(absolutePathDirMoved, folder2.getAbsolutePath() + File.separator + folder.getName());
      Assert.assertTrue(folderResource.isDirectory());
      Assert.assertTrue(folderResource.exists());
   }

   @Test(expected = ResourceException.class)
   public void testMoveDirectoryResourceToFile() throws IOException
   {
      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      file.createNewFile();

      File folder = OperatingSystemUtils.createTempDir();
      folder.deleteOnExit();
      folder.mkdir();

      DirectoryResource folderResource = resourceFactory.create(DirectoryResource.class, folder);
      FileResource fileResource = resourceFactory.create(FileResource.class, file);
      folderResource.moveTo(fileResource);
   }

   @Test
   public void testDeleteDirectoryNotRecursive() throws IOException
   {
      File folder = OperatingSystemUtils.createTempDir();
      folder.deleteOnExit();
      folder.mkdir();

      DirectoryResource folderResource = resourceFactory.create(DirectoryResource.class, folder);

      Assert.assertTrue(folderResource.isDirectory());
      Assert.assertTrue(folderResource.exists());

      folderResource.delete();

      Assert.assertTrue(!folderResource.exists());
   }

   @Test
   public void testDeleteDirectoryRecursive() throws IOException
   {
      File folder = OperatingSystemUtils.createTempDir();
      folder.deleteOnExit();
      folder.mkdir();

      File folder2 = OperatingSystemUtils.createTempDir();
      folder2.deleteOnExit();
      folder2.mkdir();

      File file = File.createTempFile("fileresourcetest", ".tmp");
      file.deleteOnExit();
      file.createNewFile();

      File file2 = File.createTempFile("fileresourcetest2", ".tmp");
      file2.deleteOnExit();
      file2.createNewFile();

      DirectoryResource folderResource = resourceFactory.create(DirectoryResource.class, folder);
      FileResource<?> src = resourceFactory.create(file).reify(FileResource.class);
      src.moveTo(folderResource);

      DirectoryResource folderResource2 = resourceFactory.create(DirectoryResource.class, folder2);
      FileResource<?> src2 = resourceFactory.create(file2).reify(FileResource.class);

      folderResource2.moveTo(folderResource);
      src2.moveTo(folderResource2);

      Assert.assertTrue(folderResource.isDirectory());
      Assert.assertTrue(folderResource.exists());
      Assert.assertTrue(src.exists());

      Assert.assertTrue(folderResource2.isDirectory());
      Assert.assertTrue(folderResource2.exists());
      Assert.assertTrue(src2.exists());

      folderResource.delete(true);

      Assert.assertTrue(!folderResource.exists());
      Assert.assertTrue(!file.exists());
      Assert.assertTrue(!folderResource2.exists());
      Assert.assertTrue(!file2.exists());
   }
}
