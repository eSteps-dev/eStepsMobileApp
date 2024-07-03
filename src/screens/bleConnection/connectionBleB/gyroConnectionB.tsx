import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface GyroProps {
  device: string;
  client: any;
}
const GyroConnectionViewB = ({ device, client }: GyroProps) => {
  const { GyroConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(GyroConnectionB);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('gyroDataB', (gyroDataB) => {
      const { x, y, z, serialNumber } = gyroDataB;

      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
      const dataGyro = { x, y, z };
      if (gyroDataB) {
        const message = new Paho.MQTT.Message(JSON.stringify(dataGyro));
        message.destinationName = `${serialNumber}/gyro`;
        const sendMqttMessage = () => {
          if (client.isConnected()) {
            client.send(message);
          } else {
            console.log('Not connected from Accel');
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
    GyroConnectionB.bindGyroService(device);

    // Clean up the GyroConnection and remove listeners when the component unmounts
    return () => {
      GyroConnectionB.removeListeners('false');
      GyroConnectionB.unbindGyroService();
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

export default GyroConnectionViewB;
