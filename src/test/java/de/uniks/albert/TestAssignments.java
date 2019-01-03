package de.uniks.albert;

import de.uniks.albert.model.Assignment;
import de.uniks.albert.model.Room;
import de.uniks.albert.model.Student;
import org.fulib.Fulib;
import org.fulib.FulibTools;
import org.fulib.builder.ClassBuilder;
import org.fulib.builder.ClassModelBuilder;
import org.fulib.classmodel.ClassModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.equalTo;

public class TestAssignments
{
   @Test
   public void testBadCases() throws IOException
   {
      Logger.getGlobal().addHandler(new FileHandler("tmp/logfile.log"));
      UniBuilder ub = new UniBuilder();

      // no student
      try {
         ub.doAssignments(null);
         Assert.fail();
      }
      catch (NullPointerException e)
      {
         // cool
      }

      // no room
      Student karli = new Student().setName("Karli")
            .setMotivation(4);

      ub.doAssignments(karli);
      Assert.assertThat(karli.getMotivation(), equalTo(4));

      // karli in room but no assignments
      Room math = new Room().setTopic("math").setCredits(17);
      karli.setIn(math);

      boolean result = ub.doAssignments(karli);

      Assert.assertThat(result, equalTo(false));

      Assignment a1 = new Assignment().setTask("series").setPoints(5);
      math.withAssignments(a1);

      result = ub.doAssignments(karli);

      Assert.assertThat(karli.getCredits(), equalTo(0));

   }


   @Test
   public void testAssignments()
   {
      //      Karli is in the math room.
      Student karli = new Student().setName("Karli").setMotivation(214);

      Room math = new Room().setTopic("math").setCredits(17);

      karli.setIn(math);

      Assignment series = new Assignment().setTask("Series").setPoints(5);
      Assignment integrals = new Assignment().setTask("Integrals").setPoints(5);
      Assignment matrices = new Assignment().setTask("Matrices").setPoints(8);

      math.withAssignments(series, integrals, matrices);

      String fileName = FulibTools.objectDiagrams().dumpPng(karli);

      System.out.println(fileName);

      Assert.assertThat(karli.getIn(), equalTo(math));

      //      There are assignments for
      //      integrals, series, and matrices.
      //      Karli does assignments
      UniBuilder ub = new UniBuilder();
      ub.doAssignments(karli);

      //      Karli got 17 credits
      Assert.assertThat(karli.getCredits(), equalTo(17));
      Assert.assertThat(karli.getDone().size(), equalTo(3));
      Assert.assertThat(karli.getMotivation(), equalTo(214-18));
   }



   public static void main(String[] args)
   {
      new TestAssignments().generateClassModelCode();
   }

   private void generateClassModelCode()
   {
      ClassModelBuilder mb = Fulib.classModelBuilder("de.uniks.albert.model");

      ClassBuilder uni = mb.buildClass("University")
            .buildAttribute("name", mb.STRING);

      ClassBuilder student = mb.buildClass("Student")
            .buildAttribute("matNo", mb.INT)
            .buildAttribute("name", mb.STRING)
            .buildAttribute("credits", mb.INT)
            .buildAttribute("points", mb.INT)
            .buildAttribute("motivation", mb.INT);

      uni.buildAssociation(student, "students", mb.MANY, "uni", mb.ONE);

      ClassBuilder room = mb.buildClass("Room")
            .buildAttribute("topic", mb.STRING)
            .buildAttribute("credits", mb.INT);

      uni.buildAssociation(room, "rooms", mb.MANY, "uni", mb.ONE)
            .setAggregation();

      ClassBuilder assignment = mb.buildClass("Assignment")
            .buildAttribute("task", mb.STRING)
            .buildAttribute("points", mb.INT);

      assignment.buildAssociation(room, "room", mb.ONE, "assignments", mb.MANY);
      student.buildAssociation(assignment, "done", mb.MANY, "students", mb.MANY);

      student.buildAssociation(room, "in", mb.ONE, "students", mb.MANY);

      ClassModel model = mb.getClassModel();

      Fulib.generator().generate(model);
   }
}
