import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';
import Paho from 'paho-mqtt';
import {pubsub} from '../../../pubSub'


interface AccelProps {
  device: string;
}
const MovementDetectionA = ({ device }: AccelProps) => {
  const { MovementDetectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(MovementDetectionA);
  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('movementData', (accelDataA) => {
      const { x, y, z, serialNumber, timestamp} = accelDataA;
   

      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
      const message = { x, y, z , timestamp: new Date(timestamp).getTime()};
      if (accelDataA) {
        const topics = `esteps/accel/${serialNumber}`;
        const sendMqttMessage = async() => {
          await pubsub.publish({ 
            topics,
            message
          });
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
    MovementDetectionA.bindMovementService(device);

    return () => {
        MovementDetectionA.removeListeners('false');
        MovementDetectionA.unbindMovementService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Accel: {device}</Text>
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

export default MovementDetectionA;
