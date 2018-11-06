package de.uniks.albert;

import cucumber.api.java.en.Given;
import org.fulib.FulibTools;
import org.fulib.yaml.YamlObject;

public class StepDefinitions
{

   public static final String ROOM = "Room";
   public static final String ROOMS = "rooms";
   public static final String CREDITS = "credits";
   private YamlObject studyRight;
   private YamlObject mathRoom;
   private YamlObject hall;

   @Given("^The StudyRightUniversity is cool\\.$")
   public void the_StudyRightUniversity_is_cool()
   {
      studyRight = new YamlObject("studyRight", "University");
      studyRight.put("name", "StudyRight");

   }


   @Given("^The math room with (\\d+) credits$")
   public void the_math_room_with_credits(int credits) {
      mathRoom = new YamlObject("mathRoom", ROOM)
            .put(CREDITS, credits);
      studyRight.with("rooms", mathRoom);
   }


   @Given("^The entrance hall with (\\d+) credits$")
   public void the_entrance_hall_with_credits(int credits) {
      hall = new YamlObject("hall", ROOM)
            .put(CREDITS, credits);
      studyRight.with(ROOMS, hall);
      FulibTools.objectDiagrams().dumpSVG("tmp/UniDiag.svg", studyRight);
   }
}
