package frc.robot.subsystems;


import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.time.Instant;

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



    //private double m_TicksToRotation = 0.000244140625;
    private double m_DegreesToRotation = 19.64;
    //57.12;
    
    //Current limit / shutoff
    private boolean m_OverCurrent;
    private Instant m_CurrentBreakTarget;
    private boolean m_Disabled;
    private double m_MaxCurrent;
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
        armMotorConfigs.MotionMagic.MotionMagicCruiseVelocity = 40; // 80 rps cruise velocity
        armMotorConfigs.MotionMagic.MotionMagicAcceleration = 80; // 160 rps/s acceleration (0.5 seconds)
        armMotorConfigs.MotionMagic.MotionMagicJerk = 800; // 1600 rps/s^2 jerk (0.1 seconds)

        

        m_ArmDriveMotor.getConfigurator().apply(armMotorConfigs, 0.050);
       
        m_FollowMotor.getConfigurator().apply(armMotorConfigs, 0.050);
        
        m_MaxCurrent = 30.0;
        m_CurrentBreakTarget = Instant.now();
        m_Disabled = false;

        m_TargetAngle = 0;
        
        // Attempt at global current limit

        CurrentLimitsConfigs CurrentLimit = new CurrentLimitsConfigs().withSupplyCurrentLimit(40.0)
        .withSupplyCurrentLimitEnable(true)
        .withSupplyCurrentThreshold(40)
        .withSupplyTimeThreshold(0);
        
        m_ArmDriveMotor.getConfigurator().refresh(CurrentLimit);
        m_FollowMotor.getConfigurator().refresh(CurrentLimit);
        
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

        


         

        double current = m_ArmDriveMotor.getSupplyCurrent().getValueAsDouble();
        if (current > m_MaxCurrent && !m_OverCurrent)
        {
            m_OverCurrent = true;
            m_CurrentBreakTarget = Instant.now().plusMillis(2000);
        }
        else if(m_OverCurrent)
        {
            if (current <= m_MaxCurrent)
            {
                m_OverCurrent = false;
            }
            else if (Instant.now().isAfter(m_CurrentBreakTarget))
            {
                m_Disabled = true;
            }
        }
        
        if (m_Disabled)
        {
           // m_ArmDriveMotor.set(1);
           
        }
        else
        {
            m_ArmDriveMotor.setControl(m_request.withPosition(m_TargetAngle));
            m_FollowMotor.setControl(new Follower(m_ArmDriveMotor.getDeviceID(), true));

           
        }

        var armRotorPosSignal = m_ArmDriveMotor.getRotorPosition();
        var armRotorPos = armRotorPosSignal.getValue();

        SmartDashboard.putNumber("arm angle", (armRotorPos / 1024*3.14));

        
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
        if (angle == 0)
        {
            m_Disabled = false;
        }
        m_TargetAngle = angle * m_DegreesToRotation;
        arm_angle = angle;
        //TargetAngle = m_TargetAngle / m_DegreesToRotation;
        armRotationComplete = false;
    }
    
}


