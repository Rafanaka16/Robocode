package edu.ufl.cise.cs1.robots;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Nova extends TeamRobot
{
    private int changeMovement=1;                                                   //Global variable that controls direction of movement
    PrimeMessage objectiveUnderAttackInfo1;
    PrimeMessage objectiveUnderAttackInfo2;

    public void run()
    {
        //#region TEAM_COLORS                                                       //Set team colors
        setAllColors(Color.black);
        setRadarColor(Color.CYAN);
        setScanColor(Color.white);
        setBulletColor(Color.CYAN);
        //#endregion

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRight(Double.POSITIVE_INFINITY);

        while (true)
        {
            scan();
            if(getRadarTurnRemaining()==0) {
                setTurnRadarRight(Double.POSITIVE_INFINITY);
            }
        }
    }

    @Override public void onScannedRobot (ScannedRobotEvent event)
    {

        String nameObjective1 = objectiveUnderAttackInfo1!=null? objectiveUnderAttackInfo1.getObjectiveName():"";
        String nameObjective2 = objectiveUnderAttackInfo2!=null? objectiveUnderAttackInfo2.getObjectiveName():"";

        if(event.getName().equals(nameObjective1) || event.getName().equals(nameObjective2))
            return;

        if(!isTeammate(event.getName()))
        {
            aiming(event);                                                          //This method aim the enemy tank
            movementToAttack(event);                                                //This method do the movement

            if(event.getDistance()<70)
            {
                setFire(3);
                return;
            }
            fireChoice(event);                                                      //This method fire the ball it makes a choice depending on the distance
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event)
    {
        setTurnRight(90);
        setAhead(100);
        //setTurnRadarRight(2 * normalRelativeAngleDegrees(getHeading() - getRadarHeading() + event.getBearing()));
    }

    @Override
    public void onHitWall(HitWallEvent event)
    {
        setBack(200);                                                               //Change movement check for the variable change movement
        if(getVelocity()==0)                                                       //Means the velocity of the tank is zero tank is stopped
        {
            setTurnLeft(45);
            setAhead(100);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if(event.getMessage() instanceof PrimeMessage)
        {
            PrimeMessage temp = (PrimeMessage) event.getMessage();
            if(temp.getSenderName().contains("(2)"))
                objectiveUnderAttackInfo2 = temp;
            else
                objectiveUnderAttackInfo1 = temp;
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        if(objectiveUnderAttackInfo1 != null && objectiveUnderAttackInfo1.getSenderName().equals(event.getName()))
            objectiveUnderAttackInfo1 = null;
        else if(objectiveUnderAttackInfo2 != null && objectiveUnderAttackInfo2.getSenderName().equals(event.getName()))
            objectiveUnderAttackInfo2 = null;
    }

    private void fireChoice(ScannedRobotEvent event)
    {
        if(getEnergy()<=40)                                                       //When the robot energy is low
        {
            if (event.getDistance() < 10) setFire(3);
            else if(event.getDistance()<20) setFire(1.5);
            else return;
        }
        if(event.getEnergy()<=30)                                                //Try to kill the enemy when its energy is low
            setFire(3);
        if (event.getDistance() < 10)                                            //When the enemy is close use all the power
            setFire(3);
        else
            setFire(1);
    }

    private void movementToAttack(ScannedRobotEvent event)
    {
        setTurnRight(Utils.normalRelativeAngleDegrees(event.getBearing() + 90));  // It position my tank 90 degrees with respective to the head of the tank

        if(getVelocity()==0)                                                      //Means the velocity of the tank is zero tank is stopped
            changeMovement*= -1;

        if (getTime() % 10 == 0)                                                  //Change its behavior every 10 ticks
        {
            changeMovement *= -1;                                                 //Its changing the movement back and for
            setAhead(300* changeMovement);                                        //This allow the tank to move faster
        }
    }

    private void aiming(ScannedRobotEvent event)
    {
        double angleToPoint=getHeading() - getRadarHeading() + event.getBearing();//This variable is to determine the position of the enemy solider
        double angleToShot=getHeading()-getGunHeading() + event.getBearing();     //This variable is to determine the angle to shot
        setTurnRadarRight(2 * normalRelativeAngleDegrees(angleToPoint));          //Set the radar in the direction of the enemy, lock the radar the number 4 means a bigger arc
        setTurnGunRight(normalRelativeAngleDegrees(angleToShot));                 //Set the gun towards the enemy
    }
}

/***************************************************************************************
 * CITATIONS:
 *
 *    Title: One on One Radar
 *    Author: RoboWiki
 *    Date: 09/05/2017
 *    Availability: http://robowiki.net/wiki/One_on_One_Radar
 *
 *    Lines where code was used: 19-21, 35-36, 51
 ***************************************************************************************/

