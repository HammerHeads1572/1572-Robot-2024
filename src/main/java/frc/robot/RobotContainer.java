// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.wpilibj2.command.Commands.runOnce;

import java.util.function.Supplier;

import com.ctre.phoenix6.Utils;
import com.ctre.phoenix6.mechanisms.swerve.SwerveRequest;
import com.ctre.phoenix6.mechanisms.swerve.SwerveModule.DriveRequestType;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Util.SysIdRoutine.Direction;
import frc.robot.Vision.Limelight;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Feeder;



public class RobotContainer {
  private SendableChooser<Command> autoChooser;
  private SendableChooser<String> controlChooser = new SendableChooser<>();
  private SendableChooser<Double> speedChooser = new SendableChooser<>();
  private double MaxSpeed = TunerConstants.kSpeedAt12VoltsMps; // Initial max is true top speed
  private final double TurtleSpeed = 0.1; // Reduction in speed from Max Speed, 0.1 = 10%
  private final double MaxAngularRate = Math.PI * 2; // .75 rotation per second max angular velocity.  Adjust for max turning rate speed.
  private final double TurtleAngularRate = Math.PI * 1; // .75 rotation per second max angular velocity.  Adjust for max turning rate speed.
  private double AngularRate = MaxAngularRate; // This will be updated when turtle and reset to MaxAngularRate

  /* Setting up bindings for necessary control of the swerve drive platform */
  CommandXboxController drv = new CommandXboxController(0); // driver xbox controller
  CommandXboxController op = new CommandXboxController(1); // operator xbox controller
  CommandSwerveDrivetrain drivetrain = TunerConstants.DriveTrain; // drivetrain
  public Intake m_Intake = new Intake(Constants.LowerintakeMotorID, Constants.UpperintakeMotorID);
  public Arm m_Arm = new Arm(Constants.armPID, Constants.armLeaderID, Constants.armFollowerID);
  public Shooter m_Shooter = new Shooter(Constants.shooterPID, Constants.RightShooterID, Constants.LeftShooterID);
  public Feeder m_Feeder = new Feeder(Constants.FeederMotorID);




  // Field-centric driving in Open Loop, can change to closed loop after characterization 
  SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric().withDriveRequestType(DriveRequestType.OpenLoopVoltage).withDeadband(MaxSpeed * 0.01).withRotationalDeadband(AngularRate * 0.1);
  // Field-centric driving in Closed Loop.  Comment above and uncomment below.
  //SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric().withDriveRequestType(DriveRequestType.Velocity).withDeadband(MaxSpeed * 0.1).withRotationalDeadband(AngularRate * 0.1);

  SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
  SwerveRequest.RobotCentric forwardStraight = new SwerveRequest.RobotCentric().withDriveRequestType(DriveRequestType.OpenLoopVoltage);
  SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

  Limelight vision = new Limelight(drivetrain);

  Telemetry logger = new Telemetry(MaxSpeed);

  Pose2d odomStart = new Pose2d(0, 0, new Rotation2d(0,0));

  private Supplier<SwerveRequest> controlStyle;

  private String lastControl = "2 Joysticks";
  private Double lastSpeed = 0.65;

  private void configureBindings() {
    newControlStyle();
    newSpeed();

    drv.a().whileTrue(drivetrain.applyRequest(() -> brake));
    drv.b().whileTrue(drivetrain
        .applyRequest(() -> point.withModuleDirection(new Rotation2d(-drv.getLeftY(), -drv.getLeftX()))));

    // reset the field-centric heading on start button press
    drv.button(8).onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldRelative()));

    // Turtle Mode while not held
    drv.leftBumper().onFalse(runOnce(() -> MaxSpeed = TunerConstants.kSpeedAt12VoltsMps * TurtleSpeed)
        .andThen(() -> AngularRate = TurtleAngularRate));
    drv.leftBumper().onTrue(runOnce(() -> MaxSpeed = TunerConstants.kSpeedAt12VoltsMps * speedChooser.getSelected())
        .andThen(() -> AngularRate = MaxAngularRate));

    if (Utils.isSimulation()) {
      drivetrain.seedFieldRelative(new Pose2d(new Translation2d(), Rotation2d.fromDegrees(0)));
    }
    drivetrain.registerTelemetry(logger::telemeterize);

    Trigger controlPick = new Trigger(() -> lastControl != controlChooser.getSelected());
    controlPick.onTrue(runOnce(() -> newControlStyle()));

    Trigger speedPick = new Trigger(() -> lastSpeed != speedChooser.getSelected());
    speedPick.onTrue(runOnce(() -> newSpeed()));

    op.leftBumper().whileTrue(new InstantCommand(() -> m_Intake.setSpeed(-.75)));
    op.leftBumper().whileFalse(new InstantCommand(() -> m_Intake.setSpeed(.0)));
   
    op.leftBumper().whileTrue(new InstantCommand(() -> m_Intake.setUpperSpeed(-.40)));
    op.leftBumper().whileFalse(new InstantCommand(() -> m_Intake.setUpperSpeed(.0)));

    op.leftBumper().whileTrue(new InstantCommand(() -> m_Feeder.SetSpeed(.5)));
    op.leftBumper().whileFalse(new InstantCommand(() -> m_Feeder.SetSpeed(.0)));



    op.rightBumper().whileTrue(new InstantCommand(() -> m_Feeder.SetSpeed(1)));
    op.rightBumper().whileFalse(new InstantCommand(() -> m_Feeder.SetSpeed(0)));

    op.leftTrigger().onTrue(new InstantCommand(() -> m_Arm.setArmAngle(Arm.arm_angle + 5)));

    op.rightTrigger().onTrue(new InstantCommand(() -> m_Arm.setArmAngle(Arm.arm_angle - 5)));


    op.a().onTrue(new InstantCommand(() -> m_Shooter.ToggleShooter()));

    op.povUp().whileTrue(new InstantCommand(() -> m_Shooter.setLeftSpeed(-50)));
    op.povUp().whileTrue(new InstantCommand(() -> m_Shooter.setRightSpeed(-50)));
    op.povUp().whileTrue(new InstantCommand(() -> m_Feeder.SetSpeed(-1)));
    op.povUp().whileTrue(new InstantCommand(() -> m_Intake.setUpperSpeed(1)));
    op.povUp().whileTrue(new InstantCommand(() -> m_Intake.setSpeed(1)));


    op.povUp().whileFalse(new InstantCommand(() -> m_Intake.setUpperSpeed(0)));
    op.povUp().whileFalse(new InstantCommand(() -> m_Shooter.setLeftSpeed(0)));
    op.povUp().whileFalse(new InstantCommand(() -> m_Shooter.setRightSpeed(0)));
    op.povUp().whileFalse(new InstantCommand(() -> m_Feeder.SetSpeed(0)));
    op.povUp().whileFalse(new InstantCommand(() -> m_Intake.setSpeed(0)));


    
    op.y().onTrue(new InstantCommand(() -> m_Arm.setArmAngle(45)));
    op.y().onTrue(new InstantCommand(() -> m_Shooter.setLeftSpeed(75)));
    op.y().onTrue(new InstantCommand(() -> m_Shooter.setRightSpeed(70)));


    op.x().onTrue(new InstantCommand(() -> m_Arm.setArmAngle(110)));
    op.x().onTrue(new InstantCommand(() -> m_Shooter.setLeftSpeed(100)));
    op.x().onTrue(new InstantCommand(() -> m_Shooter.setRightSpeed(95))); 
  
    op.b().onTrue(new InstantCommand(() -> m_Arm.setArmAngle(125)));
    op.b().onTrue(new InstantCommand(() -> m_Shooter.setLeftSpeed(0)));
    op.b().onTrue(new InstantCommand(() -> m_Shooter.setRightSpeed(0)));

    op.button(8).onTrue(new InstantCommand(() -> m_Arm.setArmAngle(0)));
    op.button(8).onTrue(new InstantCommand(() -> m_Shooter.setLeftSpeed(0)));
    op.button(8).onTrue(new InstantCommand(() -> m_Shooter.setRightSpeed(0)));

    

    //Drivetrain
    drv.x().and(drv.pov(0)).whileTrue(drivetrain.runDriveQuasiTest(Direction.kForward));
    drv.x().and(drv.pov(180)).whileTrue(drivetrain.runDriveQuasiTest(Direction.kReverse));

    drv.y().and(drv.pov(0)).whileTrue(drivetrain.runDriveDynamTest(Direction.kForward));
    drv.y().and(drv.pov(180)).whileTrue(drivetrain.runDriveDynamTest(Direction.kReverse));

    drv.a().and(drv.pov(0)).whileTrue(drivetrain.runSteerQuasiTest(Direction.kForward));
    drv.a().and(drv.pov(180)).whileTrue(drivetrain.runSteerQuasiTest(Direction.kReverse));

    drv.b().and(drv.pov(0)).whileTrue(drivetrain.runSteerDynamTest(Direction.kForward));
    drv.b().and(drv.pov(180)).whileTrue(drivetrain.runSteerDynamTest(Direction.kReverse));

    // Drivetrain needs to be placed against a sturdy wall and test stopped immediately upon wheel slip
    drv.back().and(drv.pov(0)).whileTrue(drivetrain.runDriveSlipTest());

    drv.rightBumper().whileTrue(new RunCommand(() -> vision.Search()));

  }

  public RobotContainer() {

    NamedCommands.registerCommand("Run Intake", new InstantCommand(() -> m_Intake.setSpeed(-0.75)));
    NamedCommands.registerCommand("Run Upper Intake", new InstantCommand(() -> m_Intake.setUpperSpeed(-0.75)));
    NamedCommands.registerCommand("Intake Off", new InstantCommand(() -> m_Intake.setSpeed(0)));
    NamedCommands.registerCommand("Upper Intake Off", new InstantCommand(() -> m_Intake.setUpperSpeed(0)));
    NamedCommands.registerCommand("Arm 45", new InstantCommand(() -> m_Arm.setArmAngle(45)));
    NamedCommands.registerCommand("Toggle Fly Wheels", new InstantCommand(() -> m_Shooter.ToggleShooter()));
    NamedCommands.registerCommand("Feeder", new InstantCommand(() -> m_Feeder.SetSpeed(1)));
    NamedCommands.registerCommand("Feeder Off", new InstantCommand(() -> m_Feeder.SetSpeed(0)));
    NamedCommands.registerCommand("Arm 120", new InstantCommand(() -> m_Arm.setArmAngle(120)));
    NamedCommands.registerCommand("Arm 37", new InstantCommand(() -> m_Arm.setArmAngle(37)));

    
  
    // Detect if controllers are missing / Stop multiple warnings
    DriverStation.silenceJoystickConnectionWarning(true);

    // Build an auto chooser. This will use Commands.none() as the default option.
    autoChooser = AutoBuilder.buildAutoChooser();
 

    controlChooser.setDefaultOption("2 Joysticks", "2 Joysticks");
    controlChooser.addOption("1 Joystick Rotation Triggers", "1 Joystick Rotation Triggers");
    controlChooser.addOption("Split Joysticks Rotation Triggers", "Split Joysticks Rotation Triggers");
    controlChooser.addOption("2 Joysticks with Gas Pedal", "2 Joysticks with Gas Pedal");
    SmartDashboard.putData("Control Chooser", controlChooser);

    speedChooser.addOption("100%", 1.0);
    speedChooser.addOption("95%", 0.95);
    speedChooser.addOption("90%", 0.9);
    speedChooser.addOption("85%", 0.85);
    speedChooser.addOption("80%", 0.8);
    speedChooser.addOption("75%", 0.75);
    speedChooser.addOption("70%", 0.7);
    speedChooser.setDefaultOption("65%", 0.65);
    speedChooser.addOption("60%", 0.6);
    speedChooser.addOption("55%", 0.55);
    speedChooser.addOption("50%", 0.5);
    speedChooser.addOption("35%", 0.35);
    speedChooser.addOption("10%", 0.1);
    SmartDashboard.putData("Speed Limit", speedChooser);
    
    SmartDashboard.putData("Auto Chooser", autoChooser);
    
    configureBindings();
  }

  public Command getAutonomousCommand() {
    /* First put the drivetrain into auto run mode, then run the auto */
    return autoChooser.getSelected();
  }

  public Command elimsAuto(){
    return new InstantCommand(()-> m_Arm.setArmAngle(42)).andThen
    (new InstantCommand(() -> {m_Shooter.setLeftSpeed(95); m_Shooter.setRightSpeed(100);})).andThen(new WaitCommand(3)).andThen
    (new InstantCommand(()-> m_Feeder.SetSpeed(1))).andThen(new InstantCommand(()-> m_Intake.setUpperSpeed(-1)));
  }

  public Command SetForward(Rotation2d Angle){
    return drivetrain.runOnce(() -> drivetrain.setOperatorPerspectiveForward(Angle));
  }

  private void newControlStyle() {
    lastControl = controlChooser.getSelected();
    switch (controlChooser.getSelected()) {
      case "2 Joysticks":
        controlStyle = () -> drive.withVelocityX(-drv.getLeftY() * MaxSpeed) // Drive forward -Y
            .withVelocityY(-drv.getLeftX() * MaxSpeed) // Drive left with negative X (left)
            .withRotationalRate(Limelight.rotationMovement  * AngularRate); // Drive counterclockwise with negative X (left)
        break;
      case "1 Joystick Rotation Triggers":
        controlStyle = () -> drive.withVelocityX(-drv.getLeftY() * MaxSpeed) // Drive forward -Y
            .withVelocityY(-drv.getLeftX() * MaxSpeed) // Drive left with negative X (left)
            .withRotationalRate((drv.getLeftTriggerAxis() - drv.getRightTriggerAxis()) * AngularRate);
            // Left trigger turns left, right trigger turns right
        break;
      case "Split Joysticks Rotation Triggers":
        controlStyle = () -> drive.withVelocityX(-drv.getLeftY() * MaxSpeed) // Left stick forward/back
            .withVelocityY(-drv.getRightX() * MaxSpeed) // Right stick strafe
            .withRotationalRate((drv.getLeftTriggerAxis() - drv.getRightTriggerAxis()) * AngularRate);
            // Left trigger turns left, right trigger turns right
        break;
      case "2 Joysticks with Gas Pedal":
        controlStyle = () -> {
            var stickX = -drv.getLeftX();
            var stickY = -drv.getLeftY();
            var angle = Math.atan2(stickX, stickY);
            return drive.withVelocityX(Math.cos(angle) * drv.getRightTriggerAxis() * MaxSpeed) // left x * gas
                .withVelocityY(Math.sin(angle) * drv.getRightTriggerAxis() * MaxSpeed) // Angle of left stick Y * gas pedal
                .withRotationalRate(-drv.getRightX() * AngularRate); // Drive counterclockwise with negative X (left)
        };
        break;
    }
    try {
      drivetrain.getDefaultCommand().cancel();
    } catch(Exception e) {}
    drivetrain.setDefaultCommand( // Drivetrain will execute this command periodically
        drivetrain.applyRequest(controlStyle).ignoringDisable(true));
  }

  private void newSpeed() {
    lastSpeed = speedChooser.getSelected();
    MaxSpeed = TunerConstants.kSpeedAt12VoltsMps * lastSpeed;
  }
 
}
