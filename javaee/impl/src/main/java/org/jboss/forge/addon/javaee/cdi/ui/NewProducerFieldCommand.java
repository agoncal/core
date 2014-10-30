/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.javaee.cdi.ui;

import java.io.FileNotFoundException;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.javaee.cdi.CDIFacet;
import org.jboss.forge.addon.parser.java.beans.FieldOperations;
import org.jboss.forge.addon.parser.java.beans.ProjectOperations;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.ui.JavaFieldCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.command.PrerequisiteCommandsProvider;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIPrompt;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.Field;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

public class NewProducerFieldCommand extends AbstractProjectCommand implements JavaFieldCommand,
         PrerequisiteCommandsProvider
{

   @Inject
   @WithAttributes(label = "Target Class", description = "The class where the producer field will be created", required = true, type = InputType.DROPDOWN)
   private UISelectOne<JavaResource> targetClass;

   @Inject
   @WithAttributes(label = "Field Name", description = "The field name to be created in this class", required = true)
   private UIInput<String> named;

   @Inject
   @WithAttributes(label = "Field Type", description = "The type intended to be used for this field", type = InputType.JAVA_CLASS_PICKER, required = true, defaultValue = "String")
   private UIInput<String> type;

   @Inject
   @WithAttributes(label = "Access Type", description = "The access type", type = InputType.RADIO)
   private UISelectOne<Visibility> accessType;

   @Inject
   @WithAttributes(label = "Alternative", description = "If the producer is @Alternative", defaultValue = "true")
   private UIInput<Boolean> alternative;

   @Inject
   @WithAttributes(label = "Qualifer", description = "The qualifier of the producer", type = InputType.JAVA_CLASS_PICKER)
   private UIInput<String> qualifier;

   @Inject
   @WithAttributes(label = "Scope")
   private UISelectOne<BeanScope> scoped;

   @Inject
   private FieldOperations fieldOperations;

   @Inject
   private ProjectOperations projectOperations;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(getClass()).name("CDI: New Producer Field")
               .description("Creates a new producer field")
               .category(Categories.create("CDI"));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      setupTargetClass(builder.getUIContext());
      setupAccessType();
      builder.add(targetClass).add(named).add(type).add(accessType).add(alternative).add(qualifier).add(scoped);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      JavaResource javaResource = targetClass.getValue();
      String fieldNameStr = named.getValue();
      JavaClassSource targetClass = javaResource.getJavaType();
      FieldSource<JavaClassSource> field = targetClass.getField(fieldNameStr);
      String action = (field == null) ? "created" : "updated";
      if (field != null)
      {
         UIPrompt prompt = context.getPrompt();
         if (prompt.promptBoolean("Field '" + field.getName() + "' already exists. Do you want to overwrite it?"))
         {
            fieldOperations.removeField(targetClass, field);
         }
         else
         {
            return Results.fail("Field '" + field.getName() + "' already exists.");
         }
      }

      field.setName("em").setType(type.getValue());
      if (accessType == null)
      {
         field.setVisibility(Visibility.PRIVATE);
      } else {
         field.setVisibility(accessType.getValue());
      }
      field.addAnnotation(Produces.class);
      if (qualifier != null)
      {
         field.addAnnotation(qualifier.getValue());
      }
      if (alternative.getValue() != null && alternative.getValue())
      {
         field.addAnnotation(Alternative.class);
      }
      if (scoped.getValue() != null)
      {
         field.addAnnotation(scoped.getValue().getAnnotation());
      }

      setCurrentWorkingResource(context, javaResource, field);
      return Results.success("Field " + named.getValue() + " " + action);

   }

   private void setupTargetClass(UIContext context)
   {
      UISelection<FileResource<?>> selection = context.getInitialSelection();
      Project project = getSelectedProject(context);
      final List<JavaResource> entities = projectOperations.getProjectClasses(project);
      targetClass.setValueChoices(entities);
      int idx = -1;
      if (!selection.isEmpty())
      {
         idx = entities.indexOf(selection.get());
      }
      if (idx != -1)
      {
         targetClass.setDefaultValue(entities.get(idx));
      }
   }

   private void setupAccessType()
   {
      accessType.setItemLabelConverter(new Converter<Visibility, String>()
      {
         @Override
         public String convert(Visibility source)
         {
            if (source == null)
               return null;
            if (source == Visibility.PACKAGE_PRIVATE)
            {
               return "default";
            }
            return source.toString();
         }
      });
      accessType.setDefaultValue(Visibility.PRIVATE);
   }

   private void setCurrentWorkingResource(UIExecutionContext context, JavaResource javaResource,
            Field<JavaClassSource> field)
            throws FileNotFoundException
   {
      Project selectedProject = getSelectedProject(context);
      if (selectedProject != null)
      {
         JavaSourceFacet facet = selectedProject.getFacet(JavaSourceFacet.class);
         facet.saveJavaSource(field.getOrigin());
      }
      context.getUIContext().setSelection(javaResource);
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Inject
   private ProjectFactory projectFactory;

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }

   @Override
   public NavigationResult getPrerequisiteCommands(UIContext context)
   {
      NavigationResultBuilder builder = NavigationResultBuilder.create();
      Project project = getSelectedProject(context);
      if (project != null)
      {
         if (!project.hasFacet(CDIFacet.class))
         {
            builder.add(CDISetupCommand.class);
         }
      }
      return builder.build();
   }
}
