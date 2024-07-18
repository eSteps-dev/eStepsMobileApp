import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface CalibrationProps {
  device: string;
}
const CalibrateSensor = ({ device }: CalibrationProps) => {
  const { CalibrateSensorA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(CalibrateSensorA);

  const [data, setData] = useState({
    accel: '',
    gyro: '',
    magne: '',
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('calibrationDataA', (eventData) => {
      setData((prevData) => ({
        ...prevData,
        accel: eventData.accel,
        gyro: eventData.gyro,
        magne: eventData.magne,
      }));
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    CalibrateSensorA.bindCalibrateService(device);

    return () => {
      CalibrateSensorA.unbindCalibrateService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Device: {device}</Text>
      <Text style={styles.text}>Accel: {data.accel}</Text>
      <Text style={styles.text}>Gyro: {data.gyro}</Text>
      <Text style={styles.text}>Magne: {data.magne}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  button: {
    alignItems: 'center',
    backgroundColor: '#eee',
    flex: 1,
    justifyContent: 'center',
    padding: 10,
  },
  buttonContainer: {
    alignItems: 'stretch',
    flexDirection: 'row',
    marginTop: 15,
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  text: {
    textAlign: 'center',
  },
});

export default CalibrateSensor;
