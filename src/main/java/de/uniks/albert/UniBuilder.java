package de.uniks.albert;

import de.uniks.albert.model.Assignment;
import de.uniks.albert.model.Room;
import de.uniks.albert.model.Student;

import javax.print.attribute.standard.OutputDeviceAssigned;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UniBuilder
{
   public boolean doAssignments(Student karli)
   {
      Objects.requireNonNull(karli);
      karli.setPoints(0);

      Room r = karli.getIn();

      if (r == null)
      {
         Logger.getGlobal().log(Level.WARNING, "student has no room can't do assignments");
         return false;
      }

      for (Assignment a : r.getAssignments())
      {
         int p = a.getPoints();

         if (karli.getMotivation() < a.getPoints()
               || karli.getDone().contains(a))
         {
            continue;
         }

         karli.setPoints( karli.getPoints() + p);
         karli.setMotivation(karli.getMotivation()- p);

         karli.withDone(a);

         //  Still points needed for credits of current topic?this.points >= r.credits ?
         if (karli.getPoints() >= r.getCredits())
         {
            // turn in points. Get credits.this.points = 0, this.credits += r.credits
            karli.setPoints(0);
            karli.setCredits(karli.getCredits() + r.getCredits());

            return true;
         }
      }

      // out of assignments
      Logger.getGlobal().log(Level.WARNING, "could not get credits");
      karli.setPoints(0);

      return false;
   }
}
