/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.addons.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.jboss.forge.addon.addons.facets.AddonClassifierFacet;
import org.jboss.forge.addon.addons.project.AddonProjectConfiguratorImpl;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.forge.furnace.util.Strings;

/**
 * Adds an addon dependency to the current project
 *
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@FacetConstraint(AddonClassifierFacet.class)
public class AddAddonDependencyCommandImpl extends AbstractProjectCommand implements AddAddonDependencyCommand
{
   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private AddonProjectConfiguratorImpl configurator;

   @Inject
   private Furnace furnace;

   @Inject
   @WithAttributes(label = "Addon Coordinates", description = "Addon coordinates to be added as a dependency for the selected project", required = true)
   private UIInput<String> addon;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      final Set<AddonId> addonChoices = new TreeSet<>();
      Project project = getSelectedProject(builder);
      for (AddonRepository repository : furnace.getRepositories())
      {
         for (AddonId id : repository.listEnabled())
         {
            // TODO: Furnace should provide some way to detect if an addon is a Container type
            boolean isContainerAddon = id.getName().contains("org.jboss.forge.furnace.container");
            if (!isContainerAddon && !configurator.dependsOnAddon(project, id))
            {
               addonChoices.add(id);
            }
         }
      }
      addon.setCompleter(new UICompleter<String>()
      {
         @Override
         public Iterable<String> getCompletionProposals(UIContext context, InputComponent<?, String> input,
                  String value)
         {
            List<String> addons = new ArrayList<>();
            for (AddonId addonId : addonChoices)
            {
               if (Strings.isNullOrEmpty(value) || addonId.toCoordinates().startsWith(value))
               {
                  addons.add(addonId.toCoordinates());
               }
            }
            return addons;
         }
      }).addValidator(new UIValidator()
      {
         @Override
         public void validate(UIValidationContext context)
         {
            String value = (String) context.getCurrentInputComponent().getValue();
            try
            {
               AddonId.fromCoordinates(value);
            }
            catch (Exception e)
            {
               context.addValidationError(addon, e.getMessage());
            }
         }
      });
      builder.add(addon);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Project project = getSelectedProject(context);
      AddonId addonId = AddonId.fromCoordinates(addon.getValue());
      configurator.installSelectedAddons(project, Collections.singleton(addonId), false);
      return Results.success("Addon " + addonId + " added as a dependency to project "
               + project.getFacet(MetadataFacet.class).getProjectName());
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(NewUICommandWizardImpl.class)
               .name("Addon: Add Dependency")
               .description("Adds the provided addon as a dependency to the selected project")
               .category(Categories.create("Forge", "Setup"));
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }

}
