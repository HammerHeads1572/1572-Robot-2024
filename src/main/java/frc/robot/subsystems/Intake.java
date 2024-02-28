package frc.robot.subsystems;

//import edu.wpi.first.wpilibj.motorcontrol.Talon;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

//import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.*;

import java.time.Instant;
import frc.robot.Constants;

public class Intake extends SubsystemBase{
    
    private TalonFX m_intakeMotor;
    private TalonFX m_UpperIntakeMotor;
    private double m_UpperSpeed;
    private double m_Speed;
    private boolean m_Peaking;
    private Instant m_TimeToHold;
    private boolean m_IsHolding;


    public Intake(int LowerIntakeMotorID, int UpperIntakeMotorID)
    {
        // Initialize intake motor
        m_intakeMotor = new TalonFX(LowerIntakeMotorID, "Canivore");
        m_UpperIntakeMotor = new TalonFX(UpperIntakeMotorID, "Canivore");
        

        m_intakeMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_UpperIntakeMotor.getConfigurator().apply(new TalonFXConfiguration());

        m_IsHolding = false;

        m_Speed = 0;
    }

    /**
     * Sets motor speed to most recent value
     * Also checks for current spikes and readjusts to holding mode
     */
    @Override
    public void periodic()
    {
        m_intakeMotor.set(m_Speed);
        m_UpperIntakeMotor.set(m_UpperSpeed);



       // SmartDashboard.putNumber("Intake Current", m_intakeMotor.getOutputCurrent());

        double current = m_intakeMotor.get();

        if (m_Speed < 0)
        {
            m_IsHolding = false;
        }
        else if (current > Constants.Intake.currentThreshold && !m_Peaking)
        {
            m_Peaking = true;
            m_TimeToHold = Instant.now().plusMillis(Constants.Intake.msToHold);
        }
        else if(m_Peaking)
        {
            if (current <= Constants.Intake.currentThreshold)
            {
                m_Peaking = false;
            }
            else if (Instant.now().isAfter(m_TimeToHold))
            {
                m_Speed = Constants.Intake.holdSpeed;
                m_IsHolding = true;
            }
        }

    }


    /**
     * @param speed double between -1 and 1 containing speed to set motor to
     */
    public void setSpeed(double speed)
    {

       
        if (speed != 0)
        {
            m_Speed = speed;
            m_IsHolding = false;
        }
        else if (!m_IsHolding)
        {
            m_Speed = 0;
        }
    }

    public void setUpperSpeed(double UpperSpeed)
    {

       
        if (UpperSpeed != 0)
        {
            m_UpperSpeed = UpperSpeed;
            m_IsHolding = false;
        }
        else if (!m_IsHolding)
        {
            m_Speed = 0;
        }
    }

}
