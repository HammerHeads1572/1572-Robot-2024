package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.*;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Feeder extends SubsystemBase{

    public static TalonFX m_FeederMotor;
    private double m_FeederMotorSpeed;

    public Feeder(int FeederMotorID){
        m_FeederMotor = new TalonFX(FeederMotorID, "Canivore");

        m_FeederMotor.getConfigurator().apply(new TalonFXConfiguration());
        m_FeederMotor.setInverted(true);

    }

    @Override
    public void periodic()
    {
        m_FeederMotor.set(m_FeederMotorSpeed);

    }

    public void SetSpeed(double FeederSpeed){
        m_FeederMotorSpeed = FeederSpeed;
    }
}
