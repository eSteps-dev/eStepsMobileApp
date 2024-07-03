import React, { useState, useEffect } from 'react';
import { NativeEventEmitter, View, Text, StyleSheet, NativeModules } from 'react-native';

interface TempProps {
  device: string;
  client: any;
}
const TempViewA = ({ device, client }: TempProps) => {
  const { TempConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionA);

  const [data, setData] = useState<any>();

  useEffect(() => {
    const subscription = eventEmitter.addListener('tempDataA', (tempDataA: any) => {
      const { temp, serialNumber } = tempDataA;

      setData(temp);
      if (tempDataA) {
        const message = new Paho.MQTT.Message(JSON.stringify(temp));
        message.destinationName = `${serialNumber}/temp`;
        const sendMqttMessage = () => {
          if (client.isConnected()) {
            client.send(message);
          } else {
            console.log('Not connected from Temp A');
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
    TempConnectionA.bindTempService(device);
    // Clean up the tempConnection and remove listeners when the component unmounts
    return () => {
      TempConnectionA.removeListeners('false');
      TempConnectionA.unbindTempService();
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

export default TempViewA;
