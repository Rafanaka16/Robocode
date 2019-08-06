package edu.ufl.cise.cs1.robots;

import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.io.Serializable;

public class PrimeMessage implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String objectiveName;                       //Stores the name of the objective robot (scanned)
    private String senderName;                          //Stores the name of the robot sending the message


    public PrimeMessage(ScannedRobotEvent scannedRobotEvent, Robot messenger)
    {
        objectiveName = scannedRobotEvent.getName();
        senderName = messenger.getName();
    }

    public String getObjectiveName()
    {
        return objectiveName;
    }
    public String getSenderName()
    {
        return senderName;
    }
}
