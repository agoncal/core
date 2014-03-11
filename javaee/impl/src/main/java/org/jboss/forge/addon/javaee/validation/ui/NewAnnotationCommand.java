/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.javaee.validation.ui;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.parser.java.JavaAnnotation;
import org.jboss.forge.parser.java.JavaSource;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.validation.Constraint;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Creates a new Bean Validation constraint annotation
 *
 * @author <a href="antonio.goncalves@gmail.com">Antonio Goncalves</a>
 */
public class NewAnnotationCommand extends AbstractJavaSourceCommand
{
   @Override
   public Metadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
            .name("Constraint: New Annotation")
            .description("Create a Bean Validation constraint annotation")
            .category(Categories.create(super.getMetadata(context).getCategory(), "Bean Validation"));
   }

   @Override
  protected String getType() {
    return "Bean Validation Constraint Annotations";
  }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      Result result = super.execute(context);
      if (!(result instanceof Failed))
      {
         JavaSourceFacet javaSourceFacet = getSelectedProject(context).getFacet(JavaSourceFacet.class);
         JavaResource javaResource = context.getUIContext().getSelection();
         JavaAnnotation constraint = (JavaAnnotation) javaResource.getJavaSource();
         // Constraint annotation header
         constraint.addAnnotation(Constraint.class).setStringValue("validatedBy = {}");
         constraint.addAnnotation(ReportAsSingleViolation.class);
         constraint.addAnnotation(Retention.class).setEnumValue(RUNTIME);
         constraint.addAnnotation(Target.class).setEnumValue(METHOD, FIELD, PARAMETER, TYPE, ANNOTATION_TYPE, CONSTRUCTOR);
         constraint.addAnnotation(Documented.class);
         // Constraint annotation body
         constraint.addAnnotationElement("String message() default \"Invalid value\"");
         constraint.addAnnotationElement("Class<?>[] groups() default { }");
         constraint.addAnnotationElement("Class<? extends Payload>[] payload() default { }");

         javaSourceFacet.saveJavaSource(constraint);
      }
      return result;
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected Class<? extends JavaSource<?>> getSourceType()
   {
      return JavaAnnotation.class;
   }

  @Override
  protected String calculateDefaultPackage(UIContext context)
  {
    return getSelectedProject(context).getFacet(MetadataFacet.class).getTopLevelPackage() + ".constraints";
  }
}
