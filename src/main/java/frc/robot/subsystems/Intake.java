package frc.robot.subsystems;


import edu.wpi.first.wpilibj2.command.SubsystemBase;

import  java.time.Instant;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.configs.*;


import frc.robot.Constants;

public class Intake extends SubsystemBase{
    
    private TalonFX m_intakeMotor;
    private TalonFX m_UpperIntakeMotor;
    private double m_UpperSpeed;
    private double m_Speed;
   // private boolean m_Peaking;
    //private Instant m_TimeToHold;
    private boolean m_IsHolding;
    private boolean m_IsRunning = false;
    private Instant m_CurrentBreakTarget;



    public Intake(int LowerIntakeMotorID, int UpperIntakeMotorID)
    {
        // Initialize intake motor
        m_intakeMotor = new TalonFX(LowerIntakeMotorID, "Canivore");
        m_intakeMotor.setNeutralMode(NeutralModeValue.Coast);

        m_UpperIntakeMotor = new TalonFX(UpperIntakeMotorID, "Canivore");
        m_UpperIntakeMotor.setNeutralMode(NeutralModeValue.Coast);
        

        m_intakeMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_UpperIntakeMotor.getConfigurator().apply(new TalonFXConfiguration());

        m_IsHolding = false;
        m_IsRunning = false;
        m_CurrentBreakTarget = Instant.now();


        m_Speed = 0;
    }

    /**
     * Sets motor speed to most recent value
     * Also checks for current spikes and readjusts to holding mode
     */
    @Override
    public void periodic()
    {

        //double upperCurrent = m_UpperIntakeMotor.getStatorCurrent().getValueAsDouble();
        double lowerCurrent = m_intakeMotor.getStatorCurrent().getValueAsDouble();

        if (lowerCurrent > Constants.Intake.pickedCurrentThreshold) {
           
            m_IsRunning = true;
        
        } else if (m_IsRunning && lowerCurrent < Constants.Intake.releasedCurrentThreshold) {
                
            m_IsRunning = false;
            m_IsHolding = true;
            m_CurrentBreakTarget = Instant.now().plusMillis(10);

        } else if (m_IsHolding && Instant.now().isAfter(m_CurrentBreakTarget)) {
        
            m_UpperIntakeMotor.set(0);

        } else {
            
            m_intakeMotor.set(m_Speed);
            m_UpperIntakeMotor.set(m_UpperSpeed);
            
        }


    }


    /**
     * @param speed double between -1 and 1 containing speed to set motor to
     */
    public void setSpeed(double speed)
    {

        m_Speed = speed;
        
        if (speed == 0) {
            
            m_IsHolding = false;
        
        }

    }

    public void setUpperSpeed(double UpperSpeed)
    {

        m_UpperSpeed = UpperSpeed;
        
        if (UpperSpeed == 0) {

            m_IsHolding = false;

        }
    }

}
