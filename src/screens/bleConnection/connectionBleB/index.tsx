import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import TempViewB from './tempConnectionB';
import AccelConnectionViewB from './accelConnectionB';
import GyroConnectionViewB from './gyroConnectionB';

interface HomeProps {
  device: string;
}
export const HomeConnectionBleB = ({ device }: HomeProps) => {
  return (
    <View style={styles.container}>
      <Text style={styles.textStyle}>Device: {device}</Text>
      <TempViewB device={device} />
      <AccelConnectionViewB device={device} />
      <GyroConnectionViewB device={device} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'white',
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-between',
    padding: 10,
  },
  textStyle: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold',
    backgroundColor: '#F7C4B9',
  },
});
