import React, { useState, useEffect } from 'react';
import { NativeModules, NativeEventEmitter, View, Text, TouchableOpacity, StyleSheet } from 'react-native';

interface AccelProps {
  device: string;
}
function AccelConnectionViewB({ device }: AccelProps) {
  const { AccelConnectionB } = NativeModules;
  const eventEmitter = new NativeEventEmitter(AccelConnectionB);
  const [accelSubscription, setAccelSubscription] = useState<boolean>(false);

  const [data, setData] = useState({
    x: 0,
    y: 0,
    z: 0,
  });

  useEffect(() => {
    const subscription = eventEmitter.addListener('accelDataB', (accelDataB) => {
      const [x, y, z] = accelDataB.split('/');
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
    AccelConnectionB.bindAccelService(device);
    setAccelSubscription(!accelSubscription);

    return () => {
      AccelConnectionB.removeListeners('false');
      AccelConnectionB.unbindAccelService();
    };
  }, []);

  const handleRemoveAccelSub = () => {
    AccelConnectionB.removeListeners('false');
    AccelConnectionB.unbindAccelService();

    setAccelSubscription(!accelSubscription);
  };
  const handleAccelSub = () => {
    AccelConnectionB.bindAccelService(device);
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

export default AccelConnectionViewB;
