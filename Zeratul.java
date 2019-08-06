package edu.ufl.cise.cs1.robots;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;

public class Zeratul extends TeamRobot
{
    PrimeMessage messageReceived1;                                         //Stores the message sent from Bugger 1
    PrimeMessage messageReceived2;                                         //Stores the message sent from Bugger 2
    private double baseRangeOfMovement = 100;                              //Stores minimum oscillation range
    private double extraRangeOfMovement= 0;                                //Stores the random oscillation increment
    private double rotationAngle = 45;                                     //Stores angle of rotation angle for oscillation

    private double previousRobotEnergy  = 0;                               //Stores target energy during previous tick
    private int possibleStuck = 0;                                         //Stores how many tick the target speed has been 0
    private int possibleLastStanding = 0;                                  //Stores the number of times that the scanner detect only one robot
    private String nameOfTarget = "";                                      //Stores target name

    @Override
    public void run() {

        //#region TEAM_COLORS                                               //Sets team colors
        setAllColors(Color.black);
        setRadarColor(Color.CYAN);
        setScanColor(Color.white);
        setBulletColor(Color.CYAN);
        //#endregion

        setAdjustGunForRobotTurn(true);                                     //Enable move the gun independently from the body
        setAdjustRadarForGunTurn(true);                                     //Enable move radar independently from the gun
        setTurnRadarRight(Double.POSITIVE_INFINITY);                        //Rotate the radar infinitely times

        while (true) {                                                      //Basic Routine
            scan();
            setTurnRight(baseRangeOfMovement*rotationAngle);
            ahead(baseRangeOfMovement+extraRangeOfMovement);
            baseRangeOfMovement*=-1;                                        //Invert the oscillation direction
            extraRangeOfMovement*=-1;
            rotationAngle = 45;                                             //Reset basic rotation angle
            if(getRadarTurnRemaining()==0)                                  //Reset the infinitely radar rotation if an event disabled it
                setTurnRadarRight(Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        setTurnRight(-45);                                                  //Make an semi-circle to turn backward
        setBack(Utils.getRandom().nextInt(150));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event)
    {
        if(isTeammate(event.getName()))                                     //Assure that the scanned robot is not a teammate
            return;
        String nameObjective1 = messageReceived1!=null? messageReceived1.getObjectiveName():"";
        String nameObjective2 = messageReceived2!=null? messageReceived2.getObjectiveName():"";

        //Verifies if the scanned robot is under attack by a Bugger robot
        if(event.getName().equals(nameObjective1) || event.getName().equals(nameObjective2))
        {
            if(possibleLastStanding<10)                                     //Check if those are the only remaining robots, counting
            {                                                               //counting how many consecutively had been count
                possibleLastStanding++;
                return;
            }
        }
        else                                                                //In case that another robot was detected
            possibleLastStanding=0;

        oscillationToScannedObjective(event);                               //Update global variables to perform oscillation
        setTurnRight(event.getBearing()+90);

        //Aim radar and gun to objective
        setTurnRadarRight(2*Utils.normalRelativeAngleDegrees(event.getBearing() + ( getHeading()-getRadarHeading())));
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading()+ event.getBearing() - getGunHeading()));

        //In case that there is a Bugger close to the objective hold the fire
        if(messageReceived1 != null && messageReceived1.getObjectiveName().equals(event.getName()))
            return;
        if(messageReceived2 != null && messageReceived2.getObjectiveName().equals(event.getName()))
            return;

        double firePower = 0.1;
        if(event.getDistance()<350 || possibleStuck>7)                      //Calculate the fire power regarding the distance
        {
            if(event.getDistance()<80)
                firePower = 3;
            else if(event.getDistance()<180)
                firePower= 1.5;
            else
                firePower = 0.5;
            setFire(firePower);
        }
                                                                            //Check if the objective is stuck
        if(nameOfTarget.compareTo(event.getName())==0 && event.getVelocity()==0)
            possibleStuck++;
        else
            possibleStuck = 0;
    }

    @Override
    public void onHitWall(HitWallEvent event)                               //Actions to take when robot hits a wall
    {
        setBack(150);
        turnRight(-45);
    }

    @Override
    public void onMessageReceived(MessageEvent event)
    {
        if(event.getMessage() instanceof PrimeMessage)
        {
            PrimeMessage temp = (PrimeMessage) event.getMessage();          //Cast the message to PrimeMessage
            if(temp.getSenderName().contains("(2)"))                        //Storage the message (Depending to sender)
                messageReceived2 = temp;
            else
                messageReceived1 = temp;
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event)                         //Check if any of Buggers robot die to enable fire
    {                                                                       //against the objective
        if(messageReceived1 != null && messageReceived1.getSenderName().equals(event.getName()))
            messageReceived1 = null;
        else if(messageReceived2 != null && messageReceived2.getSenderName().equals(event.getName()))
            messageReceived2 = null;
    }

    //#region ADDITIONAL METHODS
    /*
        Obtain the sing of the extra range of movement.
        Used for the oscillation.
     */
    private int reverseExtraRangeOfMovementSign()
    {
        int sign = extraRangeOfMovement<0? -1:1;
        return sign*-1;
    }

    /*
        Perform variations needed to oscillate in a random length.
        Check if there is an enemy close to move back 50 pixel in and angle of +-90 degrees.
        Angle sing is determined arbitrary using the game time.
     */
    private void oscillationToScannedObjective(ScannedRobotEvent event)
    {
        if(event.getDistance()<100 )                                    //Check for close enemies.
        {
            setAhead(-50);                                              //Move back 50px
            int sign = getTime()%2==0? -1:1;                            //Compute the sing of the angle of rotation
            rotationAngle = 90*sign;                                    //Set rotation angle to 90. It will be executed in run()
        }
        double energyDifference = 0;                                    //Store the objective energy
        if(event.getName().compareTo(nameOfTarget)!=0)                  //Check if the current scanned objective is different same than the last scanned
        {
            previousRobotEnergy = event.getEnergy();                    //Store energy
            nameOfTarget = event.getName();                             //Store name
        }
        else                                                            //In case that is the same
        {
            energyDifference = previousRobotEnergy - event.getEnergy(); //Calculate energy variation (check if it fired)
            if(energyDifference>=0.1 && energyDifference<=3.0)          //In case of fire add and extra movement to oscillation trying to avoid the projectile
                baseRangeOfMovement =  reverseExtraRangeOfMovementSign()*-1*Utils.getRandom().nextInt(100)+20;
            else                                                        //If no variation of energy detected there is not need of move extra
                extraRangeOfMovement=0;
        }
    }
    //#endregion
}

/***************************************************************************************
 * CITATIONS:
 *
 *    Title: One on One Radar
 *    Author: RoboWiki
 *    Date: 09/05/2017
 *    Availability: http://robowiki.net/wiki/One_on_One_Radar
 *
 *    Lines where code was used: 30-32, 42, 77-78
 ***************************************************************************************/