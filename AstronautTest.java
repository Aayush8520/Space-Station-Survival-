
import java.util.ArrayList;

public class AstronautTest {

    public static void main(String[] args) {

        Engineer sky = createEngineer("Sky");

        showAstronautInfo(sky);
        moveTowardsRocket(sky);
        boardRocket(sky);
        engineerRepairsEquipment(sky);

        Photographer rain = createPhotographer("Rain");

        showAstronautInfo(rain);
        moveTowardsRocket(rain);
        boardRocket(rain);
        photographerPreparesCamera(rain);

        // Generic collection of astronauts
        ArrayList<Astronaut> crew = new ArrayList<>();

        crew.add(sky);
        crew.add(rain);

        System.out.println("\n=== Victory Crew Tasks ===");

        // Polymorphism
        for (Astronaut astronaut : crew) {
            astronaut.performTask();
        }
    }

    public static Engineer createEngineer(String name) {
        System.out.println("\n=== Creating Engineer ===");
        return new Engineer(name);
    }

    public static Photographer createPhotographer(String name) {
        System.out.println("\n=== Creating Photographer ===");
        return new Photographer(name);
    }

    public static void showAstronautInfo(Astronaut astronaut) {
        System.out.println("\n=== Astronaut Details ===");
        System.out.println("Name: " + astronaut.getName());
        System.out.println("Health: " + astronaut.getHealth());
        System.out.println("Oxygen: " + astronaut.getOxygen());
    }

    public static void moveTowardsRocket(Astronaut astronaut) {
        System.out.println("\n=== Moving Towards the Rocket ===");
        System.out.println(
                astronaut.getName()
                        + " is walking towards the Victory rocket."
        );
    }

    public static void boardRocket(Astronaut astronaut) {
        System.out.println("\n=== Boarding the Rocket ===");
        astronaut.board();
    }

    public static void engineerRepairsEquipment(Engineer engineer) {
        System.out.println("\n=== Engineer Repairing Equipment ===");

        SolarPanel panel = new SolarPanel();
        engineer.repairEquipment(panel);
    }

    public static void photographerPreparesCamera(
            Photographer photographer) {

        System.out.println(
                "\n=== Photographer Preparing the Camera ==="
        );

        photographer.performTask();
    }
}
