package frc.robot.subsystems;

import frc.robot.Constants;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.SparkMaxAlternateEncoder;
import com.revrobotics.CANSparkMax;

public class Wrist extends SubsystemBase
{
    public static CANSparkMax m_WristMotor;
    private SparkPIDController m_PidController;
    public static RelativeEncoder m_WristEncoder;
    private double m_TargetRotations;
    private double m_AngleToRotations = 0.11111111;
    public double CurrentAngle;

    /* @param PID[] array containing kP, kI, kD values, in that order
     * @param outputRange double storing min & max value for PID output
     * @param motorID id of motor controlling wrist */
    public Wrist(double []PID, double outputRange, int motorID) {
        
        if (PID.length != 3) {
            System.err.println("ERROR: invalid arg for Wrist initialisation. Must contain 3 kPID values.");
        }
        
        //Initialize motor
        m_WristMotor = new CANSparkMax(motorID, MotorType.kBrushless);

        //Reset to factory defaults
        m_WristMotor.restoreFactoryDefaults();
        m_WristMotor.setClosedLoopRampRate(1);

        //Constructs PID controller from SparkMax object
        m_PidController = m_WristMotor.getPIDController();

        //Creates encoder object to store location
        m_WristEncoder = m_WristMotor.getEncoder();

        //Initialize PID values
        m_PidController.setP(PID[0]);
        m_PidController.setI(PID[1]);
        m_PidController.setD(PID[2]);

        m_PidController.setIZone(0);
        m_PidController.setFF(0);
        m_PidController.setOutputRange( -outputRange, outputRange);

        m_TargetRotations = 0;

    }

    @Override

    public void periodic() {
        
        m_PidController.setReference(m_TargetRotations, CANSparkMax.ControlType.kPosition);
    
    }    

    public void setWristAngle (double angle) {

        m_TargetRotations = m_AngleToRotations * angle;
        CurrentAngle = angle;

    }
    
}
