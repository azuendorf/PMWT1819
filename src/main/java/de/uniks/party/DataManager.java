package de.uniks.party;

import org.fulib.yaml.ModelListener;
import org.fulib.yaml.ReflectorMap;
import org.fulib.yaml.YamlIdMap;
import org.h2.jdbcx.JdbcDataSource;

import java.beans.PropertyChangeEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
   private EventYamler eventYamler;
   private ReflectorMap reflectorMap;
   private File logFilePath = null;
   private File modelFile = null;
   private String logDirName = "tmp";
   private String logFileName;
   private Connection conn;
   private Statement stmt;

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
      eventYamler = new EventYamler(packageName).setYamlIdMap(yamlIdMap);
      reflectorMap = new ReflectorMap(packageName);

      // try a database
      JdbcDataSource ds = new JdbcDataSource();
      ds.setURL("jdbc:h2:./tmp/h2test;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9090;");
      ds.setUser("sa");
      ds.setPassword("sa");
      try
      {
         conn = ds.getConnection();
         stmt = conn.createStatement();
         String sql =  "CREATE TABLE   PartyLog " +
            "(historyKey VARCHAR(255) not NULL, " +
            " time VARCHAR(255), " +
            " source VARCHAR(255), " +
            " sourceType VARCHAR(255), " +
            " property VARCHAR(255), " +
            " newValue VARCHAR(255), " +
            " newValueType VARCHAR(255), " +
            " PRIMARY KEY ( historyKey ));";

         stmt.executeUpdate(sql);
         System.out.println("Created table in given database...");

         // STEP 4: Clean-up environment
         //         stmt.close();
         //         conn.close();
      }
      catch (SQLException e)
      {
         System.out.println(e.getMessage());
      }

      // loadModel(rootObject);

      // loadEvents(rootObject);

      loadDatabase(rootObject);

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
      String yaml = eventYamler.encode(e);

      String sql = eventYamler.sqlUpdate.toString();
      try
      {
         if (e.getNewValue() != null)
         {

            int rowCount = stmt.executeUpdate(sql);
            System.out.println("update " + rowCount);

            if (rowCount == 0)
            {
               sql = eventYamler.sqlInsert.toString();
               rowCount = stmt.executeUpdate(sql);
               System.out.println("insert " + rowCount);
            }
         }
      }
      catch (Exception ex)
      {
         System.out.println(sql);
         ex.printStackTrace();
      }

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
            fileWriter.write(yaml);
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
            fileWriter.write(yaml);
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


   private void loadDatabase(Object rootObject)
   {
      String sql = "SELECT * FROM PartyLog;";
      try
      {
         boolean success = stmt.execute(sql);

         ResultSet resultSet = stmt.getResultSet();

         eventYamler.decode(resultSet, rootObject);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
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
      eventYamler.decode(rootObject, content);
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
