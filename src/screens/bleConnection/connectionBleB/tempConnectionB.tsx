import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, StyleSheet } from 'react-native';

interface TempProps {
  device: string;
}
const TempViewB = ({ device }: TempProps) => {
  const { TempConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(TempConnectionB);
  const [tempSubscription, setTempSubscription] = useState<boolean>(false);

  const [data, setData] = useState<any>();

  useEffect(() => {
    const subscription = eventEmitter.addListener('tempDataB', (tempDataB: any) => {
      setData(tempDataB);
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    TempConnectionB.bindTempService(device);
    setTempSubscription(!tempSubscription);
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
