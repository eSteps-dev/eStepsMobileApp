import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, TouchableOpacity, StyleSheet } from 'react-native';

interface AccelProps {
  device: string;
}
function AccelConnectionViewA({ device }: AccelProps) {
  const { AccelConnectionA } = NativeModules;
  const eventEmitter = new NativeEventEmitter(AccelConnectionA);
  const [accelSubscription, setAccelSubscription] = useState<boolean>(false);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('accelDataA', (accelDataA) => {
      const [x, y, z] = accelDataA.split('/');
      setData((prevData) => ({
        ...prevData,
        x,
        y,
        z,
      }));
    });

    // Clean up the subscription when the component unmounts
    return () => {
      subscription.remove();
    };
  }, []);

  useEffect(() => {
    AccelConnectionA.bindAccelService(device);
    setAccelSubscription(!accelSubscription);

    return () => {
      AccelConnectionA.removeListeners('false');
      AccelConnectionA.unbindAccelService();
    };
  }, []);

  const handleRemoveAccelSub = () => {
    AccelConnectionA.removeListeners('false');
    AccelConnectionA.unbindAccelService();

    setAccelSubscription(!accelSubscription);
  };
  const handleAccelSub = () => {
    AccelConnectionA.bindAccelService(device);
    setAccelSubscription(!accelSubscription);
  };
  return (
    <View style={styles.container}>
      <Text style={styles.text}>Accel: {device}</Text>
      <Text style={styles.text}>x: {data.x}</Text>
      <Text style={styles.text}>y: {data.y}</Text>
      <Text style={styles.text}>z: {data.z}</Text>
      <View style={styles.buttonContainer}>
        <TouchableOpacity onPress={accelSubscription ? handleRemoveAccelSub : handleAccelSub} style={styles.button}>
          <Text>{accelSubscription ? 'On' : 'Off'}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

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

export default AccelConnectionViewA;
