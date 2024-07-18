import React, { useState, useEffect } from 'react';
import { NativeEventEmitter, View, Text, StyleSheet, NativeModules } from 'react-native';
import Paho from 'paho-mqtt';
import { pubsub } from '../../../pubSub';

interface TempProps {
  device: string;
}
const TempViewA = ({ device }: TempProps) => {
  const { TempConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionA);

  const [data, setData] = useState<string>('');

  useEffect(() => {
    const subscriptionTemp = eventEmitter.addListener('tempDataA', (tempDataA: any) => {
      const { temp, serialNumber, timestamp } = tempDataA;

      setData(Number(temp)?.toFixed(2));

      if (tempDataA) {
        const message = { temp: Number(temp)?.toFixed(2), timestamp: new Date(timestamp).getTime() };
        const topics = `esteps/temp/${serialNumber}`;
        const sendMqttMessage = async () => {
          await pubsub.publish({
            topics,
            message,
          });
        };
        sendMqttMessage();
      }
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscriptionTemp.remove();
    };
  }, []);

  useEffect(() => {
    TempConnectionA.bindTempService(device);
    // Clean up the tempConnection and remove listeners when the component unmounts
    return () => {
      TempConnectionA.unbindTempService();
    };
  }, []);

  return <Text style={styles.text}>Temp: {data}°</Text>;
};

const styles = StyleSheet.create({
  text: {
    textAlign: 'center',
  },
});

export default TempViewA;
