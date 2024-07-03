import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface AccelProps {
  device: string;
  client: any;
}
const AccelConnectionViewA = ({ device, client }: AccelProps) => {
  const { AccelConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(AccelConnectionA);
  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('accelDataA', (accelDataA) => {
      const { x, y, z, serialNumber } = accelDataA;

      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
      const dataAccel = { x, y, z };
      if (accelDataA) {
        const message = new Paho.MQTT.Message(JSON.stringify(dataAccel));
        message.destinationName = `${serialNumber}/accel`;
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
    AccelConnectionA.bindAccelService(device);

    return () => {
      AccelConnectionA.removeListeners('false');
      AccelConnectionA.unbindAccelService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Accel: {device}</Text>
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

export default AccelConnectionViewA;
