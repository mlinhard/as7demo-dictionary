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

import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.ModelRemoveOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.RuntimeOperationContext;
import org.jboss.as.controller.RuntimeTask;
import org.jboss.as.controller.RuntimeTaskContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.as.controller.registry.OperationEntry.EntryType;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Dictionary extension.
 *
 * @author Michal Linhard
 */
public class DictionaryExtension implements Extension, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {
   private static final Logger log = Logger.getLogger(DictionaryExtension.class.getPackage().getName());
   public static final String SUBSYSTEM_NAME = "dict";
   public static final String NAMESPACE = "urn:jboss:domain:dict-demo:1.0";
   public static final String ENTRY = "entry";
   public static final String KEY = "key";
   public static final String VALUE = "value";

   private static final PathElement entryPath = PathElement.pathElement(ENTRY);

   private SubsystemAdd subsystemAdd = new SubsystemAdd();
   private SubsystemDescribe subsystemDescribe = new SubsystemDescribe();
   private EntryAdd entryAdd = new EntryAdd();
   private EntryRemove entryRemove = new EntryRemove();

   @Override
   public void initialize(ExtensionContext context) {
      log.debug("Activating Dictionary Demo Extension");
      SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
      subsystem.registerXMLElementWriter(this);

      // register subsystem model
      ModelNodeRegistration subsystemReg = subsystem.registerSubsystemModel(DictionaryDescriptions.SUBSYSTEM_DESCRIPTION_PROVIDER);
      subsystemReg.registerOperationHandler(ModelDescriptionConstants.ADD, subsystemAdd, subsystemAdd, false);
      subsystemReg.registerOperationHandler(ModelDescriptionConstants.DESCRIBE, subsystemDescribe, subsystemDescribe, false, EntryType.PRIVATE);

      ModelNodeRegistration containers = subsystemReg.registerSubModel(entryPath, DictionaryDescriptions.ENTRY_DESCRIPTION_PROVIDER);
      containers.registerOperationHandler(ModelDescriptionConstants.ADD, entryAdd, entryAdd, false);
      containers.registerOperationHandler(ModelDescriptionConstants.REMOVE, entryRemove, entryRemove, false);
   }

   @Override
   public void initializeParsers(ExtensionParsingContext context) {
      context.setSubsystemXmlMapping(NAMESPACE, this);
   }

   public static class SubsystemAdd implements ModelAddOperationHandler, DescriptionProvider {

      @Override
      public ModelNode getModelDescription(Locale locale) {
         return DictionaryDescriptions.getDictionarySubsystemAddDescription(locale);
      }

      @Override
      public OperationResult execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) throws OperationFailedException {
         log.info("Activating Dictionary demo subsystem.");

         populate(operation, context.getSubModel());

         RuntimeOperationContext runtime = context.getRuntimeContext();
         if (runtime != null) {
             RuntimeTask task = new RuntimeTask() {
                 @Override
                 public void execute(RuntimeTaskContext context) throws OperationFailedException {
                    DictionaryService.addService(context.getServiceTarget());
                 }
             };

             runtime.setRuntimeTask(task);
         } else {
         }
         BasicOperationResult operationResult = new BasicOperationResult(Util.getResourceRemoveOperation(operation.require(ModelDescriptionConstants.OP_ADDR)));
         resultHandler.handleResultComplete();
         return operationResult;
      }

      static ModelNode createOperation(ModelNode address, ModelNode existing) {
         ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
         populate(existing, operation);
         return operation;
     }

     private static void populate(ModelNode source, ModelNode target) {
         target.get(ENTRY).setEmptyObject();
     }

   }

   public static class SubsystemDescribe implements ModelQueryOperationHandler, DescriptionProvider {

      @Override
      public ModelNode getModelDescription(Locale locale) {
         return DictionaryDescriptions.getDictionarySubsystemDescribeDescription(locale);
      }

      /**
       * {@inheritDoc}
       *
       * @see org.jboss.as.controller.ModelQueryOperationHandler#execute(org.jboss.as.controller.OperationContext,
       *      org.jboss.dmr.ModelNode, org.jboss.as.controller.ResultHandler)
       */
      @Override
      public OperationResult execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) throws OperationFailedException {
         ModelNode result = new ModelNode();
         PathAddress rootAddress = PathAddress.pathAddress(PathAddress.pathAddress(operation.require(ModelDescriptionConstants.OP_ADDR)).getLastElement());
         ModelNode subModel = context.getSubModel();

         result.add(SubsystemAdd.createOperation(rootAddress.toModelNode(), subModel));

         if (subModel.hasDefined(ENTRY)) {
            for (Property entry : subModel.get(ENTRY).asPropertyList()) {
               ModelNode address = rootAddress.toModelNode();
               address.add(ENTRY, entry.getName());
               result.add(EntryAdd.createOperation(address, entry.getValue()));
            }
         }

         resultHandler.handleResultFragment(Util.NO_LOCATION, result);
         resultHandler.handleResultComplete();
         return new BasicOperationResult();
      }

   }

   public static class EntryAdd implements ModelAddOperationHandler, DescriptionProvider {

      @Override
      public ModelNode getModelDescription(Locale locale) {
         return DictionaryDescriptions.getDictionaryEntryAddDescription(locale);
      }

      @Override
      public OperationResult execute(OperationContext context, final ModelNode operation, ResultHandler resultHandler) throws OperationFailedException {
         ModelNode opAddr = operation.require(ModelDescriptionConstants.OP_ADDR);
         final String key = PathAddress.pathAddress(opAddr).getLastElement().getValue();
         ModelNode removeOperation = Util.getResourceRemoveOperation(opAddr);

         populate(operation, context.getSubModel());

         RuntimeOperationContext runtime = context.getRuntimeContext();
         if (runtime != null) {
             RuntimeTask task = new RuntimeTask() {
                 @Override
                 public void execute(RuntimeTaskContext context) throws OperationFailedException {
                    ServiceController<?> serviceController = context.getServiceRegistry().getService(DictionaryService.getServiceName());
                    DictionaryService service = (DictionaryService) serviceController.getValue();
                    service.add(key, operation.get(VALUE).asString());
                 }
             };
             runtime.setRuntimeTask(task);
         } else {
             resultHandler.handleResultComplete();
         }

         return new BasicOperationResult(removeOperation);
      }

      static ModelNode createOperation(ModelNode address, ModelNode existing) {
         ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
         populate(existing, operation);
         return operation;
      }

      private static void populate(ModelNode source, ModelNode target) {
         target.get(VALUE).set(source.require(VALUE));
      }

   }

   public static class EntryRemove implements ModelRemoveOperationHandler, DescriptionProvider {

      @Override
      public ModelNode getModelDescription(Locale locale) {
         return DictionaryDescriptions.getDictionaryEntryRemoveDescription(locale);
      }

      @Override
      public OperationResult execute(OperationContext opContext, ModelNode operation, ResultHandler resultHandler) throws OperationFailedException {
         ModelNode opAddr = operation.require(ModelDescriptionConstants.OP_ADDR);
         final String key = PathAddress.pathAddress(opAddr).getLastElement().getValue();
         ModelNode restoreOperation = EntryAdd.createOperation(opAddr, opContext.getSubModel());

         RuntimeOperationContext runtime = opContext.getRuntimeContext();
         if (runtime != null) {
             RuntimeTask task = new RuntimeTask() {
                 @Override
                 public void execute(RuntimeTaskContext context) throws OperationFailedException {
                    ServiceController<?> serviceController = context.getServiceRegistry().getService(DictionaryService.getServiceName());
                    DictionaryService service = (DictionaryService) serviceController.getValue();
                    service.remove(key);
                 }
             };
             runtime.setRuntimeTask(task);
         } else {
             resultHandler.handleResultComplete();
         }

         return new BasicOperationResult(restoreOperation);
      }

   }

   @Override
   public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
      context.startSubsystemElement(NAMESPACE, false);
      ModelNode model = context.getModelNode();
      if (model.isDefined()) {
         for (Property entry : model.get(ENTRY).asPropertyList()) {
            writer.writeStartElement(ENTRY);
            writer.writeAttribute(KEY, entry.getName());
            writer.writeAttribute(VALUE, entry.getValue().get(VALUE).asString());
            writer.writeEndElement();
         }
      }
      writer.writeEndElement();
   }

   @Override
   public void readElement(XMLExtendedStreamReader reader, List<ModelNode> operations) throws XMLStreamException {
      ModelNode address = new ModelNode();
      address.add(ModelDescriptionConstants.SUBSYSTEM, SUBSYSTEM_NAME);
      address.protect();
      ModelNode subsystem = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      operations.add(subsystem);

      ParseUtils.requireNoAttributes(reader);

      while (reader.hasNext()) {
         if (reader.nextTag() == XMLStreamConstants.END_ELEMENT) {
            if (ENTRY.equals(reader.getLocalName())) {
               continue;
            } else {
               break;
            }
         }
         if (!NAMESPACE.equals(reader.getNamespaceURI()) || !ENTRY.equals(reader.getLocalName())) {
            throw ParseUtils.unexpectedElement(reader);
         }
         operations.add(parseEntry(reader, address));
      }
   }

   private ModelNode parseEntry(XMLExtendedStreamReader reader, ModelNode address) throws XMLStreamException {
      ModelNode entry = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
      ParseUtils.requireAttributes(reader, KEY, VALUE);
      entry.get(ModelDescriptionConstants.OP_ADDR).add(ENTRY, reader.getAttributeValue(null, KEY));
      entry.get(VALUE).set(reader.getAttributeValue(null, VALUE));
      return entry;
   }

}
