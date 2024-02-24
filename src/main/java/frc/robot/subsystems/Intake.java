package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.Talon;
//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

//import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.Follower;

import java.time.Instant;
import frc.robot.Constants;

public class Intake extends SubsystemBase{
    
    private TalonFX m_intakeMotor;
    private TalonFX m_transferMotor;
    public static TalonFX m_storageMotor;
    private double m_Speed;
    private boolean m_Peaking;
    private Instant m_TimeToHold;
    private boolean m_IsHolding;
    private double m_StorageMotorSpeed;


    public Intake(int IntakeMotorID, int TransferMotorID, int StorageMotorID)
    {
        // Initialize intake motor
        m_intakeMotor = new TalonFX(IntakeMotorID);
        m_transferMotor = new TalonFX(TransferMotorID);
        m_storageMotor = new TalonFX(StorageMotorID);

        m_intakeMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_transferMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_storageMotor.getConfigurator().apply(new TalonFXConfiguration());

        m_transferMotor.setControl(new Follower(m_intakeMotor.getDeviceID(), false));

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
        m_storageMotor.set(m_StorageMotorSpeed);


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
    public void setSpeed(double speed, double storagespeed)
    {

        m_StorageMotorSpeed = storagespeed;
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
