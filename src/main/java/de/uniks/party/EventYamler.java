package de.uniks.party;

import org.fulib.yaml.Reflector;
import org.fulib.yaml.ReflectorMap;
import org.fulib.yaml.YamlIdMap;
import org.fulib.yaml.Yamler;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventYamler
{
   public static final String TIME = "time";
   public static final String SOURCE = "source";
   public static final String SOURCE_TYPE = "sourceType";
   public static final String PROPERTY = "property";
   public static final String OLD_VALUE = "oldValue";
   public static final String OLD_VALUE_TYPE = OLD_VALUE + "Type";
   public static final String NEW_VALUE = "newValue";
   public static final String NEW_VALUE_TYPE = NEW_VALUE + "Type";
   public static final String HISTORY_KEY = "historyKey";


   private ReflectorMap reflectorMap;
   private YamlIdMap yamlIdMap;
   private String packageName;

   public EventYamler(String packageName)
   {
      this.packageName = packageName;
      this.reflectorMap = new ReflectorMap(packageName);
      this.yamlIdMap = new YamlIdMap(packageName);
   }


   public EventYamler setYamlIdMap(YamlIdMap yamlIdMap)
   {
      this.yamlIdMap = yamlIdMap;

      return this;
   }


   public String encode(PropertyChangeEvent e)
   {
      Object source = e.getSource();
      StringBuilder buf = new StringBuilder("- ");

      long timeMillis = System.currentTimeMillis();
      Date date = new Date(timeMillis);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
      String timeString = dateFormat.format(date);
      buf.append(TIME + ": ").append(timeString).append("\n");

      String sourceKey = yamlIdMap.getOrCreateKey(source);
      buf.append("  " + SOURCE + ": ").append(sourceKey).append("\n");

      String className = source.getClass().getSimpleName();
      buf.append("  " + SOURCE_TYPE + ": ").append(className).append("\n");

      String prop = e.getPropertyName();
      buf.append("  " + PROPERTY + ": ").append(prop).append("\n");

      String historyKey = sourceKey + "/" + prop;


      Object oldValue = e.getOldValue();
      if (oldValue != null)
      {
         Class valueClass = oldValue.getClass();

         if (valueClass == String.class)
         {
            String encapsulted = Yamler.encapsulate((String) oldValue);
            buf.append("  " + OLD_VALUE + ": ").append(encapsulted).append("\n");
         }
         else if (  valueClass.getName().startsWith("java.lang."))
         {
            buf.append("  " + OLD_VALUE + ": ").append(oldValue).append("\n");
         }
         else
         {
            String valueKey = yamlIdMap.getOrCreateKey(oldValue);
            buf.append("  " + OLD_VALUE + ": ").append(valueKey).append("\n");

            historyKey += "/" + valueKey;

            className = oldValue.getClass().getSimpleName();
            buf.append("  " + OLD_VALUE_TYPE + ": ").append(className).append("\n");
         }
      }

      Object newValue = e.getNewValue();
      if (newValue != null)
      {
         Class valueClass = newValue.getClass();

         if (valueClass == String.class)
         {
            String encapsulted = Yamler.encapsulate((String) newValue);
            buf.append("  " + NEW_VALUE + ": ").append(encapsulted).append("\n");
         }
         else if (  valueClass.getName().startsWith("java.lang."))
         {
            buf.append("  " + NEW_VALUE + ": ").append(newValue).append("\n");
         }
         else
         {
            String valueKey = yamlIdMap.getOrCreateKey(newValue);
            buf.append("  " + NEW_VALUE + ": ").append(valueKey).append("\n");

            Reflector reflector = reflectorMap.getReflector(className);
            Object attrValue = reflector.getValue(source, prop);
            if (attrValue != null && Collection.class.isAssignableFrom(attrValue.getClass()))
            {
               historyKey += "/" + valueKey;
            }

            className = newValue.getClass().getSimpleName();
            buf.append("  " + NEW_VALUE_TYPE + ": ").append(className).append("\n");
         }
      }

      buf.append("  " + HISTORY_KEY + ": ").append(historyKey).append("\n");
      buf.append("\n");
      return buf.toString();
   }


   public Object decode(Object rootObject, String content)
   {
      Yamler yamler = new Yamler();
      ArrayList<LinkedHashMap<String, String>> list = yamler.decodeList(content);

      String firstKey = null;
      for (LinkedHashMap<String, String> map : list)
      {
         // execute change
         String sourceKey = map.get(DataManager.SOURCE);

         if (firstKey == null)
         {
            firstKey = sourceKey;
            Object oldObject = yamlIdMap.getObject(firstKey);
            if (oldObject == null)
            {
               yamlIdMap.putNameObject(firstKey, rootObject);
            }
         }

         Object sourceObject = yamlIdMap.getObject(sourceKey);
         String className = map.get(DataManager.SOURCE_TYPE);
         Reflector reflector = reflectorMap.getReflector(className);

         if (reflector == null)
         {
            Logger.getGlobal().log(Level.SEVERE, "did not find a reflector for " + className);
         }

         if (sourceObject == null)
         {
            sourceObject = reflector.newInstance();
            yamlIdMap.putNameObject(sourceKey, sourceObject);
         }

         String property = map.get(DataManager.PROPERTY);
         String newValue = map.get(DataManager.NEW_VALUE);
         String newValueType = map.get(DataManager.NEW_VALUE_TYPE);

         if (newValueType == null)
         {
            reflector.setValue(sourceObject, property, newValue, null);
         }
         else
         {
            Object newValueObject = yamlIdMap.getObject(newValue);
            if (newValueObject == null)
            {
               Reflector newValueReflector = reflectorMap.getReflector(newValueType);
               newValueObject = newValueReflector.newInstance();
               yamlIdMap.putNameObject(newValue, newValueObject);
            }

            reflector.setValue(sourceObject, property, newValueObject, null);
         }
      }

      Object firstObject = yamlIdMap.getObject(firstKey);

      return firstObject;
   }
}
