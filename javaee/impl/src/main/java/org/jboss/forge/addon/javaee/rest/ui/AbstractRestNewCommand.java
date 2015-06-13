package org.jboss.forge.addon.javaee.rest.ui;

import static org.jboss.forge.addon.javaee.JavaEEPackageConstants.DEFAULT_REST_PACKAGE;

import org.jboss.forge.addon.javaee.rest.RestFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.command.PrerequisiteCommandsProvider;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.source.JavaSource;

/**
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public abstract class AbstractRestNewCommand<T extends JavaSource<?>> extends AbstractJavaSourceCommand<T>
         implements PrerequisiteCommandsProvider
{

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
               .category(Categories.create(Categories.create("Java EE"), "JAX-RS"));
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected String calculateDefaultPackage(UIContext context)
   {
      return getSelectedProject(context).getFacet(JavaSourceFacet.class).getBasePackage() + "."
               + DEFAULT_REST_PACKAGE;
   }

   @Override
   public NavigationResult getPrerequisiteCommands(UIContext context)
   {
      NavigationResultBuilder builder = NavigationResultBuilder.create();
      Project project = getSelectedProject(context);
      if (project != null)
      {
         if (!project.hasFacet(RestFacet.class))
         {
            builder.add(RestSetupWizard.class);
         }
      }
      return builder.build();
   }
}
