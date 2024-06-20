import React, { useState, useEffect } from 'react';
import { NativeEventEmitter, View, Text, StyleSheet, NativeModules } from 'react-native';

interface TempProps {
  device: string;
}
const TempViewA = ({ device }: TempProps) => {
  const { TempConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionA);
  const [tempSubscription, setTempSubscription] = useState<boolean>(false);

  const [data, setData] = useState<any>();

  useEffect(() => {
    const subscription = eventEmitter.addListener('tempDataA', (tempDataA: any) => {
      setData(tempDataA);
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    TempConnectionA.bindTempService(device);
    setTempSubscription(!tempSubscription);
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
