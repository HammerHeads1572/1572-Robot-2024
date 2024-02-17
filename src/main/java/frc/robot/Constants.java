// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

    public static final int armLeaderID = 14;
    public static final int armFollowerID = 15;

    public static final int wristMotorID = 16;

    public static final int intakeMotorID = 18;    

    public static final int ArmLED = 20;

    public static final double [] wristPID = {0.06, 0, 8.};
    public static final double [] armPID = {0.065, 0, 6.};


    public static final class Intake
    {
        public static final double currentThreshold = 30.0;
        public static final int msToHold = 2500;
        public static final double holdSpeed = 0.2;
    }

}
