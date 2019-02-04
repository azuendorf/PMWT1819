package de.uniks.albert;

import de.uniks.albert.model.Assignment;
import de.uniks.albert.model.Room;
import de.uniks.albert.model.Student;
import de.uniks.albert.model.University;
import de.uniks.albert.model.tables.AssignmentTable;
import de.uniks.albert.model.tables.RoomTable;
import de.uniks.albert.model.tables.UniversityTable;
import de.uniks.albert.model.tables.doubleTable;
import org.fulib.FulibTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;

public class StudyRightTests
{
   @Test
   public void testModelQueries()
   {
      // build object structure
      University studyRight = new University().setName("Study Right");
      String name = University[].class.getName();
      System.out.println(name);

      Room mathRoom = new Room().setRoomNo("wa1337").setTopic("Math").setCredits(42.0).setUni(studyRight);
      Room artsRoom = new Room().setRoomNo("wa1338").setTopic("Arts").setCredits(23.0).setUni(studyRight);
      Room sportsRoom = new Room().setRoomNo("wa1339").setTopic("Football").setUni(studyRight);

      Assignment integrals = new Assignment().setTask("integrals").setPoints(42).setRoom(mathRoom);
      Assignment matrix = new Assignment().setTask("matrices").setPoints(23).setRoom(mathRoom);
      Assignment drawings = new Assignment().setTask("drawings").setPoints(12).setRoom(artsRoom);
      Assignment sculptures = new Assignment().setTask("sculptures").setPoints(12).setRoom(artsRoom);

      Student alice = new Student().setStudentId("m4242").setName("Alice").setUni(studyRight).setIn(mathRoom).withDone(integrals);
      Student bob   = new Student().setStudentId("m2323").setName("Bobby"  ).setUni(studyRight).setIn(artsRoom).withFriends(alice);
      Student carli = new Student().setStudentId("m2323").setName("Carli").setUni(studyRight).setIn(mathRoom);
      // end_code_fragment:

      FulibTools.objectDiagrams().dumpSVG("tmp/studyright/studyRightObjects.svg", studyRight);
      FulibTools.objectDiagrams().dumpPng("tmp/studyright/studyRightObjects.png", studyRight);


      // some table stuff
      UniversityTable universityTable = new UniversityTable(studyRight);
      double tableSum = universityTable
            .expandRooms("Room")
            .expandAssignments("Assignment")
            .expandPoints("Points")
            .sum();

      // JAVA 8
      double streamSum =
            studyRight.getRooms().stream()
                  .flatMap(r -> r.getAssignments().stream())
                  .mapToDouble(a -> a.getPoints())
                  .sum();

      System.out.println(streamSum);

      System.out.println(universityTable.toMarkDown());
      System.out.println( " " + tableSum);

   }
}
