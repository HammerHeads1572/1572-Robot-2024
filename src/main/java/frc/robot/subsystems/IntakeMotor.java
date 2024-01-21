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

public class IntakeMotor extends SubsystemBase{
    
    //private WPI_TalonFX m_Motor;
    private TalonFX m_Motor;
    private double m_Speed;
    private boolean m_Peaking;
    private Instant m_TimeToHold;
    private boolean m_IsHolding;
    private CANdle Lights;

    public IntakeMotor(int motorID, int ArmLED)
    {
        // Initialize intake motor
        //m_Motor = new WPI_TalonFX(motorID);
        m_Motor = new TalonFX(motorID);
        //m_Motor.configFactoryDefault();
        m_Motor.getConfigurator().apply(new TalonFXConfiguration());

        m_Motor.setInverted(true);
        

        Lights = new CANdle(ArmLED,"Canivore");
        Lights.configFactoryDefault();
        Lights.configLEDType(LEDStripType.RGB);
        Lights.configBrightnessScalar(.5);
        Lights.setLEDs(128,0,0);
        
        

        

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
        m_Motor.set(m_Speed);



        if (m_Speed > 0){
            Lights.configBrightnessScalar(.7);
            Lights.setLEDs(0,255,0);

        }
        else if (m_Speed <0){
            Lights.configBrightnessScalar(.7);
            Lights.setLEDs(255, 0, 0);
        }
        else{
            Lights.configBrightnessScalar(.25);
            Lights.setLEDs(0,0,255);
        }


       // SmartDashboard.putNumber("Intake Current", m_Motor.getOutputCurrent());

        //double current = m_Motor.getOutputCurrent();
        double current = m_Motor.get();

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

}
