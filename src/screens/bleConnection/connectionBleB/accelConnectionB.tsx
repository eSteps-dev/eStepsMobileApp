import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface AccelProps {
  device: string;
  client: any;
}
const AccelConnectionViewB = ({ device, client }: AccelProps) => {
  const { AccelConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(AccelConnectionB);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('accelDataB', (accelDataB) => {
      const { x, y, z, serialNumber } = accelDataB;

      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
      const dataAccel = { x, y, z };
      if (accelDataB) {
        const message = new Paho.MQTT.Message(JSON.stringify(dataAccel));
        message.destinationName = `${serialNumber}/accel`;
        const sendMqttMessage = () => {
          if (client.isConnected()) {
            client.send(message);
          } else {
            console.log('Not connected from Accel B');
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
    AccelConnectionB.bindAccelService(device);

    return () => {
      AccelConnectionB.removeListeners('false');
      AccelConnectionB.unbindAccelService();
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

export default AccelConnectionViewB;
