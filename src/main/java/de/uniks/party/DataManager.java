package de.uniks.party;

import org.fulib.yaml.*;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager
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

   private YamlIdMap yamlIdMap;
   private ReflectorMap reflectorMap;
   private File logFilePath = null;
   private File modelFile = null;
   private String logDirName = "tmp";
   private String logFileName;

   public static DataManager get()
   {
      return new DataManager();
   }

   public DataManager attach(Object rootObject, String logDirName)
   {
      getOrCreateLogDir(logDirName);

      this.logFileName = "logFile.yaml";

      String packageName = rootObject.getClass().getPackage().getName();
      yamlIdMap = new YamlIdMap(packageName);
      reflectorMap = new ReflectorMap(packageName);

      loadModel(rootObject);

      loadEvents(rootObject);

      storeModel(rootObject);

      removeLogFile();

      new ModelListener(rootObject, e -> handleEvent(e));

      return this;
   }

   private void removeLogFile()
   {
      try
      {
         File logFile = new File(logDirName + "/" + logFileName);
         if (logFile.exists())
         {
            logFile.delete();
         }
      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, " could not remove log file " + this.logFileName);
      }
   }


   private void getOrCreateLogDir(String logDirName)
   {
      this.logDirName = logDirName;

      try
      {
         File logDirFile = new File(logDirName);

         if ( ! logDirFile.exists())
         {
            logDirFile.mkdirs();
         }
      }
      catch (Exception e)
      {
         // maybe we are on an android system
         this.logDirName = "/sdcard/" + this.logDirName;

         try
         {
            File logDirFile = new File(logDirName);

            if ( ! logDirFile.exists())
            {
               logDirFile.mkdirs();
            }
         }
         catch (Exception e2)
         {
            Logger.getGlobal().log(Level.SEVERE, "could not create log directory " + this.logDirName, e2);
         }
      }
   }


   private void handleEvent(PropertyChangeEvent e)
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

      try
      {
         File logDirFile = new File(logDirName);
         try
         {
            boolean mkdirs = logDirFile.mkdirs();
            logFilePath = new File(logDirName +"/" + logFileName);
            if ( ! logFilePath.exists())
            {
               logFilePath.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(logFilePath, true);
            fileWriter.write(buf.toString());
            fileWriter.flush();
            fileWriter.close();
         }
         catch (Exception e2)
         {
            logDirName = "/sdcard/" + logDirName;
            logDirFile = new File(logDirName);
            logDirFile.mkdirs();
            logFilePath = new File(logDirName +"/" + logFileName);
            if ( ! logFilePath.exists())
            {
               logFilePath.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(logFilePath, true);
            fileWriter.write(buf.toString());
            fileWriter.flush();
            fileWriter.close();
         }
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
         Logger.getGlobal().log(Level.SEVERE, "could not write log to " + logFilePath, e);
      }
   }


   private boolean storeModel(Object rootObject)
   {
      try
      {
         modelFile = new File(logDirName + "/model.yaml");

         if ( ! modelFile.exists())
         {
            modelFile.createNewFile();
         }

         String yamlText = yamlIdMap.encode(rootObject);

         FileWriter fileWriter = new FileWriter(logDirName + "/model.yaml");
         fileWriter.write(yamlText);
         fileWriter.flush();
         fileWriter.close();

         return true;
      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, "could not store model to file " + this.logDirName + "/model.yaml");
      }

      return false;
   }


   private void loadModel(Object rootObject)
   {
      try
      {
         modelFile = new File(logDirName + "/model.yaml");

         if ( ! modelFile.exists())
         {
           return;
         }

         byte[] bytes = read(modelFile);

         if (bytes == null) return;

         String content = new String(bytes);

         yamlIdMap.decode(content, rootObject);

      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, "could not store model to file " + this.logDirName + "/model.yaml");
      }
   }




   private void loadEvents(Object rootObject)
   {

      try
      {
         File logDirFile = new File(logDirName);
         if (logDirFile.exists())
         {
            logFilePath = new File(logDirName + "/" + logFileName);
         }
         else
         {
            logDirFile = new File("/sdcard/" + logDirName);
            logFilePath = new File(logDirName + "/" + logFileName);
         }
      }
      catch (Exception e)
      {
         Logger.getGlobal().log(Level.SEVERE, "could not create log dir " + logDirName);
         return;
      }

      if (logFilePath == null) return;
      if ( ! logFilePath.exists()) return;

      byte[] bytes = new byte[0];
      bytes = read(logFilePath);

      if (bytes == null) return;

      String content = new String(bytes);

      String packageName = rootObject.getClass().getPackage().getName();
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
   }



   public byte[] read(File file) {
      byte[] buffer = new byte[(int) file.length()];
      InputStream ios = null;
      try
      {
         ios = new FileInputStream(file);
         if (ios.read(buffer) == -1)
         {
            throw new RuntimeException("EOF reached while trying to read the whole file");
         }
      }
      catch (Exception e)
      {
        Logger.getGlobal().log(Level.SEVERE, "failed reading yaml log", e);
      }
      finally
      {
         try
         {
            if (ios != null)
               ios.close();
         }
         catch (IOException e)
         {
         }
      }
      return buffer;
   }


}
