import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, TouchableOpacity, StyleSheet } from 'react-native';

interface GyroProps {
  device: string;
}
const GyroConnectionViewA = ({ device }: GyroProps) => {
  const { GyroConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(GyroConnectionA);
  const [gyroSubscription, setGyroSubscription] = useState<boolean>(false);
  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('gyroDataA', (gyroDataA) => {
      const [x, y, z] = gyroDataA.split('/');
      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    GyroConnectionA.bindGyroService(device);
    setGyroSubscription(!gyroSubscription);

    // Clean up the GyroConnection and remove listeners when the component unmounts
    return () => {
      GyroConnectionA.removeListeners('false');
      GyroConnectionA.unbindGyroService();
    };
  }, []);

  const handleRemoveGyroSub = () => {
    GyroConnectionA.removeListeners('false');
    GyroConnectionA.unbindGyroService();

    setGyroSubscription(!gyroSubscription);
  };
  const handleGyroSub = () => {
    GyroConnectionA.bindGyroService(device);
    setGyroSubscription(!gyroSubscription);
  };
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Gyro: {device}</Text>
      <Text style={styles.text}>x: {data.x}</Text>
      <Text style={styles.text}>y: {data.y}</Text>
      <Text style={styles.text}>z: {data.z}</Text>
      <View style={styles.buttonContainer}>
        <TouchableOpacity onPress={gyroSubscription ? handleRemoveGyroSub : handleGyroSub} style={styles.button}>
          <Text>{gyroSubscription ? 'On' : 'Off'}</Text>
        </TouchableOpacity>
      </View>
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

export default GyroConnectionViewA;
