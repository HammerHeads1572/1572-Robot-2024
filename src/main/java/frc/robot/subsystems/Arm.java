package frc.robot.subsystems;


import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.math.geometry.Rotation2d;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.*;

public class Arm extends SubsystemBase {
    
    public static TalonFX m_ArmDriveMotor;
    private TalonFX m_FollowMotor;
    private double m_TargetAngle;
    private CANcoder angleEncoder;
    public double TargetAngle;
    public Boolean armRotationComplete = true;

    double upperLimit = 145;
    double lowerLimit = -45;

    boolean wentOverCurrentLimit = false;
    boolean isOverCurrentLimit = false;

    //private double m_TicksToRotation = 0.000244140625;
    private double m_DegreesToRotation = 57.12;
    //19.64
    

    public static double arm_angle;

    /**
     * 
     * @param kPID: double array holding values for kP, kI, kD, in that order
     * @param leaderID: ID of the drive motor
     */
    public Arm(double []kPID, int leaderID, int followerID) {
        // Verify the length of kPID array
        if (kPID.length != 3)
        {
            System.err.println("ERROR: INVALID KPID LENGTH IN ARM INIT");
            return;
        }
        
      
        

        m_ArmDriveMotor = new TalonFX(leaderID,"Canivore");
        m_ArmDriveMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_ArmDriveMotor.setNeutralMode(NeutralModeValue.Brake);

        m_FollowMotor = new TalonFX(followerID,"Canivore");
        m_FollowMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_FollowMotor.setNeutralMode(NeutralModeValue.Brake);

        
        var armMotorConfigs = new TalonFXConfiguration();

        armMotorConfigs.Slot0.kS = 0.24;
        armMotorConfigs.Slot0.kV = 0.12;
        armMotorConfigs.Slot0.kP = kPID[0];
        armMotorConfigs.Slot0.kI = kPID[1];
        armMotorConfigs.Slot0.kD = kPID[2];
        
        // set Motion Magic settings
        armMotorConfigs.MotionMagic.MotionMagicCruiseVelocity = 60; // 80 rps cruise velocity
        armMotorConfigs.MotionMagic.MotionMagicAcceleration = 60; // 160 rps/s acceleration (0.5 seconds)
        armMotorConfigs.MotionMagic.MotionMagicJerk = 800; // 1600 rps/s^2 jerk (0.1 seconds)

        

        m_ArmDriveMotor.getConfigurator().apply(armMotorConfigs, 0.050);
       
        m_FollowMotor.getConfigurator().apply(armMotorConfigs, 0.050);
      
        m_TargetAngle = 0;
        
        // Attempt at global current limit

        CurrentLimitsConfigs StartingCurrentLimit = new CurrentLimitsConfigs().withSupplyCurrentLimit(40.0)
        .withSupplyCurrentLimitEnable(true)
        .withSupplyCurrentThreshold(40)
        .withSupplyTimeThreshold(0);
        
        m_ArmDriveMotor.getConfigurator().refresh(StartingCurrentLimit);
        m_FollowMotor.getConfigurator().refresh(StartingCurrentLimit);
        
    }

    /**
     * Periodic function. Creates new request and uses PID to set to angle?
     */
    @Override
    public void periodic()
    {
        // Create a position closed-loop request
       
        SmartDashboard.putData("arm encoder", m_ArmDriveMotor);
        SmartDashboard.putNumber("m_TargetAngle", m_TargetAngle);
       
        final MotionMagicVoltage m_request = new MotionMagicVoltage(0, true, 0.0, 0, true, false, false);
        
        final CurrentLimitsConfigs RestingCurrentLimit = new CurrentLimitsConfigs().withSupplyCurrentLimit(40.0)
        .withSupplyCurrentLimitEnable(true)
        .withSupplyCurrentThreshold(3)
        .withSupplyTimeThreshold(0);

        
        final CurrentLimitsConfigs NormalCurrentLimit = new CurrentLimitsConfigs().withSupplyCurrentLimit(40.0)
        .withSupplyCurrentLimitEnable(true)
        .withSupplyCurrentThreshold(40)
        .withSupplyTimeThreshold(0);

        if(arm_angle >= 140){
            if (wentOverCurrentLimit = true) {
                m_ArmDriveMotor.getConfigurator().apply(RestingCurrentLimit);
                m_FollowMotor.getConfigurator().apply(RestingCurrentLimit);
                wentOverCurrentLimit = false;
                isOverCurrentLimit = true;
            } else if (isOverCurrentLimit = false) {
                wentOverCurrentLimit = true;
            }
        }

        if (arm_angle < 140 && isOverCurrentLimit == true) {
            m_ArmDriveMotor.getConfigurator().apply(NormalCurrentLimit);
            m_FollowMotor.getConfigurator().apply(NormalCurrentLimit);
            isOverCurrentLimit = false;
        }
        
        if(arm_angle > upperLimit) {
            m_TargetAngle = upperLimit / 360 * m_DegreesToRotation;
        } else if (arm_angle < lowerLimit) {
            m_TargetAngle = lowerLimit / 360 * m_DegreesToRotation;
        }
           
        m_ArmDriveMotor.setControl(m_request.withPosition(m_TargetAngle));
        m_FollowMotor.setControl(new Follower(m_ArmDriveMotor.getDeviceID(), true));

        var armRotorPosSignal = m_ArmDriveMotor.getRotorPosition();
        var armRotorPos = armRotorPosSignal.getValue();

        SmartDashboard.putNumber("arm angle", ((armRotorPos / 1024*3.14)*m_DegreesToRotation));

        
    }
    
    public Rotation2d getCanCoder(){
        return Rotation2d.fromDegrees(angleEncoder.getAbsolutePosition().getValueAsDouble());
    }

    /**
     * Converts and sets angle
     * 
     * @param angle : angle in degrees
     */
    public void setArmAngle(double angle)
    {
        m_TargetAngle = angle / 360 * m_DegreesToRotation;
        arm_angle = angle;


        
        //TargetAngle = m_TargetAngle / m_DegreesToRotation;
        armRotationComplete = false;
    }
    
}


