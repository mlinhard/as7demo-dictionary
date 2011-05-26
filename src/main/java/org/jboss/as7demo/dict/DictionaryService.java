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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * Dictionary Demo service implementation.
 * 
 * @author Michal Linhard
 */
public class DictionaryService implements Service<DictionaryService>, DictionaryServiceMBean {
   private static final ServiceName SERVICE_NAME = ServiceName
            .of(DictionaryExtension.SUBSYSTEM_NAME);

   private static final Logger log = Logger.getLogger(DictionaryExtension.class.getPackage()
            .getName());
   private Map<String, String> dictionary = new ConcurrentHashMap<String, String>();

   public static ServiceName getServiceName() {
      return SERVICE_NAME;
   }

   public static void addService(ServiceTarget serviceTarget) {
      DictionaryService service = new DictionaryService();
      serviceTarget.addService(SERVICE_NAME, service).install();
   }

   @Override
   public DictionaryService getValue() throws IllegalStateException, IllegalArgumentException {
      return this;
   }

   @Override
   public void start(StartContext context) throws StartException {
      try {
         ObjectName objectName = new ObjectName("dictionary.demo", "name", "dictionary");
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         mBeanServer.registerMBean(this, objectName);
         log.debug("Dictionary service started");
      } catch (Exception e) {
         log.error("Couldn't register DictionaryServiceMBean", e);
      }
   }

   @Override
   public void stop(StopContext context) {
      log.debug("Dictionary service stopped");
   }

   public void add(String key, String value) {
      dictionary.put(key, value);
   }

   public void remove(String key) {
      dictionary.remove(key);
   }

   public String find(String key) {
      return dictionary.get(key);
   }

   public synchronized String list() {
      StringBuffer sb = new StringBuffer();
      List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(dictionary
               .entrySet());
      Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
         @Override
         public int compare(Entry<String, String> o1, Entry<String, String> o2) {
            return o1.getKey().compareTo(o2.getKey());
         }
      });
      for (Map.Entry<String, String> entry : list) {
         sb.append(entry.getKey());
         sb.append(": ");
         sb.append(entry.getValue());
         sb.append("\n");
      }
      return sb.toString();
   }

   public int size() {
      return dictionary.size();
   }

}
