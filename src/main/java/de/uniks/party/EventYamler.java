package de.uniks.party;

import org.fulib.yaml.Reflector;
import org.fulib.yaml.ReflectorMap;
import org.fulib.yaml.YamlIdMap;
import org.fulib.yaml.Yamler;

import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
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

   public StringBuilder sqlUpdate = new StringBuilder();
   public StringBuilder sqlInsert = new StringBuilder();

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
      String newValueString = null;
      String newValueTypeString = null;
      if (newValue != null)
      {
         Class valueClass = newValue.getClass();

         if (valueClass == String.class)
         {
            newValueString = (String) newValue;
            String encapsulated = Yamler.encapsulate((String) newValue);
            buf.append("  " + NEW_VALUE + ": ").append(encapsulated).append("\n");
         }
         else if (  valueClass.getName().startsWith("java.lang."))
         {
            newValueString = "" + newValue;
            buf.append("  " + NEW_VALUE + ": ").append(newValueString).append("\n");
         }
         else
         {
            newValueString = yamlIdMap.getOrCreateKey(newValue);
            buf.append("  " + NEW_VALUE + ": ").append(newValueString).append("\n");

            Reflector reflector = reflectorMap.getReflector(className);
            Object attrValue = reflector.getValue(source, prop);
            if (attrValue != null && Collection.class.isAssignableFrom(attrValue.getClass()))
            {
               historyKey += "/" + newValueString;
            }

            newValueTypeString = newValue.getClass().getSimpleName();
            buf.append("  " + NEW_VALUE_TYPE + ": ").append(newValueTypeString).append("\n");
         }
      }

      buf.append("  " + HISTORY_KEY + ": ").append(historyKey).append("\n");
      buf.append("\n");

      // database stuff
      sqlUpdate.setLength(0);
      sqlInsert.setLength(0);

      sqlUpdate.append("UPDATE PartyLog SET \n")
            .append("time = '").append(timeString).append("', ")
            .append("source = '").append(sourceKey).append("', ")
            .append("sourceType = '").append(className).append("', ")
            .append("property = '").append(prop).append("', ")
            .append("newValue = '").append(newValueString).append("', ");
      if (newValueTypeString != null)
      {
         sqlUpdate.append("newValueType = '").append(newValueTypeString).append("' ");
      }
      else
      {
         sqlUpdate.append("newValueType = NULL ");
      }

      sqlUpdate.append("\nWHERE historyKey = '").append(historyKey)
            .append("' ;");

      sqlInsert.append("INSERT INTO PartyLog\n")
            .append("VALUES (")
            .append("'").append(historyKey).append("', ")
            .append("'").append(timeString).append("', ")
            .append("'").append(sourceKey).append("', ")
            .append("'").append(className).append("', ")
            .append("'").append(prop).append("', ")
            .append("'").append(newValueString).append("', ");

      if (newValueTypeString != null)
      {
         sqlInsert.append("'").append(newValueTypeString).append("' ");
      }
      else
      {
         sqlInsert.append("NULL ");
      }

      sqlInsert.append(");\n");

      return buf.toString();
   }

   public Object decode(ResultSet resultSet, Object rootObject)
   {
      try
      {
         String firstKey = null;
         while (resultSet.next())
         {
            String historyKey = resultSet.getString("historyKey");

            // execute change
            String sourceKey = resultSet.getString(DataManager.SOURCE);

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
            String className = resultSet.getString(DataManager.SOURCE_TYPE);
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

            String property = resultSet.getString(DataManager.PROPERTY);
            String newValue = resultSet.getString(DataManager.NEW_VALUE);
            String newValueType = resultSet.getString(DataManager.NEW_VALUE_TYPE);

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

            System.out.println(historyKey);
         }

         Object firstObject = yamlIdMap.getObject(firstKey);

         return firstObject;
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return null;
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
