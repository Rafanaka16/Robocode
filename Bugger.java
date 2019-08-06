package edu.ufl.cise.cs1.robots;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.io.IOException;

public class Bugger extends TeamRobot
{
    public void run()
    {
        //#region TEAM_COLORS
        setAllColors(Color.black);
        setRadarColor(Color.CYAN);
        setScanColor(Color.white);
        setBulletColor(Color.CYAN);
        //#endregion

        setTurnRadarRight(Double.POSITIVE_INFINITY);    //Set radar to rotate infinitely many times
        setAdjustGunForRobotTurn(true);     //Applies property than allows gun and radar to rotate free
        setAdjustRadarForGunTurn(true);
        while (true)
        {
            scan();     //Robot scan routine
            if(getRadarTurnRemaining()==0)
                setTurnRadarRight(360);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event)
    {
        if(isTeammate(event.getName()))     //Check if it is a team member
            return;
        setTurnRadarRight(2.0 * Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getRadarHeading()));   //Align the radar to the objective
        setTurnGunRight(Utils.normalRelativeAngleDegrees(getHeading() + event.getBearing() - getGunHeading()));     //Align the gun to the objective
        if (event.getDistance() <= 90)      //Check is the enemy is close to shot
        {
            fire(3);
            try
            {
                broadcastMessage(new PrimeMessage(event,this));     //Send enemy information to team members
            }
            catch (IOException e)
            {
                System.out.println("Unable to broadcast message");
            }
        }
        else
        {
            setTurnRight(Utils.normalRelativeAngleDegrees(event.getBearing()));     //Approach the objective
            setAhead(event.getDistance());
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent event)
    {
        if(isTeammate(event.getName()))     //If hit a team member, reverse 100px in a 45 degrees angel
        {
            setTurnRight(45);
            setBack(100);
        }
    }

    @Override
    public void onHitWall(HitWallEvent event)       //If hit a wall, reverse 100px in a 45 degrees angel
    {
        setTurnRight(45);
        setBack(100);
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