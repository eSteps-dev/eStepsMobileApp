import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface TempProps {
  device: string;
  client: any;
}
const TempViewB = ({ device, client }: TempProps) => {
  const { TempConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionB);

  const [data, setData] = useState<any>();

  useEffect(() => {
    const subscription = eventEmitter.addListener('tempDataB', (tempDataB: any) => {
      const { temp, serialNumber } = tempDataB;

      setData(temp);
      if (tempDataB) {
        const message = new Paho.MQTT.Message(JSON.stringify(temp));
        message.destinationName = `${serialNumber}/temp`;
        const sendMqttMessage = () => {
          if (client.isConnected()) {
            client.send(message);
          } else {
            console.log('Not connected from Temp');
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
    TempConnectionB.bindTempService(device);
    // Clean up the tempConnection and remove listeners when the component unmounts
    return () => {
      TempConnectionB.removeListeners('false');
      TempConnectionB.unbindTempService();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>Temperature:</Text>
      <Text style={styles.text}>{data}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#E9DCD9',
  },
  text: {
    textAlign: 'center',
  },
});

export default TempViewB;
