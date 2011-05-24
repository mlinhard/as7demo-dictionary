/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as7demo.dict;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Descriptions for the subsystem.
 *
 * @author Michal Linhard
 */
public class DictionaryDescriptions {

   public static final DescriptionProvider SUBSYSTEM_DESCRIPTION_PROVIDER = new DescriptionProvider() {
      @Override
      public ModelNode getModelDescription(Locale locale) {
         return getDictionarySubsystemDescription(locale);
      }
   };

   public static final DescriptionProvider ENTRY_DESCRIPTION_PROVIDER = new DescriptionProvider() {
      @Override
      public ModelNode getModelDescription(Locale locale) {
         return getDictionaryEntryDescription(locale);
      }
   };

   public static ModelNode getDictionarySubsystemDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      ModelNode description = new ModelNode();
      description.get(ModelDescriptionConstants.DESCRIPTION).set(res.getString("dict"));
      description.get(ModelDescriptionConstants.HEAD_COMMENT_ALLOWED).set(true);
      description.get(ModelDescriptionConstants.TAIL_COMMENT_ALLOWED).set(true);
      description.get(ModelDescriptionConstants.NAMESPACE).set(DictionaryExtension.NAMESPACE);
      return description;
   }

   public static ModelNode getDictionarySubsystemAddDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      return createOperationDescription(ModelDescriptionConstants.ADD, res.getString("dict.add"), null, null);
   }

   public static ModelNode getDictionarySubsystemDescribeDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      return createOperationDescription(ModelDescriptionConstants.DESCRIBE, res.getString("dict.describe"), null, null);
   }

   public static ModelNode getDictionaryEntryDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      ModelNode description = new ModelNode();
      description.get(ModelDescriptionConstants.DESCRIPTION).set(res.getString("dict.entry"));
      return description;
   }

   public static ModelNode getDictionaryEntryAddDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      ModelNode reqProps = new ModelNode();
      ModelNode keyArg = reqProps.get("key").addEmptyObject();
      keyArg.get("type").set(ModelType.STRING);
      keyArg.get("description").set(res.getString("dict.entry.add.key"));
      keyArg.get("required").set(true);
      ModelNode valueArg = reqProps.get("value").addEmptyObject();
      valueArg.get("type").set(ModelType.STRING);
      valueArg.get("description").set(res.getString("dict.entry.add.value"));
      valueArg.get("required").set(true);
      return createOperationDescription(ModelDescriptionConstants.ADD, res.getString("dict.entry.add"), reqProps, null);
   }

   public static ModelNode getDictionaryEntryRemoveDescription(Locale locale) {
      ResourceBundle res = getResources(locale);
      return createOperationDescription(ModelDescriptionConstants.REMOVE, res.getString("dict.entry.remove"), null, null);
   }

   private static ResourceBundle getResources(Locale locale) {
      return ResourceBundle.getBundle(DictionaryDescriptions.class.getName(), (locale == null) ? Locale.getDefault() : locale);
   }

   private static ModelNode createOperationDescription(String operation, String descrString, ModelNode reqProps, ModelNode repProps) {
      ModelNode description = new ModelNode();
      description.get(ModelDescriptionConstants.OPERATION_NAME).set(operation);
      description.get(ModelDescriptionConstants.DESCRIPTION).set(descrString);
      if (reqProps != null) {
         description.get(ModelDescriptionConstants.REQUEST_PROPERTIES).set(reqProps);
      }
      if (repProps != null) {
         description.get(ModelDescriptionConstants.REPLY_PROPERTIES).set(repProps);
      }
      return description;
   }
}
