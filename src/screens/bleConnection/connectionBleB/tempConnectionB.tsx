import React, { useState, useEffect } from 'react';
import { NativeEventEmitter, Text, StyleSheet, NativeModules } from 'react-native';
import { pubsub } from '../../../pubSub';

interface TempProps {
  device: string;
}
const TempViewB = ({ device }: TempProps) => {
  const { TempConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionB);

  const [data, setData] = useState<string>('');

  useEffect(() => {
    const subscriptionTemp = eventEmitter.addListener('tempDataB', (tempDataB: any) => {
      const { temp, serialNumber, timestamp } = tempDataB;

      setData(Number(temp)?.toFixed(2));

      if (tempDataB) {
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
    TempConnectionB.bindTempService(device);
    // Clean up the tempConnection and remove listeners when the component unmounts
    return () => {
      TempConnectionB.unbindTempService();
    };
  }, []);

  return <Text style={styles.text}>Temp: {data}°</Text>;
};

const styles = StyleSheet.create({
  text: {
    textAlign: 'center',
  },
});

export default TempViewB;
