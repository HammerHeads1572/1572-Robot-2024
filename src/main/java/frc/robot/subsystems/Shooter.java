package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.MotionMagicVelocityVoltage;



public class Shooter extends SubsystemBase{
    
    private TalonFX m_RightShooter;
    private TalonFX m_LeftShooter;
    private double m_LeftSpeed;
    private double m_RightSpeed;


    public Shooter(double []kPID, int RightShooterID, int LeftShooterID)
    {
        // Initialize intake motor
        m_RightShooter = new TalonFX(RightShooterID, "Canivore");
        m_LeftShooter = new TalonFX(LeftShooterID, "Canivore");


        m_RightShooter.getConfigurator().apply(new TalonFXConfiguration());

        m_LeftShooter.getConfigurator().apply(new TalonFXConfiguration());


        
        var shooterMotorConfigs = new TalonFXConfiguration();


        shooterMotorConfigs.MotionMagic.MotionMagicCruiseVelocity = 80; // 80 rps cruise velocity
        shooterMotorConfigs.MotionMagic.MotionMagicAcceleration = 160; // 160 rps/s acceleration (0.5 seconds)
        shooterMotorConfigs.MotionMagic.MotionMagicJerk = 1600; // 1600 rps/s^2 jerk (0.1 seconds)

        shooterMotorConfigs.Slot0.kS = 0.24;
        shooterMotorConfigs.Slot0.kV = 0.12;
        shooterMotorConfigs.Slot0.kP = kPID[0];
        shooterMotorConfigs.Slot0.kI = kPID[1];
        shooterMotorConfigs.Slot0.kD = kPID[2];

        m_RightShooter.getConfigurator().apply(shooterMotorConfigs, 0.050);
        m_LeftShooter.getConfigurator().apply(shooterMotorConfigs, 0.050);

    }

    @Override
    public void periodic()
    {
        final MotionMagicVelocityVoltage m_request = new MotionMagicVelocityVoltage(0);
        
       // SmartDashboard.putNumber("Shooter Speed", m_Speed);

        m_RightShooter.setControl(m_request.withVelocity(-m_RightSpeed));
        m_LeftShooter.setControl(m_request.withVelocity(m_LeftSpeed));




       
    }


    /**
     * @param speed double between -1 and 1 containing speed to set motor to
     */
    public void setLeftSpeed(double Leftspeed)
    {

            m_LeftSpeed = Leftspeed;
    }

    public void setRightSpeed(double Rightspeed)
    {

            m_RightSpeed = Rightspeed;
    }

    public void ToggleShooter(){

        if(m_LeftSpeed != 0){
            m_LeftSpeed = 0;
            m_RightSpeed = 0;
        }
        else{
            m_LeftSpeed = Constants.shooterLeftSpeed;
            m_RightSpeed = Constants.shooterRightSpeed;
        }
    }

}
