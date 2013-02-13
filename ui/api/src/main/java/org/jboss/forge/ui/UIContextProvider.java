/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.ui;

/**
 * Provides a {@link UIContext} object
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 *
 */
public interface UIContextProvider
{

   /**
    * Returns the {@link UIContext} that is shared through all the wizard screens
    *
    * @return
    */
   public abstract UIContext getUIContext();

}