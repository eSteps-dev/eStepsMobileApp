import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface GyroProps {
  device: string;
  client: any;
}
const GyroConnectionViewA = ({ device, client }: GyroProps) => {
  const { GyroConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(GyroConnectionA);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('gyroDataA', (gyroDataA) => {
      const { x, y, z, serialNumber } = gyroDataA;

      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
      const dataAccel = { x, y, z };
      if (gyroDataA) {
        const message = new Paho.MQTT.Message(JSON.stringify(dataAccel));
        message.destinationName = `${serialNumber}/gyro`;
        const sendMqttMessage = () => {
          if (client.isConnected()) {
            client.send(message);
          } else {
            console.log('Not connected from Gyro');
          }
        };
        sendMqttMessage();
      }
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    GyroConnectionA.bindGyroService(device);

    // Clean up the GyroConnection and remove listeners when the component unmounts
    return () => {
      GyroConnectionA.removeListeners('false');
      GyroConnectionA.unbindGyroService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Gyro: {device}</Text>
      <Text style={styles.text}>x: {data.x}</Text>
      <Text style={styles.text}>y: {data.y}</Text>
      <Text style={styles.text}>z: {data.z}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
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
