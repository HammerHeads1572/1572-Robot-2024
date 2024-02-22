package frc.robot.subsystems;

//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

//import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix.led.*;
import com.ctre.phoenix.led.CANdle.LEDStripType;


import java.time.Instant;
import frc.robot.Constants;

public class Shooter extends SubsystemBase{
    
    //private WPI_TalonFX m_Motor;
    private TalonFX m_ShooterLeader;
    private TalonFX m_ShooterFollower;
    private double m_Speed;
    private boolean m_Peaking;
    private Instant m_TimeToHold;
    private boolean m_IsHolding;

    public Shooter(int ShooterLeaderID, int ShooterFollowerID)
    {
        // Initialize intake motor
        m_ShooterLeader = new TalonFX(ShooterLeaderID);
        m_ShooterFollower = new TalonFX(ShooterFollowerID);


        m_ShooterLeader.getConfigurator().apply(new TalonFXConfiguration());
        m_ShooterFollower.getConfigurator().apply(new TalonFXConfiguration());

       // m_Motor.setInverted(true);
         

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
        //m_Motor.set(m_Speed);


       // SmartDashboard.putNumber("Intake Current", m_Motor.getOutputCurrent());

        //double current = m_Motor.getOutputCurrent();
        /*double current = m_Motor.get();

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
        */

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

}
