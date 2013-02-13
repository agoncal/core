package org.jboss.forge.ui.impl;

import org.jboss.forge.ui.Result;
import org.jboss.forge.ui.UIBuilder;
import org.jboss.forge.ui.UICommandMetadata;
import org.jboss.forge.ui.UIContext;
import org.jboss.forge.ui.UIValidationContext;
import org.jboss.forge.ui.base.UICommandMetadataBase;
import org.jboss.forge.ui.wizard.UIWizard;
import org.jboss.forge.ui.wizard.UIWizardStep;

public class MockChooseFrameworkStep implements UIWizardStep
{

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

   @Override
   public UICommandMetadata getMetadata()
   {
      return new UICommandMetadataBase("Choose Framework", "Pick the framework you wish to use for this command.");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
   }

   @Override
   public void validate(UIValidationContext context)
   {
   }

   @Override
   public Result execute(UIContext context) throws Exception
   {
      return null;
   }

   @Override
   public Class<? extends UIWizard> getSuccessor()
   {
      return null;
   }



}
